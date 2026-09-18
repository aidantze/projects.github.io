use crate::card_handler;
use ortalib::{Card, Joker, PokerHand, Rank, Suit};

#[derive(Debug)]
pub struct JokerEvaluation {
    pub chips: f64,
    pub mult: f64,
    pub xmult: f64,
}

impl Default for JokerEvaluation {
    fn default() -> Self {
        Self {
            chips: 0.0,
            mult: 0.0,
            xmult: 1.0,
        }
    }
}

pub struct JokerContext<'a> {
    pub played_hand: &'a PokerHand,
    pub scoring_cards: &'a Vec<&'a Card>,
    pub total_jokers: usize,
    pub smeared_active: bool,
    pub cards_played: &'a Vec<Card>,
    pub active_jokers: &'a Vec<Joker>,
}

// ============================================================================
// EVALUATE JOKER HAND AND DETERMINE RETRIGGERS BASED ON JOKER HAND
// ============================================================================

pub fn evaluate_joker(joker: &Joker, joker_context: &JokerContext) -> JokerEvaluation {
    let mut eval = JokerEvaluation::default();

    match joker {
        Joker::Joker => {
            eval.mult = 4.0;
        }

        // --- Mult Jokers ---
        Joker::JollyJoker => {
            if check_pair(joker_context.played_hand, joker_context.scoring_cards) {
                eval.mult = 8.0;
            }
        }
        Joker::SlyJoker => {
            if check_pair(joker_context.played_hand, joker_context.scoring_cards) {
                eval.chips = 50.0;
            }
        }
        Joker::ZanyJoker => {
            if check_three_of_a_kind(joker_context.played_hand, joker_context.scoring_cards) {
                eval.mult = 12.0;
            }
        }
        Joker::WilyJoker => {
            if check_three_of_a_kind(joker_context.played_hand, joker_context.scoring_cards) {
                eval.chips = 100.0;
            }
        }
        Joker::MadJoker => {
            if check_two_pair(joker_context.played_hand, joker_context.scoring_cards) {
                eval.mult = 10.0;
            }
        }
        Joker::CleverJoker => {
            if check_two_pair(joker_context.played_hand, joker_context.scoring_cards) {
                eval.chips = 80.0;
            }
        }
        Joker::TheOrder => {
            if check_straight(
                joker_context.played_hand,
                joker_context.cards_played,
                joker_context.active_jokers,
            ) {
                eval.xmult = 3.0;
            }
        }
        Joker::DeviousJoker => {
            if check_straight(
                joker_context.played_hand,
                joker_context.cards_played,
                joker_context.active_jokers,
            ) {
                eval.chips = 100.0;
            }
        }
        Joker::TheTribe => {
            if check_flush(
                joker_context.played_hand,
                joker_context.cards_played,
                joker_context.active_jokers,
                joker_context.smeared_active,
            ) {
                eval.xmult = 2.0;
            }
        }
        Joker::CraftyJoker => {
            if check_flush(
                joker_context.played_hand,
                joker_context.cards_played,
                joker_context.active_jokers,
                joker_context.smeared_active,
            ) {
                eval.chips = 80.0;
            }
        }

        // --- Other Independent Jokers ---
        Joker::AbstractJoker => {
            eval.mult = (joker_context.total_jokers as f64) * 3.0;
        }
        // Joker::Blackboard moved to score.rs
        Joker::FlowerPot
            if has_unique_suit_coverage(
                joker_context.scoring_cards,
                joker_context.smeared_active,
            ) =>
        {
            eval.xmult = 3.0;
        }
        _ => {}
    }

    eval
}

/// Apply retriggers to relevant scoring cards
pub fn get_scoring_card_retriggers(
    card: &Card,
    is_first_scoring_card: bool,
    active_jokers: &[Joker],
    is_pareidolia: &bool,
) -> usize {
    let mut retriggers = 0;

    for joker in active_jokers {
        match joker {
            Joker::HangingChad if is_first_scoring_card => {
                retriggers += 2;
            }
            Joker::SockAndBuskin if card_handler::is_face(card, is_pareidolia) => {
                retriggers += 1;
            }
            Joker::Hack if card_handler::is_hack_target(card) => {
                retriggers += 1;
            }
            _ => {}
        }
    }

    retriggers
}

/// Apply mime to held cards
pub fn get_held_card_retriggers(active_jokers: &[Joker]) -> usize {
    let mut retriggers = 0;

    for joker in active_jokers {
        if matches!(joker, Joker::Mime) {
            retriggers += 1;
        }
    }

    retriggers
}

// ============================================================================
// EVALUATION HELPERS
// ============================================================================

fn rank_to_index(rank: &Rank) -> usize {
    match rank {
        Rank::Two => 0,
        Rank::Three => 1,
        Rank::Four => 2,
        Rank::Five => 3,
        Rank::Six => 4,
        Rank::Seven => 5,
        Rank::Eight => 6,
        Rank::Nine => 7,
        Rank::Ten => 8,
        Rank::Jack => 9,
        Rank::Queen => 10,
        Rank::King => 11,
        Rank::Ace => 12,
    }
}

pub fn check_pair(hand: &PokerHand, scoring_cards: &Vec<&Card>) -> bool {
    if matches!(
        hand,
        PokerHand::Pair
            | PokerHand::TwoPair
            | PokerHand::ThreeOfAKind
            | PokerHand::FullHouse
            | PokerHand::FourOfAKind
            | PokerHand::FiveOfAKind
            | PokerHand::FlushHouse
            | PokerHand::FlushFive
    ) {
        return true;
    }
    let mut counts = [0; 13];
    for card in scoring_cards {
        if card.enhancement != Some(ortalib::Enhancement::Stone) {
            counts[rank_to_index(&card.rank)] += 1;
        }
    }
    for &count in counts.iter() {
        if count >= 2 {
            return true;
        }
    }
    false
}

pub fn check_three_of_a_kind(hand: &PokerHand, scoring_cards: &Vec<&Card>) -> bool {
    if matches!(
        hand,
        PokerHand::ThreeOfAKind
            | PokerHand::FullHouse
            | PokerHand::FourOfAKind
            | PokerHand::FiveOfAKind
            | PokerHand::FlushHouse
            | PokerHand::FlushFive
    ) {
        return true;
    }
    let mut counts = [0; 13];
    for card in scoring_cards {
        if card.enhancement != Some(ortalib::Enhancement::Stone) {
            counts[rank_to_index(&card.rank)] += 1;
        }
    }
    for &count in counts.iter() {
        if count >= 3 {
            return true;
        }
    }
    false
}

pub fn check_two_pair(hand: &PokerHand, scoring_cards: &Vec<&Card>) -> bool {
    if matches!(
        hand,
        PokerHand::TwoPair | PokerHand::FullHouse | PokerHand::FlushHouse
    ) {
        return true;
    }
    let mut counts = [0; 13];
    for card in scoring_cards {
        if card.enhancement != Some(ortalib::Enhancement::Stone) {
            counts[rank_to_index(&card.rank)] += 1;
        }
    }
    let mut pair_count = 0;
    for &count in counts.iter() {
        if count >= 2 {
            pair_count += 1;
        }
    }
    pair_count >= 2
}

pub fn check_straight(hand: &PokerHand, cards_played: &[Card], active_jokers: &[Joker]) -> bool {
    if matches!(hand, PokerHand::Straight | PokerHand::StraightFlush) {
        return true;
    }
    let is_four_fingers = active_jokers
        .iter()
        .any(|j| matches!(j, Joker::FourFingers));
    let is_shortcut = active_jokers.iter().any(|j| matches!(j, Joker::Shortcut));
    card_handler::get_straight_cards(cards_played, is_four_fingers, is_shortcut).is_some()
}

pub fn check_flush(
    hand: &PokerHand,
    cards_played: &[Card],
    active_jokers: &[Joker],
    smeared: bool,
) -> bool {
    if matches!(
        hand,
        PokerHand::Flush | PokerHand::StraightFlush | PokerHand::FlushHouse | PokerHand::FlushFive
    ) {
        return true;
    }
    let is_four_fingers = active_jokers
        .iter()
        .any(|j| matches!(j, Joker::FourFingers));
    let flush_target = if is_four_fingers { 4 } else { 5 };
    card_handler::get_flush_cards(cards_played, flush_target, &smeared).is_some()
}

// helper for flower pot
fn has_unique_suit_coverage(scoring_cards: &[&Card], smeared: bool) -> bool {
    if scoring_cards.len() < 4 {
        return false;
    }

    let target_suits = [Suit::Spades, Suit::Hearts, Suit::Clubs, Suit::Diamonds];
    let mut used_indices = vec![false; scoring_cards.len()];

    fn match_suits(
        suit_id: usize,
        target_suits: &[Suit; 4],
        scoring_cards: &[&Card],
        used_indices: &mut [bool],
        smeared: bool,
    ) -> bool {
        if suit_id == 4 {
            return true;
        }

        let current_suit = &target_suits[suit_id];

        for (i, card) in scoring_cards.iter().enumerate() {
            if !used_indices[i] && card_handler::matches_suit(card, current_suit, &smeared) {
                used_indices[i] = true;
                if match_suits(
                    suit_id + 1,
                    target_suits,
                    scoring_cards,
                    used_indices,
                    smeared,
                ) {
                    return true;
                }
                used_indices[i] = false; // Backtrack
            }
        }

        false
    }

    match_suits(0, &target_suits, scoring_cards, &mut used_indices, smeared)
}

pub fn is_blueprint_compatible(joker: &Joker) -> bool {
    // Blueprint cannot copy "passive modifier" (i.e. N/A) effects.
    // These are the modifiers currently tracked as booleans in your game state.
    !matches!(
        joker,
        Joker::Pareidolia | Joker::SmearedJoker | Joker::Splash
    )
}

pub fn resolve_blueprints(base_jokers: &[Joker]) -> Vec<Joker> {
    let mut resolved = Vec::new();
    let mut current_target: Option<Joker> = None;

    // iterate right-to-left to natively solve nested Blueprint chains
    for joker in base_jokers.iter().rev() {
        match joker {
            Joker::Blueprint => {
                if let Some(target) = current_target {
                    resolved.push(target);
                } else {
                    resolved.push(*joker);
                }
            }
            _ => {
                if is_blueprint_compatible(joker) {
                    current_target = Some(*joker); 
                } else {
                    current_target = None;
                }
                resolved.push(*joker);
            }
        }
    }

    resolved.reverse();
    resolved
}
