// # Canvas pixel grid, sequence counter, and region validation
use std::collections::{HashMap, HashSet};
use std::sync::Arc;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::mpsc::Sender;

use plaza_lib::cell::{Cell, Position};
use plaza_lib::color::Color;
use plaza_lib::geometry::Rect;
use plaza_lib::reply::Reply;

pub struct Subscription {
    pub region: Rect,
    pub tx: Sender<Reply>,
    pub diff_count: Arc<AtomicUsize>,
}

pub struct Palette;

impl Palette {
    /// Map byte to color
    pub fn code_to_color(code: u8) -> Result<Color, String> {
        match code {
            b'0' => Ok(Color::White),
            b'1' => Ok(Color::Silver),
            b'2' => Ok(Color::Gray),
            b'3' => Ok(Color::Black),
            b'4' => Ok(Color::Pink),
            b'5' => Ok(Color::Red),
            b'6' => Ok(Color::Orange),
            b'7' => Ok(Color::Brown),
            b'8' => Ok(Color::Yellow),
            b'9' => Ok(Color::Lime),
            b'a' => Ok(Color::Green),
            b'b' => Ok(Color::Cyan),
            b'c' => Ok(Color::Blue),
            b'd' => Ok(Color::Navy),
            b'e' => Ok(Color::Purple),
            b'f' => Ok(Color::Magenta),
            _ => Err(format!("'{}' is not a valid color code", code as char)),
        }
    }

    /// Convert color to byte
    pub fn color_to_code(color: Color) -> u8 {
        match color {
            Color::White => b'0',
            Color::Silver => b'1',
            Color::Gray => b'2',
            Color::Black => b'3',
            Color::Pink => b'4',
            Color::Red => b'5',
            Color::Orange => b'6',
            Color::Brown => b'7',
            Color::Yellow => b'8',
            Color::Lime => b'9',
            Color::Green => b'a',
            Color::Cyan => b'b',
            Color::Blue => b'c',
            Color::Navy => b'd',
            Color::Purple => b'e',
            Color::Magenta => b'f',
        }
    }

    /// Validate the byte can be mapped to a color
    pub fn is_valid_code(code: u8) -> bool {
        Self::code_to_color(code).is_ok()
    }
}

/// The Plaza Server grid struct, can be wrapped in Arc<Mutex<Canvas>> (for stage 2 onwards)
pub struct Canvas {
    width: u32,
    height: u32,
    pixels: Vec<u8>, // Stored as ASCII hex characters: b'0' ..= b'f'
    seq: u64,        // Global sequence counter, starts at 0
    subscriptions: HashMap<usize, Subscription>, // track all user subscriptions
    lag_budget: usize, // track max diffs per client
}

impl Canvas {
    /// Create new white canvas with width * height dimensions
    pub fn new(width: u32, height: u32, lag_budget: usize) -> Self {
        let size = (width as usize).saturating_mul(height as usize);
        Self {
            width,
            height,
            pixels: vec![b'0'; size],
            seq: 0,
            subscriptions: HashMap::new(),
            lag_budget,
        }
    }

    pub fn width(&self) -> u32 {
        self.width
    }

    pub fn height(&self) -> u32 {
        self.height
    }

    pub fn seq(&self) -> u64 {
        self.seq
    }

    /// Validate that point exists within bounds of the canvas
    pub fn check_point(&self, col: u32, row: u32) -> Result<usize, String> {
        if col >= self.width || row >= self.height {
            return Err(format!("(col {}, row {}) is off the canvas", col, row));
        }
        Ok((row as usize) * (self.width as usize) + (col as usize))
    }

    /// Validate that region exists within bounds of the canvas
    pub fn check_region(&self, region: &Rect) -> Result<(), String> {
        if region.width == 0 || region.height == 0 {
            return Err(format!(
                "region (col {}, row {}, width {}, height {}) has zero area",
                region.col, region.row, region.width, region.height
            ));
        }

        let max_col = region.col.checked_add(region.width);
        let max_row = region.row.checked_add(region.height);

        match (max_col, max_row) {
            (Some(c), Some(r)) if c <= self.width && r <= self.height => Ok(()),
            _ => Err(format!(
                "region (col {}, row {}, width {}, height {}) is off the canvas",
                region.col, region.row, region.width, region.height
            )),
        }
    }

    /// Read color byte at a specific pixel location
    pub fn read_pixel(&self, col: u32, row: u32) -> Result<u8, String> {
        let idx = self.check_point(col, row)?;
        Ok(self.pixels[idx])
    }

    /// Check if a point lies inside a bounding box
    fn rect_contains(rect: &Rect, col: u32, row: u32) -> bool {
        col >= rect.col
            && col < rect.col.saturating_add(rect.width)
            && row >= rect.row
            && row < rect.row.saturating_add(rect.height)
    }

    /// Registers or replaces subscription for a connection
    pub fn subscribe(
        &mut self,
        client_id: usize,
        region: Rect,
        tx: Sender<Reply>,
        diff_count: Arc<AtomicUsize>,
    ) -> Result<u64, String> {
        self.check_region(&region)?;
        // reset client queue tracker upon successful subscription
        diff_count.store(0, Ordering::Relaxed);
        self.subscriptions.insert(
            client_id,
            Subscription {
                region,
                tx,
                diff_count,
            },
        );
        Ok(self.seq)
    }

    /// Removes subscription for a connection.
    pub fn unsubscribe(&mut self, client_id: usize) {
        self.subscriptions.remove(&client_id);
    }

    /// Paint single pixel at given location and increment seq
    /// Even if color is unchanged, the write still commits and increments seq.
    pub fn paint(&mut self, col: u32, row: u32, color_code: u8) -> Result<u64, String> {
        let idx = self.check_point(col, row)?;
        if !Palette::is_valid_code(color_code) {
            return Err(format!(
                "'{}' is not a valid color code",
                color_code as char
            ));
        }

        self.seq += 1;
        self.pixels[idx] = color_code;

        let color = Palette::code_to_color(color_code).unwrap();
        let mut lagged = HashSet::new();
        for (&client_id, sub) in &self.subscriptions {
            if Self::rect_contains(&sub.region, col, row) {
                let prev_count = sub.diff_count.fetch_add(1, Ordering::SeqCst);

                if prev_count >= self.lag_budget {
                    // revert the increment so the counter doesn't overflow
                    sub.diff_count.fetch_sub(1, Ordering::SeqCst);
                    lagged.insert(client_id);
                } else {
                    let _ = sub.tx.send(Reply::Diff {
                        seq: self.seq,
                        cell: Cell {
                            position: Position { col, row },
                            color,
                        },
                    });
                }
            }
        }

        for id in lagged {
            if let Some(sub) = self.subscriptions.remove(&id) {
                let _ = sub.tx.send(Reply::Lagged);
            }
        }

        Ok(self.seq)
    }

    /// Atomically applies rectangular stamp of color codes
    /// Codes must be provided in row-major order matching region width × height.
    pub fn stamp(&mut self, region: &Rect, codes: &[u8]) -> Result<u64, String> {
        self.check_region(region)?;

        if region.width > 4 || region.height > 4 {
            return Err("stamps may be at most 4x4".to_string());
        }

        let expected_len = (region.width as usize) * (region.height as usize);
        if codes.len() != expected_len {
            return Err(format!(
                "expected {} color codes for {}x{} stamp, got {}",
                expected_len,
                region.width,
                region.height,
                codes.len()
            ));
        }

        for &code in codes {
            if !Palette::is_valid_code(code) {
                return Err(format!("'{}' is not a valid color code", code as char));
            }
        }

        self.seq += 1;

        for r in 0..region.height {
            for c in 0..region.width {
                let grid_idx = ((region.row + r) as usize) * (self.width as usize)
                    + ((region.col + c) as usize);
                let code_idx = (r * region.width + c) as usize;
                self.pixels[grid_idx] = codes[code_idx];
            }
        }

        let mut lagged = HashSet::new();

        // Broadcast contiguous diffs in row-major order per subscriber
        for (&client_id, sub) in &self.subscriptions {
            for r in 0..region.height {
                for c in 0..region.width {
                    let col = region.col + c;
                    let row = region.row + r;
                    if Self::rect_contains(&sub.region, col, row) {
                        if lagged.contains(&client_id) {
                            continue; // skip remaining pixels if already flagged as lagged
                        }

                        let prev_count = sub.diff_count.fetch_add(1, Ordering::SeqCst);
                        if prev_count >= self.lag_budget {
                            sub.diff_count.fetch_sub(1, Ordering::SeqCst);
                            lagged.insert(client_id);
                        } else {
                            let code = codes[(r * region.width + c) as usize];
                            let _ = sub.tx.send(Reply::Diff {
                                seq: self.seq,
                                cell: Cell {
                                    position: Position { col, row },
                                    color: Palette::code_to_color(code).unwrap(),
                                },
                            });
                        }
                    }
                }
            }
        }

        for id in lagged {
            if let Some(sub) = self.subscriptions.remove(&id) {
                let _ = sub.tx.send(Reply::Lagged);
            }
        }

        Ok(self.seq)
    }

    /// Get snapshot of requested region. If None, defaults to entire canvas
    /// Returns seq, the resolved Rect, and a 2D grid of colours.
    pub fn snapshot(&self, region: &Option<Rect>) -> Result<(u64, Rect, Vec<Vec<Color>>), String> {
        let target_rect = match region {
            Some(r) => *r,
            None => Rect {
                col: 0,
                row: 0,
                width: self.width,
                height: self.height,
            },
        };

        self.check_region(&target_rect)?;

        // 2D Vec<Vec<Color>> grid
        let mut rows = Vec::with_capacity(target_rect.height as usize);
        for r in 0..target_rect.height {
            let start = ((target_rect.row + r) as usize) * (self.width as usize)
                + (target_rect.col as usize);
            let end = start + (target_rect.width as usize);
            let row_slice = &self.pixels[start..end];

            let mut color_row = Vec::with_capacity(target_rect.width as usize);
            for &code in row_slice {
                let color = Palette::code_to_color(code)?;
                color_row.push(color);
            }
            rows.push(color_row);
        }

        Ok((self.seq, target_rect, rows))
    }
}
