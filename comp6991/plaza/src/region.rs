// # Region bounds
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub struct Region {
    pub col: u32,
    pub row: u32,
    pub width: u32,
    pub height: u32,
}

impl Region {
    pub fn new(col: u32, row: u32, width: u32, height: u32) -> Self {
        Self {
            col,
            row,
            width,
            height,
        }
    }

    /// Check of coordinate exists in region
    pub fn contains(&self, col: u32, row: u32) -> bool {
        col >= self.col
            && col < self.col.saturating_add(self.width)
            && row >= self.row
            && row < self.row.saturating_add(self.height)
    }

    /// Check if region overlaps other region (used for stamps)
    pub fn overlaps(&self, other: &Region) -> bool {
        self.col < other.col.saturating_add(other.width)
            && self.col.saturating_add(self.width) > other.col
            && self.row < other.row.saturating_add(other.height)
            && self.row.saturating_add(self.height) > other.row
    }
}
