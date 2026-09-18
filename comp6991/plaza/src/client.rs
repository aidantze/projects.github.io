// # Per-connection handler, cooldown tracking, and non-blocking writer
use plaza_lib::clock;

use crate::region::Region;

#[derive(Default)]
pub struct ClientState {
    pub last_write_until_ms: u64, // Cooldown deadline timestamp
    pub subscription: Option<Region>,
}

impl ClientState {
    pub fn new() -> Self {
        Self {
            last_write_until_ms: 0,
            subscription: None,
        }
    }

    /// Check if connection currently on cooldown
    /// Returns Some(remaining_ms) if rate limited, None otherwise
    pub fn check_cooldown(&self) -> Option<u64> {
        let now = clock::now_ms();
        if now < self.last_write_until_ms {
            Some(self.last_write_until_ms - now)
        } else {
            None
        }
    }

    /// Apply cooldown to new deadline
    /// num_pixels: 1 for Paint, width * height for Stamp
    pub fn apply_cooldown(&mut self, cooldown_ms: u64, num_pixels: u64) {
        if cooldown_ms == 0 {
            return;
        }
        let now = clock::now_ms();
        let added_time = cooldown_ms.saturating_mul(num_pixels);
        self.last_write_until_ms = now.saturating_add(added_time);
    }
}
