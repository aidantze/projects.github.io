use std::error::Error;

use clap::Parser;

use plaza::{Config, start_server};
use plaza_lib::connect::{ConnectionManager, TerminalManager, resolve_address};

/// The Plaza server.
///
/// You should not need to change this file: the autotests rely on these
/// arguments existing and meaning what they say.
#[derive(Parser, Debug)]
struct Args {
    /// Address to listen on; terminal mode if absent
    addr: Option<String>,

    /// Canvas width in pixels
    #[arg(long, default_value_t = 64)]
    width: u32,

    /// Canvas height in pixels
    #[arg(long, default_value_t = 64)]
    height: u32,

    /// Cooldown charged per written pixel, in Plaza-clock milliseconds
    #[arg(long, default_value_t = 0)]
    cooldown: u64,

    /// Maximum diffs queued to a subscriber before it is cut off
    #[arg(long, default_value_t = 64)]
    lag_budget: usize,

    /// Hides the contents of error messages
    #[arg(long, default_value_t = false)]
    mark_mode: bool,
}

fn main() -> Result<(), Box<dyn Error>> {
    let args = Args::parse();

    let config = Config {
        width: args.width,
        height: args.height,
        cooldown_ms: args.cooldown,
        lag_budget: args.lag_budget,
    };

    if let Some(addr) = args.addr {
        let addr = resolve_address(&addr)?;
        start_server(ConnectionManager::launch(addr.ip(), addr.port()), config)
    } else {
        start_server(TerminalManager::launch(args.mark_mode), config)
    }
}
