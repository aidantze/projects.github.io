use ortalib::{Card, Edition, Enhancement, Joker, PokerHand, Rank, Suit};
use std::collections::HashMap;

#[derive(Debug)]
pub struct EvaluatedHand<'a> {
    pub hand_type: PokerHand,
    pub base_chips: f64,
    pub base_mult: f64,
    pub scoring_cards: Vec<&'a Card>,
}

#[derive(Debug)]
pub struct EvaluatedModifiers {
    pub base_chips: f64,
    pub base_mult: f64,
    pub multiply_mult: bool,
}

// ============================================================================
// SCORE POKER HAND, AND OTHER CLI RELATED HELPERS
// ============================================================================

pub fn evaluate_poker_hand<'a>(
    cards: &'a [Card],
    jokers: &[Joker]
) -> Result<EvaluatedHand<'a>, &'static str> {
    if cards.is_empty() {
        return Err("Cannot evaluate an empty hand.");
    }

    let is_four_fingers = jokers.iter().any(|j| matches!(j, Joker::FourFingers));
    let is_shortcut = jokers.iter().any(|j| matches!(j, Joker::Shortcut));
    let is_smeared = jokers.iter().any(|j| matches!(j, Joker::SmearedJoker));
    let is_splash = jokers.iter().any(|j| matches!(j, Joker::Splash));

    // count ranks and suits for straight and flush, ignoring stone cards
    let mut rank_counts: HashMap<&Rank, usize> = HashMap::new();
    for card in cards {
        if card.enhancement != Some(Enhancement::Stone) {
            *rank_counts.entry(&card.rank).or_insert(0) += 1;
        }
    }

    let mut freqs: Vec<usize> = rank_counts.values().copied().collect();
    freqs.sort_unstable_by(|a, b| b.cmp(a));

    let flush_target = if is_four_fingers { 4 } else { 5 };
    let flush_result = get_flush_cards(cards, flush_target, &is_smeared);
    let straight_cards_opt = get_straight_cards(cards, is_four_fingers, is_shortcut);

    // get all non-stone cards and collect non-stone cards matching min rank frequency
    let all_non_stone_cards = || -> Vec<&'a Card> {
        let mut out = Vec::new();
        for c in cards {
            if c.enhancement != Some(Enhancement::Stone) {
                out.push(c);
            }
        }
        out
    };

    let filter_by_min_rank_count = |min_count: usize| -> Vec<&'a Card> {
        let mut out = Vec::new();
        for c in cards {
            if c.enhancement != Some(Enhancement::Stone) {
                match rank_counts.get(&c.rank) {
                    Some(&count) if count >= min_count => out.push(c),
                    _ => {}
                }
            }
        }
        out
    };

    // top down pattern match, ignoring stone cards
    let (hand_type, mut scoring_cards) = if let Some(_) = flush_result
        && freqs.first() == Some(&5)
    {
        (PokerHand::FlushFive, all_non_stone_cards())           // Flush Five
    } else if let Some(_) = flush_result
        && freqs.first() == Some(&3)
        && freqs.get(1) >= Some(&2)
    {
        (PokerHand::FlushHouse, all_non_stone_cards())          // Flush House
    } else if freqs.first() == Some(&5) {
    (PokerHand::FiveOfAKind, all_non_stone_cards())             // 5 of a kind
    } else if let Some((_, ref flush_cards)) = flush_result
        && let Some(ref straight_cards) = straight_cards_opt
    {
        let mut sf_scoring = straight_cards.clone();
        for &fc in flush_cards {
            if !sf_scoring.iter().any(|&sc| std::ptr::eq(sc, fc)) {
                sf_scoring.push(fc);
            }
        }
        (PokerHand::StraightFlush, sf_scoring)                  // Straight Flush
    } else if freqs.first() == Some(&4) {
        (PokerHand::FourOfAKind, filter_by_min_rank_count(4))   // 4 of a kind
    } else if freqs.first() == Some(&3) && freqs.get(1) >= Some(&2) {
        (PokerHand::FullHouse, all_non_stone_cards())           // Full House
    } else if let Some((_, ref flush_cards)) = flush_result {
        (PokerHand::Flush, flush_cards.clone())                 // Flush
    } else if let Some(straight_cards) = straight_cards_opt {
        (PokerHand::Straight, straight_cards.clone())           // Straight
    } else if freqs.first() == Some(&3) {
        (PokerHand::ThreeOfAKind, filter_by_min_rank_count(3))  // 3 of a kind
    } else if freqs.first() == Some(&2) && freqs.get(1) == Some(&2) {
        (PokerHand::TwoPair, filter_by_min_rank_count(2))       // 2 pair
    } else if freqs.first() == Some(&2) {
        (PokerHand::Pair, filter_by_min_rank_count(2))          // 2 of a kind
    } else {
        let mut scoring = Vec::new();
        if let Some(highest) = highest_card(cards) {
            scoring.push(highest);
        }
        (PokerHand::HighCard, scoring)                          // High Card
    };

    if is_splash {
        scoring_cards = Vec::new();
        for c in cards {
            scoring_cards.push(c);
        }
    }

    let (chips_obj, mult_obj) = hand_type.hand_value();

    Ok(EvaluatedHand {
        hand_type,
        base_chips: chips_obj,
        base_mult: mult_obj,
        scoring_cards,
    })
}

pub fn evaluate_card_enhancement(card: &Card, is_in_hand: bool) -> EvaluatedModifiers {
    if let Some(ref enhancement_ref) = card.enhancement {
        match enhancement_ref {
            Enhancement::Bonus => EvaluatedModifiers {
                base_chips: 30.0,
                base_mult: 0.0,
                multiply_mult: false,
            },
            Enhancement::Mult => EvaluatedModifiers {
                base_chips: 0.0,
                base_mult: 4.0,
                multiply_mult: false,
            },
            Enhancement::Wild => EvaluatedModifiers {
                base_chips: 0.0,
                base_mult: 0.0,
                multiply_mult: false,
            },
            Enhancement::Glass => EvaluatedModifiers {
                base_chips: 0.0,
                base_mult: 2.0,
                multiply_mult: true,
            },
            Enhancement::Steel => {
                if is_in_hand {
                    EvaluatedModifiers {
                        base_chips: 0.0,
                        base_mult: 1.5,
                        multiply_mult: true,
                    }
                } else {
                    EvaluatedModifiers {
                        base_chips: 0.0,
                        base_mult: 0.0,
                        multiply_mult: false,
                    }
                }
            }
            Enhancement::Stone => EvaluatedModifiers {
                base_chips: 50.0,
                base_mult: 0.0,
                multiply_mult: false,
            },
        }
    } else {
        EvaluatedModifiers {
            base_chips: 0.0,
            base_mult: 0.0,
            multiply_mult: false,
        }
    }
}

pub fn evaluate_card_edition(card: &Card) -> EvaluatedModifiers {
    if let Some(ref edition_ref) = card.edition {
        match edition_ref {
            Edition::Foil => EvaluatedModifiers {
                base_chips: 50.0,
                base_mult: 0.0,
                multiply_mult: false,
            },
            Edition::Holographic => EvaluatedModifiers {
                base_chips: 0.0,
                base_mult: 10.0,
                multiply_mult: false,
            },
            Edition::Polychrome => EvaluatedModifiers {
                base_chips: 0.0,
                base_mult: 1.5,
                multiply_mult: true,
            },
        }
    } else {
        EvaluatedModifiers {
            base_chips: 0.0,
            base_mult: 0.0,
            multiply_mult: false,
        }
    }
}

pub fn format_hand_name(hand: &PokerHand) -> &'static str {
    match hand {
        PokerHand::FlushFive => "Flush Five",
        PokerHand::FlushHouse => "Flush House",
        PokerHand::FiveOfAKind => "Five Of A Kind",
        PokerHand::StraightFlush => "Straight Flush",
        PokerHand::FourOfAKind => "Four Of A Kind",
        PokerHand::FullHouse => "Full House",
        PokerHand::Flush => "Flush",
        PokerHand::Straight => "Straight",
        PokerHand::ThreeOfAKind => "Three Of A Kind",
        PokerHand::TwoPair => "Two Pair",
        PokerHand::Pair => "Pair",
        PokerHand::HighCard => "High Card",
    }
}

pub fn format_card(card: &Card) -> String {
    let rank_str = match card.rank {
        Rank::Two => "2",
        Rank::Three => "3",
        Rank::Four => "4",
        Rank::Five => "5",
        Rank::Six => "6",
        Rank::Seven => "7",
        Rank::Eight => "8",
        Rank::Nine => "9",
        Rank::Ten => "10",
        Rank::Jack => "J",
        Rank::Queen => "Q",
        Rank::King => "K",
        Rank::Ace => "A",
    };
    let suit_str = match card.suit {
        Suit::Hearts => "♥",
        Suit::Spades => "♠",
        Suit::Diamonds => "♦",
        Suit::Clubs => "♣",
    };
    format!("{}{}", rank_str, suit_str)
}

// ============================================================================
// PUBLIC CARD INSPECTION HELPERS
// ============================================================================

pub fn highest_card(cards: &[Card]) -> Option<&Card> {
    cards
        .iter()
        .filter(|c| c.enhancement != Some(Enhancement::Stone))
        .max_by_key(|c| get_rank_ordinals(&c.rank))
}

/// get Raised Fist target card, which is of lowest rank. If tie, returns right most
pub fn get_raised_fist_target(held_cards: &[Card]) -> Option<&Card> {
    let min_ordinal = held_cards
        .iter()
        .filter(|c| c.enhancement != Some(Enhancement::Stone))
        .map(|c| get_rank_ordinals(&c.rank))
        .min()?;

    held_cards.iter().rev().find(|c| {
        c.enhancement != Some(Enhancement::Stone) && get_rank_ordinals(&c.rank) == min_ordinal
    })
}

pub fn is_all_spades_or_clubs(held_cards: &[Card]) -> bool {
    held_cards.iter().all(|c| {
        c.enhancement == Some(Enhancement::Wild) ||
        matches!(c.suit, Suit::Spades | Suit::Clubs)
    })
}

pub fn is_fibonacci_target(card: &Card) -> bool {
    card.enhancement != Some(Enhancement::Stone)
        && matches!(get_rank_ordinals(&card.rank), 14 | 2 | 3 | 5 | 8)
}

pub fn is_even(card: &Card) -> bool {
    card.enhancement != Some(Enhancement::Stone)
        && matches!(get_rank_ordinals(&card.rank), 2 | 4 | 6 | 8 | 10)
}

pub fn is_odd(card: &Card) -> bool {
    card.enhancement != Some(Enhancement::Stone)
        && matches!(get_rank_ordinals(&card.rank), 14 | 3 | 5 | 7 | 9)
}

pub fn is_walkie_talkie_target(card: &Card) -> bool {
    card.enhancement != Some(Enhancement::Stone) && matches!(get_rank_ordinals(&card.rank), 10 | 4)
}

pub fn is_hack_target(card: &Card) -> bool {
    card.enhancement != Some(Enhancement::Stone) && matches!(get_rank_ordinals(&card.rank), 2..=5)
}

// handles Pareidolia
pub fn is_face(card: &Card, is_pareidolia: &bool) -> bool {
    card.enhancement != Some(Enhancement::Stone) && (*is_pareidolia || card.rank.is_face())
}

// ============================================================================
// PRIVATE INTERNAL LOGIC
// ============================================================================

pub fn get_rank_ordinals(rank: &Rank) -> u8 {
    match rank {
        Rank::Two => 2,
        Rank::Three => 3,
        Rank::Four => 4,
        Rank::Five => 5,
        Rank::Six => 6,
        Rank::Seven => 7,
        Rank::Eight => 8,
        Rank::Nine => 9,
        Rank::Ten => 10,
        Rank::Jack => 11,
        Rank::Queen => 12,
        Rank::King => 13,
        Rank::Ace => 14,
    }
}

pub fn card_rank_value(rank: &Rank) -> u8 {
    match rank {
        Rank::Two => 2,
        Rank::Three => 3,
        Rank::Four => 4,
        Rank::Five => 5,
        Rank::Six => 6,
        Rank::Seven => 7,
        Rank::Eight => 8,
        Rank::Nine => 9,
        Rank::Ten | Rank::Jack | Rank::Queen | Rank::King => 10,
        Rank::Ace => 11,
    }
}

pub fn matches_suit(card: &Card, target_suit: &Suit, is_smeared: &bool) -> bool {
    if card.enhancement == Some(Enhancement::Wild) {
        return true;
    }
    if *is_smeared {
        card.suit.color() == target_suit.color()
    } else {
        card.suit == *target_suit
    }
}

pub fn get_flush_cards<'a>(
    cards: &'a [Card],
    flush_target: usize,
    is_smeared: &bool,
) -> Option<(Suit, Vec<&'a Card>)> {
    let core_suits = vec![Suit::Hearts, Suit::Spades, Suit::Diamonds, Suit::Clubs];

    for target_suit in core_suits {
        let flush_cards: Vec<&'a Card> = cards
            .iter()
            .filter(|c| {
                c.enhancement != Some(Enhancement::Stone)
                    && matches_suit(c, &target_suit, is_smeared)
            })
            .collect();

        if flush_cards.len() >= flush_target {
            return Some((target_suit, flush_cards));
        }
    }
    None
}

pub fn get_straight_cards<'a>(
    cards: &'a [Card],
    is_four_fingers: bool,
    is_shortcut: bool,
) -> Option<Vec<&'a Card>> {
    let straight_target = if is_four_fingers { 4 } else { 5 };
    if cards.len() < straight_target {
        return None;
    }

    let mut ordinals: Vec<u8> = cards
        .iter()
        .filter(|c| c.enhancement != Some(Enhancement::Stone))
        .map(|c| get_rank_ordinals(&c.rank))
        .collect();

    if ordinals.contains(&14) {
        ordinals.push(1);
    }

    ordinals.sort_unstable();
    ordinals.dedup();

    if ordinals.len() < straight_target {
        return None;
    }
    let max_gap = if is_shortcut { 2 } else { 1 };

    for target_len in (straight_target..=ordinals.len()).rev() {
        if let Some(winning_window) = ordinals.windows(target_len).find(|win| {
            win.windows(2).all(|pair| {
                let step = pair[1] - pair[0];
                step >= 1 && step <= max_gap
            })
        }) {
            let scoring_cards: Vec<&'a Card> = cards
                .iter()
                .filter(|c| {
                    if c.enhancement == Some(Enhancement::Stone) {
                        return false;
                    }
                    let ord = get_rank_ordinals(&c.rank);
                    winning_window.contains(&ord) || (ord == 14 && winning_window.contains(&1))
                })
                .collect();
            
            return Some(scoring_cards);
        }
    }

    None
}
