use std::{
    error::Error,
    fs::File,
    io::{Read, stdin},
    net::SocketAddr,
    path::{Path, PathBuf},
};

use axum::{
    Json, Router,
    http::StatusCode,
    routing::{get, post},
};
use serde_json::Value;
use std::env;
use tower_http::cors::{Any, CorsLayer};
use tokio::signal;

use clap::Parser;
use ortalib::{Chips, Enhancement, Joker, Mult, Round};
mod card_handler;
mod joker_handler;
mod scores;

#[derive(Parser)]
struct Opts {
    /// Path to the YAML file to score. Use "-" for stdin if using --serve.
    #[arg(default_value = "")]
    file: PathBuf,

    /// Print explanations during scoring
    #[arg(long)]
    explain: bool,

    /// Start the backend HTTP server
    #[arg(long)]
    serve: bool,
}


// --- M A I N ---
#[tokio::main]
async fn main() -> Result<(), Box<dyn Error>> {
    let opts = Opts::parse();

    // --- MODE 1: API Server ---
    if opts.serve {
        // allow any origin, method, and header for local development
        let cors = CorsLayer::new()
            .allow_origin(Any)
            .allow_methods(Any)
            .allow_headers(Any);

        let app = Router::new()
            .route("/", get(healthcheck_handler)) // healthcheck route
            .route("/score", post(score_handler)) // actual scoring route
            .layer(cors);

        let port: u16 = env::var("PORT")
            .unwrap_or_else(|_| "6991".to_string())
            .parse()
            .expect("PORT must be a valid number");

        // let addr = SocketAddr::from(([127, 0, 0, 1], port));  // localhost only
        let addr = SocketAddr::from(([0, 0, 0, 0], port)); // docker + deployment provider
        let listener = tokio::net::TcpListener::bind(addr).await?;

        println!("🚀 Server running publically on port {}", port);
        axum::serve(listener, app)
            .with_graceful_shutdown(shutdown_signal())
            .await?;

        return Ok(());
    }

    // --- MODE 2: CLI Tool ---
    // Manually ensure the user provided a file if they aren't starting the server
    if opts.file.as_os_str().is_empty() {
        eprintln!("Error: A <FILE> argument is required unless --serve is passed.");
        std::process::exit(1);
    }

    let round = parse_round(&opts)?;
    let (chips, mult) = score(round, opts.explain);

    println!("{}", (chips * mult).floor());
    Ok(())
}


// --- Graceful Shutdown Handler ---
async fn shutdown_signal() {
    let ctrl_c = async {
        signal::ctrl_c()
            .await
            .expect("Failed to install Ctrl+C handler");
    };

    #[cfg(unix)]
    let terminate = async {
        signal::unix::signal(signal::unix::SignalKind::terminate())
            .expect("Failed to install signal handler")
            .recv()
            .await;
    };

    #[cfg(not(unix))]
    let terminate = std::future::pending::<()>();

    tokio::select! {
        _ = ctrl_c => {},
        _ = terminate => {},
    }
    
    println!("\n🛑 Received shutdown signal. Stopping server gracefully...");
}


// --- Axum Route Handlers ---
async fn healthcheck_handler() -> &'static str {
    "Server is healthy and ready to score!"
}

async fn score_handler(Json(raw_payload): Json<Value>) -> Result<Json<i64>, StatusCode> {
    let payload = match parse_round_json(raw_payload) {
        Ok(parsed_round) => parsed_round,
        Err(e) => {
            eprintln!("❌ Payload Parsing Error: {}", e);
            return Err(StatusCode::UNPROCESSABLE_ENTITY); // returns status code 422
        }
    };

    let (chips, mult) = score(payload, false);
    let final_score = (chips * mult).floor() as i64;

    Ok(Json(final_score))
}


// --- Helpers ---
fn parse_round_json(payload: Value) -> Result<Round, Box<dyn Error>> {
    let round: Round = serde_json::from_value(payload)?;

    Ok(round)
}

fn parse_round(opts: &Opts) -> Result<Round, Box<dyn Error>> {
    let mut input = String::new();
    if opts.file == Path::new("-") {
        stdin().read_to_string(&mut input)?;
    } else {
        File::open(&opts.file)?.read_to_string(&mut input)?;
    }

    let round = serde_yaml::from_str(&input)?;
    Ok(round)
}


// --- Main scoring function ---
fn score(round: Round, explain: bool) -> (Chips, Mult) {
    let base_jokers: Vec<Joker> = round.jokers.iter().map(|jc| jc.joker).collect();
    let active_jokers = joker_handler::resolve_blueprints(&base_jokers);

    let eval = match card_handler::evaluate_poker_hand(&round.cards_played, &active_jokers) {
        Ok(evaluated_hand) => evaluated_hand,
        Err(_) => return (Chips::from(0.0), Mult::from(0.0)),
    };

    let mut state = scores::ScoreState {
        chips: eval.base_chips,
        mult: eval.base_mult,
        explain,
    };

    scores::log_step(
        &state,
        card_handler::format_hand_name(&eval.hand_type),
    );

    // --- Process Game-Changing Jokers ---
    let pareidolia_active = active_jokers.iter().any(|j| matches!(j, Joker::Pareidolia));
    let smeared_active = active_jokers
        .iter()
        .any(|j| matches!(j, Joker::SmearedJoker));
    let splash_active = active_jokers.iter().any(|j| matches!(j, Joker::Splash));
    let raised_fist_target = card_handler::get_raised_fist_target(&round.cards_held_in_hand);
    let mut is_first_scoring_card = true;

    let first_scoring_face = round.cards_played.iter().find(|card| {
        let is_in_combo = eval.scoring_cards.iter().any(|&c| std::ptr::eq(c, *card));
        is_in_combo && card_handler::is_face(card, &pareidolia_active)
    });

    // --- Score Played Cards ---
    for card in &round.cards_played {
        let is_in_combo = eval.scoring_cards.iter().any(|&c| std::ptr::eq(c, card));
        let is_stone = card.enhancement == Some(Enhancement::Stone);

        if !is_in_combo && !is_stone && !splash_active {
            continue; // Skip non-scoring cards
        }

        let mut apply_chad = false;
        if is_first_scoring_card {
            apply_chad = true;
            is_first_scoring_card = false; // Consume the flag so subsequent cards don't get it
        }

        let retriggers = joker_handler::get_scoring_card_retriggers(
            card,
            apply_chad,
            &active_jokers,
            &pareidolia_active,
        );

        let total_triggers = 1 + retriggers;
        for i in 0..total_triggers {
            if i != 0 && explain {
                println!("<> Retrigger this card");
            }

            let is_first_face = first_scoring_face.is_some_and(|f| std::ptr::eq(card, f));
            scores::score_played_card(
                card,
                &active_jokers,
                &pareidolia_active,
                &smeared_active,
                &is_first_face,
                &mut state,
            );
        }
    }

    // --- Score Cards Held in Hand ---
    for card in &round.cards_held_in_hand {
        let retriggers = joker_handler::get_held_card_retriggers(&active_jokers);

        let total_triggers = 1 + retriggers;
        for _ in 0..total_triggers {
            scores::score_held_card(card, &active_jokers, raised_fist_target, &mut state);
        }
    }

    // --- Evaluate Blackboard Joker ---
    if card_handler::is_all_spades_or_clubs(&round.cards_held_in_hand) {
        let blackboard_count = active_jokers
            .iter()
            .filter(|j| matches!(j, Joker::Blackboard))
            .count();
        for _ in 0..blackboard_count {
            state.mult *= 3.0;
            if state.explain {
                println!("Blackboard x3.0 Mult ({} x {})", state.chips, state.mult);
            }
        }
    }

    // --- Score Jokers ---
    let joker_context = joker_handler::JokerContext {
        played_hand: &eval.hand_type,
        scoring_cards: &eval.scoring_cards,
        total_jokers: active_jokers.len(),
        smeared_active,
        cards_played: &round.cards_played,
        active_jokers: &active_jokers,
    };

    for (joker_card, resolved_joker) in round.jokers.iter().zip(active_jokers.iter()) {
        scores::score_joker(
            resolved_joker,              // copied joker ability
            joker_card.edition.as_ref(), // original blueprint's edition
            &joker_context,
            &mut state,
        );
    }

    (Chips::from(state.chips), Mult::from(state.mult))
}

// fn score(round: Round) -> (Chips, Mult) {
//     todo!()
// }
