use crate::card_handler;
use crate::joker_handler;
use ortalib::{Card, Edition, Enhancement, Joker, Rank, Suit};

pub struct ScoreState {
    pub chips: f64,
    pub mult: f64,
    pub explain: bool,
}

/// Logs a general scoring step (e.g., initial poker hand combo).
pub fn log_step(state: &ScoreState, message: &str) {
    if state.explain {
        println!("{} ({} x {})", message, state.chips, state.mult);
    }
}

/// Standardizes applying base_chips, base_mult, or xmult from enhancements/editions,
/// while handling the explain logging cleanly without repeated formatting code.
fn apply_and_log_modifier(
    state: &mut ScoreState,
    base_chips: f64,
    base_mult: f64,
    multiply_mult: bool,
    label: &str,
) {
    state.chips += base_chips;
    if multiply_mult {
        state.mult *= base_mult;
    } else {
        state.mult += base_mult;
    }

    if state.explain {
        let op = if multiply_mult { "x" } else { "+" };
        let val = if base_chips == 0.0 {
            base_mult
        } else {
            base_chips
        };
        // Dynamically print Mult vs Chips to keep explain logs accurate
        let unit = if base_chips == 0.0 { "Mult" } else { "Chips" };

        println!(
            "{} {}{} {} ({} x {})",
            label, op, val, unit, state.chips, state.mult
        );
    }
}

/// Evaluates and scores a single played card, including its base rank, enhancement, and edition.
/// Isolating this makes implementing retriggers (Hack, Hanging Chad, Dusk) effortless later.
pub fn score_played_card(
    card: &Card,
    active_jokers: &[Joker],
    pareidolia_active: &bool,
    smeared_active: &bool,
    is_first_face: &bool,
    state: &mut ScoreState,
) {
    let is_stone = card.enhancement == Some(Enhancement::Stone);
    let card_name = if is_stone {
        "Stone Card".to_string()
    } else {
        card_handler::format_card(card)
    };

    if is_stone {
        // Stone cards ignore rank/suit and score ONLY their enhancement chips
        let modifier_eval = card_handler::evaluate_card_enhancement(card, false);
        state.chips += modifier_eval.base_chips;

        if state.explain {
            println!(
                "Stone Card +{} Chips ({} x {})",
                modifier_eval.base_chips, state.chips, state.mult
            );
        }
    } else {
        // Normal cards score their base rank value first
        let card_val = card.rank.rank_value();
        state.chips += card_val;

        if state.explain {
            println!(
                "{} +{} Chips ({} x {})",
                card_name, card_val, state.chips, state.mult
            );
        }

        // Apply bonuses from non-Stone enhancements
        if let Some(enhancement) = &card.enhancement && !matches!(enhancement, Enhancement::Wild | Enhancement::Steel) {
            let modifier_eval = card_handler::evaluate_card_enhancement(card, false);
            apply_and_log_modifier(
                state,
                modifier_eval.base_chips,
                modifier_eval.base_mult,
                modifier_eval.multiply_mult,
                &format!("{} {}", card_name, enhancement),
            );
        }
    }

    // Apply bonuses from card editions (e.g., Foil, Holographic, Polychrome)
    if let Some(edition) = &card.edition {
        let modifier_eval = card_handler::evaluate_card_edition(card);
        apply_and_log_modifier(
            state,
            modifier_eval.base_chips,
            modifier_eval.base_mult,
            modifier_eval.multiply_mult,
            &format!("{} {}", card_name, edition),
        );
    }

    // On-Held Card Jokers (Iterate to support multiple copies of the same Joker)
    for joker in active_jokers {
        match joker {
            Joker::GreedyJoker => {
                if card_handler::matches_suit(card, &Suit::Diamonds, smeared_active) {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        3.0,
                        false,
                        &format!("{} Greedy Joker", card_handler::format_card(card)),
                    );
                }
            }
            Joker::LustyJoker => {
                if card_handler::matches_suit(card, &Suit::Hearts, smeared_active) {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        3.0,
                        false,
                        &format!("{} Lusty Joker", card_handler::format_card(card)),
                    );
                }
            }
            Joker::Arrowhead => {
                if card_handler::matches_suit(card, &Suit::Spades, smeared_active) {
                    apply_and_log_modifier(
                        state,
                        50.0,
                        0.0,
                        false,
                        &format!("{} Arrowhead", card_handler::format_card(card)),
                    );
                }
            }
            Joker::OnyxAgate => {
                if card_handler::matches_suit(card, &Suit::Clubs, smeared_active) {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        7.0,
                        false,
                        &format!("{} Onyx Agate", card_handler::format_card(card)),
                    );
                }
            }
            Joker::Fibonacci => {
                if card_handler::is_fibonacci_target(card) {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        8.0,
                        false,
                        &format!("{} Fibonacci", card_handler::format_card(card)),
                    );
                }
            }
            Joker::EvenSteven => {
                if card_handler::is_even(card) {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        4.0,
                        false,
                        &format!("{} Even Steven", card_handler::format_card(card)),
                    );
                }
            }
            Joker::OddTodd => {
                if card_handler::is_odd(card) {
                    apply_and_log_modifier(
                        state,
                        31.0,
                        0.0,
                        false,
                        &format!("{} Odd Todd", card_handler::format_card(card)),
                    );
                }
            }
            Joker::Scholar => {
                if card.rank == Rank::Ace {
                    apply_and_log_modifier(
                        state,
                        20.0,
                        4.0,
                        false,
                        &format!("{} Scholar", card_handler::format_card(card)),
                    );
                }
            }
            Joker::WalkieTalkie => {
                if card_handler::is_walkie_talkie_target(card) {
                    apply_and_log_modifier(
                        state,
                        10.0,
                        4.0,
                        false,
                        &format!("{} Walkie Talkie", card_handler::format_card(card)),
                    );
                }
            }
            Joker::ScaryFace => {
                if card_handler::is_face(card, pareidolia_active) {
                    apply_and_log_modifier(
                        state,
                        30.0,
                        0.0,
                        false,
                        &format!("{} Scary Face", card_handler::format_card(card)),
                    );
                }
            }
            Joker::SmileyFace => {
                if card_handler::is_face(card, pareidolia_active) {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        5.0,
                        false,
                        &format!("{} Smiley Face", card_handler::format_card(card)),
                    );
                }
            }
            Joker::Photograph
                if *is_first_face && card_handler::is_face(card, pareidolia_active) =>
            {
                apply_and_log_modifier(
                    state,
                    0.0,
                    2.0,
                    true, // multiply_mult (x2 Mult)
                    &format!("{} Photograph", card_handler::format_card(card)),
                );
            }
            _ => {} // Other jokers don't trigger contextually on individual held cards
        }
    }
}

/// Evaluates and scores a single card held in hand (e.g., Steel cards).
/// Also scores based on On-Held Jokers
pub fn score_held_card(
    card: &Card,
    active_jokers: &[Joker],
    raised_fist_target: Option<&Card>,
    state: &mut ScoreState,
) {
    // 1. Inherent Card Enhancements (Steel)
    if card.enhancement == Some(Enhancement::Steel) {
        let modifier_eval = card_handler::evaluate_card_enhancement(card, true);
        apply_and_log_modifier(
            state,
            modifier_eval.base_chips,
            modifier_eval.base_mult,
            modifier_eval.multiply_mult,
            &format!("{} {}", card_handler::format_card(card), Enhancement::Steel),
        );
    }

    // 2. On-Held Card Jokers (Iterate to support multiple copies of the same Joker)
    for joker in active_jokers {
        match joker {
            Joker::Baron => {
                if card.rank == Rank::King {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        1.5,
                        true, // multiply_mult
                        &format!("{} Baron", card_handler::format_card(card)),
                    );
                }
            }
            Joker::ShootTheMoon => {
                if card.rank == Rank::Queen {
                    apply_and_log_modifier(
                        state,
                        0.0,
                        13.0,
                        false, // add_mult
                        &format!("{} Shoot the Moon", card_handler::format_card(card)),
                    );
                }
            }
            Joker::RaisedFist => {
                if let Some(target) = raised_fist_target && std::ptr::eq(card, target) {
                    let fist_mult_value =
                        (card_handler::card_rank_value(&card.rank) * 2) as f64;
                    apply_and_log_modifier(
                        state,
                        0.0,
                        fist_mult_value,
                        false, // add_mult
                        &format!("{} Raised Fist", card_handler::format_card(card)),
                    );
                }
            }
            _ => {} // Other jokers don't trigger contextually on individual held cards
        }
    }
}

/// Evaluates and scores an individual Joker.
/// Isolating this allows Blueprint and Brainstorm to pass target Joker references directly here.
pub fn score_joker(
    joker: &Joker,
    edition: Option<&Edition>,
    context: &joker_handler::JokerContext,
    state: &mut ScoreState,
) {
    // Edition (Foil or Holographic)
    if let Some(ed) = edition {
        match ed {
            Edition::Foil => {
                state.chips += 50.0;
                if state.explain {
                    println!(
                        "{:?} Foil +50 Chips ({} x {})",
                        joker, state.chips, state.mult
                    );
                }
            }
            Edition::Holographic => {
                state.mult += 10.0;
                if state.explain {
                    println!(
                        "{:?} Holographic +10 Mult ({} x {})",
                        joker, state.chips, state.mult
                    );
                }
            }
            _ => {} // Polychrome and Negative are handled elsewhere/later
        }
    }

    // 2. "Independent" Joker ability activates
    let joker_eval = joker_handler::evaluate_joker(joker, context);
    let mut triggered = false;

    if joker_eval.chips > 0.0 {
        state.chips += joker_eval.chips;
        triggered = true;
    }

    if joker_eval.mult > 0.0 {
        state.mult += joker_eval.mult;
        triggered = true;
    }

    if joker_eval.xmult > 1.0 {
        state.mult *= joker_eval.xmult;
        triggered = true;
    }

    if state.explain && triggered {
        let bonus_str = if joker_eval.xmult > 1.0 {
            format!("x{} Mult", joker_eval.xmult)
        } else if joker_eval.mult > 0.0 {
            format!("+{} Mult", joker_eval.mult)
        } else {
            format!("+{} Chips", joker_eval.chips)
        };

        println!(
            "{:?} {} ({} x {})",
            joker, bonus_str, state.chips, state.mult
        );
    }

    // Edition (Polychrome)
    if let Some(Edition::Polychrome) = edition {
        state.mult *= 1.5;
        if state.explain {
            println!(
                "{:?} Polychrome x1.5 Mult ({} x {})",
                joker, state.chips, state.mult
            );
        }
    }
}
