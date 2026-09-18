use std::error::Error;
use std::sync::atomic::{AtomicUsize, Ordering};
use std::sync::mpsc;
use std::sync::{Arc, Mutex};
use std::thread;

use plaza_lib::cell::{Cell, Position};
use plaza_lib::connect::{
    Connection, Manager, ReadMessageResult, Reader, WriteMessageResult, Writer,
};
use plaza_lib::reply::Reply;
use plaza_lib::request::Request;

pub mod canvas;
pub mod client;
pub mod region;

use canvas::{Canvas, Palette};
use client::ClientState;

enum ExitReason {
    Clean,
    Error(plaza_lib::connect::ConnectionError),
}

/// The server's configuration, filled in from the command line by
/// `main.rs`. The autotests set these flags, so make sure your server
/// honours them.
#[derive(Debug, Clone, Copy)]
pub struct Config {
    /// Canvas width, in pixels.
    pub width: u32,
    /// Canvas height, in pixels.
    pub height: u32,
    /// After a write commits, the writer may not write again for
    /// `cooldown_ms * (number of pixels written)` Plaza-clock
    /// milliseconds. 0 disables rate limiting entirely.
    pub cooldown_ms: u64,
    /// The maximum number of diffs the server may hold queued for one
    /// subscriber. A subscriber that would exceed it gets "lagged"
    /// instead, and loses its subscription.
    pub lag_budget: usize,
}

static NEXT_CLIENT_ID: AtomicUsize = AtomicUsize::new(1);

pub fn start_server<M>(mut manager: M, config: Config) -> Result<(), Box<dyn Error>>
where
    M: Manager,
{
    let canvas = Arc::new(Mutex::new(Canvas::new(
        config.width,
        config.height,
        config.lag_budget,
    )));
    let mut handles = Vec::new();

    // This starter code initiates a single client connection, and reads and
    // writes messages indefinitely.
    // You will need to implement multiple connections for Stage 2 onwards.
    // let (mut recv, mut send) = match manager.accept_new_connection() {
    //     Connection::NewConnection { reader, writer } => (reader, writer),
    //     Connection::NoMoreConnections => {
    //         // There are no more new connections to accept.
    //         return Ok(());
    //     }
    // };

    while let Connection::NewConnection {
        mut reader,
        mut writer,
    } = manager.accept_new_connection() {
        let canvas_handle = Arc::clone(&canvas);

        // assign unique ID for mpsc channel
        let client_id = NEXT_CLIENT_ID.fetch_add(1, Ordering::Relaxed);
        let (tx, rx) = mpsc::channel();

        // create writer and reader based on arc (for stage 6 lagged budget counter)
        let diff_count = Arc::new(AtomicUsize::new(0));
        let diff_count_writer = Arc::clone(&diff_count);
        let diff_count_reader = Arc::clone(&diff_count);

        // spawn writer thread
        let _writer_handle = thread::spawn(move || {
            while let Ok(reply) = rx.recv() {
                let is_diff = matches!(reply, Reply::Diff { .. });
                match writer.write_message(reply) {
                    WriteMessageResult::Ok => {
                        // decrement after successful TCP write
                        if is_diff {
                            diff_count_writer.fetch_sub(1, Ordering::Relaxed);
                        }
                    }
                    WriteMessageResult::ConnectionClosed | WriteMessageResult::Err(_) => {
                        break;
                    }
                }
            }
        });

        // main thread handle
        let handle = thread::spawn(
            move || -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
                let mut client = ClientState::new();
                let exit_reason: ExitReason; // used for proper error returning

                loop {
                    match reader.read_message() {
                        ReadMessageResult::Message(message) => {
                            let reply = match message.parse::<Request>() {
                                Ok(request) => match request {
                                    Request::Paint { cell } => {
                                        // check existing cooldown active
                                        if let Some(ms) = client.check_cooldown() {
                                            Reply::Cooldown { remaining_ms: ms }
                                        } else {
                                            let code = Palette::color_to_code(cell.color);

                                            // lock mutex while painting
                                            let paint_result =
                                                canvas_handle.lock().unwrap().paint(
                                                    cell.position.col,
                                                    cell.position.row,
                                                    code,
                                                );
                                            match paint_result {
                                                Ok(seq) => {
                                                    client.apply_cooldown(
                                                        config.cooldown_ms,
                                                        1,
                                                    );
                                                    Reply::Painted { seq }
                                                }
                                                Err(e) => Reply::Error(e),
                                            }
                                        }
                                    }
                                    Request::Read { position } => {
                                        let read_result = canvas_handle
                                            .lock()
                                            .unwrap()
                                            .read_pixel(position.col, position.row);
                                        match read_result {
                                            Ok(code) => {
                                                match Palette::code_to_color(code) {
                                                    Ok(color) => Reply::Pixel {
                                                        cell: Cell {
                                                            position: Position {
                                                                col: position.col,
                                                                row: position.row,
                                                            },
                                                            color,
                                                        },
                                                    },
                                                    Err(e) => Reply::Error(e),
                                                }
                                            }
                                            Err(e) => Reply::Error(e),
                                        }
                                    }
                                    Request::Snapshot { region } => {
                                        let snapshot_result =
                                            canvas_handle.lock().unwrap().snapshot(&region);

                                        match snapshot_result {
                                            Ok((seq, target_rect, rows)) => {
                                                Reply::Snapshot {
                                                    seq,
                                                    region: target_rect,
                                                    rows,
                                                }
                                            }
                                            Err(e) => Reply::Error(e),
                                        }
                                    }
                                    Request::Stamp { region, colors } => {
                                        if let Some(ms) = client.check_cooldown() {
                                            Reply::Cooldown { remaining_ms: ms }
                                        } else {
                                            let codes: Vec<u8> = colors
                                                .into_iter()
                                                .map(Palette::color_to_code)
                                                .collect();

                                            let stamp_result = canvas_handle
                                                .lock()
                                                .unwrap()
                                                .stamp(&region, &codes);

                                            match stamp_result {
                                                Ok(seq) => {
                                                    let num_pixels = (region.width as u64)
                                                        .saturating_mul(
                                                            region.height as u64,
                                                        ); // width * height
                                                    client.apply_cooldown(
                                                        config.cooldown_ms,
                                                        num_pixels,
                                                    );

                                                    Reply::Stamped { seq }
                                                }
                                                Err(e) => Reply::Error(e),
                                            }
                                        }
                                    }
                                    Request::Subscribe { region } => {
                                        let sub_result =
                                            canvas_handle.lock().unwrap().subscribe(
                                                client_id,
                                                region,
                                                tx.clone(),
                                                Arc::clone(&diff_count_reader),
                                            );

                                        match sub_result {
                                            Ok(seq) => Reply::Subscribed { seq },
                                            Err(e) => Reply::Error(e),
                                        }
                                    }
                                    Request::Unsubscribe => {
                                        canvas_handle
                                            .lock()
                                            .unwrap()
                                            .unsubscribe(client_id);
                                        Reply::Unsubscribed
                                    }
                                },
                                Err(err) => Reply::Error(err.to_string()),
                            };

                            if tx.send(reply).is_err() {
                                exit_reason = ExitReason::Clean; // writer thread died
                                break;
                            }
                        }
                        ReadMessageResult::ConnectionClosed => {
                            // connection closed, terminate connection but don't classify as an error
                            exit_reason = ExitReason::Clean;
                            break;
                        }
                        ReadMessageResult::Err(e) => {
                            // unexpected error occurred
                            exit_reason = ExitReason::Error(e);
                            break;
                        }
                    }
                }

                // safe cleanup for ongoing subscribed thread (just in case)
                canvas_handle.lock().unwrap().unsubscribe(client_id);
                match exit_reason {
                    ExitReason::Clean => Ok(()),
                    ExitReason::Error(e) => Err(Box::new(e)), // for proper error returning
                }
            },
        );

        handles.push(handle);
    }

    let mut final_result: Result<(), Box<dyn Error>> = Ok(());

    // if one of the handles returns an error, it sets final_result to Err.
    // we still need to join the remaining handles, but only first error gets returned
    for handle in handles {
        if let Ok(Err(thread_error)) = handle.join() && final_result.is_ok() {
            // found error but final_result hasn't been reassigned yet
            final_result = Err(thread_error);
        }
    }

    final_result
    // Ok(())
}
