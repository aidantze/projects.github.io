"""
z5360925
FINS3645 FinTech Project Guidelines
Cryptocurrency Investment Product
------------------------------------------------------------------------------

TILT.ai Crypto data pipeline – Stage 3 and Stage 4.

Stage 3 : 
    - Apply VADER model to determine sentiment score for each article
    - Perform sentiment indexing
    - Combine sentiment scores with OHLCV features
    - Perform regression/analysis to predict optimal portfolio weights
Stage 4 :  
    - ???
    
All timestamps are in UTC for consistency across datasets
"""
from __future__ import annotations
import logging, time
from pathlib import Path
from typing import Dict

from datetime import timedelta
import time

import numpy as np
import pandas as pd
import requests
import random

from tqdm import tqdm
import matplotlib.pyplot as plt
import matplotlib.dates as mdates
import seaborn as sns
from nltk.sentiment.vader import SentimentIntensityAnalyzer
from sklearn.metrics import confusion_matrix, classification_report
from sklearn.ensemble import GradientBoostingRegressor
from sklearn.linear_model import LinearRegression, Ridge
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error
import re

#### Our custom dictionary ####
from vader_custom_lexicon import custom_words    


# ----------------------------------------------------------------- logging
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
tqdm.pandas()

# ----------------------------------------------------------------- folders
def _ensure_dir(root: Path, sub: str | Path) -> Path:
    p = root / sub
    p.mkdir(parents=True, exist_ok=True)
    return p

def build_week_dirs(
    base_folder: str | Path = "data",
    results_folder: str = "results",
    data_sub: str = "clean_data",
) -> Path:
    """Return *data_dir* inside <base_folder>/results/clean_data/"""
    project_root = Path.cwd().resolve()
    base_root = project_root if project_root.name == str(base_folder) else _ensure_dir(
        project_root, base_folder
    )
    results_root = _ensure_dir(base_root, results_folder)
    data_dir = _ensure_dir(results_root, data_sub)
    return data_dir


def build_fig_dirs(
    base_folder: str | Path = "data",
    results_folder: str = "results",
    data_sub: str = "figures",
) -> Path:
    """Return *fig_dir* inside <base_folder>/results/figures/"""
    project_root = Path.cwd().resolve()
    base_root = project_root if project_root.name == str(base_folder) else _ensure_dir(
        project_root, base_folder
    )
    results_root = _ensure_dir(base_root, results_folder)
    fig_dir = _ensure_dir(results_root, data_sub)
    return fig_dir


# --- imports ---------------------------------------------------------------
tqdm.pandas()
SEED = 123
random.seed(SEED)
np.random.seed(SEED)


# ---------------------------------------------------------------------------
# API helpers ----------------------------------------------------------------

API_URL = (
    "https://api-inference.huggingface.co/models/"
    "mrm8488/distilroberta-finetuned-financial-news-sentiment-analysis"
)

def _headers(api_key: str) -> dict[str, str]:
    return {"Authorization": f"Bearer {api_key}"}


def create_polar_gauge(score: float, title: str, filename: Path):
    """
    Creates and saves a polar fear-greed gauge chart.
    """
    # Scale score from [-1, 1] to [0, 100]
    scaled_score = (score + 1) * 50

    # Determine status category and color
    if scaled_score <= 25:
        status, color = "Extreme Fear", "#8B0000"
    elif scaled_score <= 45:
        status, color = "Fear", "#FF4500"
    elif scaled_score <= 55:
        status, color = "Neutral", "#FFD700"
    elif scaled_score <= 75:
        status, color = "Greed", "#90EE90"
    else:
        status, color = "Extreme Greed", "#006400"

    fig, ax = plt.subplots(figsize=(8, 8), subplot_kw=dict(projection="polar"))
    
    colors = ["#8B0000", "#FF4500", "#FFD700", "#90EE90", "#006400"]
    bounds = [0, 20, 40, 60, 80, 100]

    for i in range(len(colors)):
        t0, t1 = np.pi * (bounds[i] / 100), np.pi * (bounds[i + 1] / 100)
        ax.fill_between(np.linspace(t0, t1, 20), 0.5, 1, color=colors[i], alpha=0.9)

    for sc in [0, 25, 50, 75, 100]:
        angle = np.pi * (sc / 100)
        ax.plot([angle, angle], [0.5, 0.55], "k-", lw=1)
    
    needle_angle = np.pi * (scaled_score / 100)
    ax.plot([needle_angle, needle_angle], [0, 0.9], "k-", lw=4)
    ax.plot(needle_angle, 0, "ko", ms=14)

    ax.text(np.pi / 2, 0.2, f"{score:.2f}", ha="center", va="center", fontsize=60, weight="bold")
    ax.text(np.pi / 2, 1.2, f"Current Status: {status}", ha="center", va="center", fontsize=18, weight="bold", color=color)

    ax.set_title(title, fontsize=20, y=1.08)
    ax.set_ylim(0, 1.1)
    ax.set_xlim(0, np.pi)
    ax.set_theta_zero_location("W")
    ax.set_theta_direction(1)
    ax.grid(False)
    ax.set_rticks([])
    ax.set_thetagrids([])
    
    plt.tight_layout()
    plt.savefig(filename, dpi=150)
    plt.close()


# ----------------------------------------------------------------------------
# Stage 3a – Sentiment Scoring -----------------------------------------------
_VADER = SentimentIntensityAnalyzer()
_VADER.lexicon.update(custom_words)
    

# Hugging Face API for financial news sentiment analysis
def hf_score(
    text: str,
    api_key: str,
    max_words: int = 400,
) -> pd.Series:
    """
    Splits text into ≤400-word chunks, scores each with HF API, and averages the scores.
    
    HF API → pos / neg / neu
    """
    
    if not isinstance(text, str) or not text.strip():
        return pd.Series([np.nan, np.nan, np.nan], index=["pos_hf", "neg_hf", "neu_hf"])

    words = text.split()
    chunks = [' '.join(words[i:i + max_words]) for i in range(0, len(words), max_words)]

    scores = []
    for chunk in chunks:
        try:
            r = requests.post(API_URL, headers=_headers(api_key), json={"inputs": chunk}, timeout=10)
            r.raise_for_status()
            result = {d["label"].lower(): d["score"] for d in r.json()[0]}
            scores.append([
                result.get("positive", 0),
                result.get("negative", 0),
                result.get("neutral", 0),
            ])
        except (requests.exceptions.RequestException, KeyError, ValueError) as e:
            logging.warning(f"HuggingFace API error: {e}")
            logging.info("HF API failed to get sentiment scores. Resorting to VADER")
            continue

    if not scores:
        return pd.Series([np.nan, np.nan, np.nan], index=["pos_hf", "neg_hf", "neu_hf"])

    scores_arr = np.array(scores)
    mean_scores = scores_arr.mean(axis=0)
    return pd.Series(mean_scores, index=["pos_hf", "neg_hf", "neu_hf"])


# --- VADER setup ------------------------------------------------------------
def vader_score(text: str) -> pd.Series:
    "VADER → pos / neg / neu / comp"
    if not isinstance(text, str):
        return pd.Series([0, 0, 0, 0], index=["pos_vr", "neg_vr", "neu_vr", "comp_vr"])
    s = _VADER.polarity_scores(text)
    return pd.Series([s["pos"], s["neg"], s["neu"], s["compound"]],
                     index=["pos_vr", "neg_vr", "neu_vr", "comp_vr"])


def compound_score_from_probs(pos, neg, neu):
    return (pos - neg) / (pos + neg + neu + 1e-6)


def stage_3_sentiment_scores(
    df: pd.DataFrame,
    api_key: str,
    data_dir: Path,
    filename: str = "stage_3_news_sentiment.csv",
    use_hf_api: bool = True,
) -> pd.DataFrame:
    """
    Applies both VADER and Hugging Face sentiment scoring to cleaned news articles,
    returning a unified dataframe with consistent sentiment score columns.
    
    Returns:
        df_sentiment: DataFrame with pos/neg/neu/compound scores from both sources,
                      as well as a combined average compound score.
    """
    tic = time.time()
    logging.info("Stage 3 – Calculcating sentiment scores...")
    
    # Filter suitable length for HuggingFace API model (e.g., 200–250 words)
    df_sentiment = (
        # df.loc[(df["n_words"] > 200) & (df["n_words"] < 250)]
        df.loc[(df["n_words"] > 200)]
        # .sample(n=10, random_state=SEED) # comment this out if required
        .copy()
    )
    # df_sentiment = df.copy()
    
    # Apply VADER sentiment scores
    vader_scores = df_sentiment["reviewText"].progress_apply(vader_score)
    if vader_scores.isnull().any().any():
        raise ValueError("VADER scoring failed for one or more articles.")
    
    df_sentiment[["pos_vr", "neg_vr", "neu_vr", "comp_vr"]] = vader_scores

    # Apply Hugging Face sentiment scores
    # if API fails, will resort to only use VADER analysis
    if use_hf_api:
        logging.info("Calling Hugging Face API for sentiment analysis...")
        try:
            df_sentiment[["pos_hf", "neg_hf", "neu_hf"]] = df_sentiment["reviewText"].progress_apply(
                hf_score, api_key=api_key
            )

            df_sentiment["comp_hf"] = compound_score_from_probs(
                df_sentiment["pos_hf"].astype(float),
                df_sentiment["neg_hf"].astype(float),
                df_sentiment["neu_hf"].astype(float),
            )
            
            df_sentiment["pos_avg"] = df_sentiment.apply(
                lambda row: np.mean([row["pos_vr"], row["pos_hf"]])
                if not pd.isna(row["pos_hf"])
                else row["pos_vr"],
                axis=1
            )
            df_sentiment["neg_avg"] = df_sentiment.apply(
                lambda row: np.mean([row["neg_vr"], row["neg_hf"]])
                if not pd.isna(row["neg_hf"])
                else row["neg_vr"],
                axis=1
            )
            df_sentiment["neu_avg"] = df_sentiment.apply(
                lambda row: np.mean([row["neu_vr"], row["neu_hf"]])
                if not pd.isna(row["neu_hf"])
                else row["neu_vr"],
                axis=1
            )
            df_sentiment["comp_avg"] = df_sentiment.apply(
                lambda row: np.mean([row["comp_vr"], row["comp_hf"]])
                if not pd.isna(row["comp_hf"])
                else row["comp_vr"],
                axis=1
            )

        except Exception as e:
            logging.warning("Hugging Face API failed: %s. Falling back to VADER only.", e)
            df_sentiment["comp_avg"] = df_sentiment["comp_vr"]
            
    else:
        logging.info("Skipping Hugging Face API – using only VADER sentiment.")
        df_sentiment["pos_avg"] = df_sentiment["pos_vr"]
        df_sentiment["neg_avg"] = df_sentiment["neg_vr"]
        df_sentiment["neu_avg"] = df_sentiment["neu_vr"]
        df_sentiment["comp_avg"] = df_sentiment["comp_vr"]
    
    df_sentiment = df_sentiment[pd.to_numeric(df_sentiment["pos_avg"], errors="coerce").notnull()]
    df_sentiment = df_sentiment[pd.to_numeric(df_sentiment["neg_avg"], errors="coerce").notnull()]
    df_sentiment = df_sentiment[pd.to_numeric(df_sentiment["neu_avg"], errors="coerce").notnull()]
    df_sentiment = df_sentiment[pd.to_numeric(df_sentiment["comp_avg"], errors="coerce").notnull()]
    
    # sentiment indexing
    df_sentiment_index = df_sentiment.copy()
    
    out_path = data_dir / filename
    df_sentiment_index.to_csv(out_path, index=False)
    logging.info("Saved cleaned news -> %s (%.2f s)", out_path.name, time.time() - tic)

    return df_sentiment_index


def merge_sentiment_ohlcv(
    df_ohlcv: pd.DataFrame,
    df_sentiment: pd.DataFrame,
    data_dir: Path,
    filename: str = "data/results/stage_3_merged.csv",
    fill_method: str = "zero",  # Options: "zero", "ffill", or "none"
) -> pd.DataFrame:
    tic = time.time()
    logging.info("Stage 3 – Merging scores on ohlcv data...")
    
    # Standardize symbol casing
    df_ohlcv["symbol"] = df_ohlcv["symbol"].str.upper()
    df_sentiment["symbol"] = df_sentiment["symbol"].str.upper()
    
    # Separate market sentiment from coin-specific sentiment
    market_sentiment_df = df_sentiment[df_sentiment['symbol'] == 'MARKET'].copy()
    coin_sentiment_df = df_sentiment[df_sentiment['symbol'] != 'MARKET'].copy()
    
    # Aggregate and save market sentiment to its own file
    if not market_sentiment_df.empty:
        market_agg = (
            market_sentiment_df.groupby("date")
            .agg(market_sentiment_index=("comp_avg", "mean"))
            .reset_index()
        )
        market_sentiment_path = data_dir / "market_sentiment.csv"
        market_agg.to_csv(market_sentiment_path, index=False)
        logging.info("Saved daily market sentiment -> %s", market_sentiment_path.name)
    
    news_agg = (
        df_sentiment.groupby(["symbol", "date"])
        .agg({
            "pos_avg": "mean",
            "neg_avg": "mean",
            "neu_avg": "mean",
            "comp_avg": "mean"
        })
        .reset_index()
    )
    
    df_merged = pd.merge(
        df_ohlcv,
        news_agg,
        on=["symbol", "date"],
        how="left"
    )
    df_merged = df_merged[df_merged["comp_avg"].notna()].copy()
    
    # Handle missing sentiment values
    if fill_method == "zero":
        df_merged["sentiment_index"] = df_merged["comp_avg"].fillna(0)
    elif fill_method == "ffill":
        df_merged.sort_values(["symbol", "date"], inplace=True)
        df_merged["sentiment_index"] = df_merged.groupby("symbol")["comp_avg"].ffill()
    
    out_path = data_dir / filename
    df_merged.to_csv(out_path, index=False)
    logging.info("Saved merged sentiment ohlcv -> %s (%.2f s)", out_path.name, time.time() - tic)
    
    return df_merged


def stage3_model_predict_and_strategies(
    df_merged: pd.DataFrame,
    features: list,
    data_dir: Path,
    filename: str = "data/results/stage_3_regress_results.csv",
    target_col: str = "return",
    sentiment_threshold: float = 0.05,
    test_size: float = 0.2,
    random_state: int = 42
) -> pd.DataFrame:
    """
    Implements a hybrid of predictive regression and sentiment-based strategy signals.
    Accounts for the following portfolio optimisation strategies
    - RIDGE
    - BOOST
    - OLS
    - EW
    
    Parameters:
        df_merged (pd.DataFrame): Merged OHLCV + sentiment data.
        target_col (str): The column to be predicted (e.g., next-day return).
        features (list): List of feature column names.
        output_path (Path): Optional path to save the dataframe with predictions and strategy.
        sentiment_threshold (float): Threshold for applying sentiment-based filters.
        test_size (float): Proportion of test data for train-test split.
        random_state (int): Random seed for reproducibility.
        
    Returns:
        pd.DataFrame: DataFrame with predictions and strategy signals.
    """
    tic = time.time()
    logging.info("Stage 3 – Training models and generating strategies...")

    df = df_merged.copy()

    if features is None:
        features = ["open", "high", "low", "close", "usd_volume", "sentiment_index"]

    df = df.dropna(subset=features + [target_col])
    if df.empty:
        raise ValueError("No data left after dropping NA rows in features or target. Cannot proceed.")

    logging.info("Training samples available: %d", len(df))

    X = df[features]
    y = df[target_col]
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=test_size, random_state=random_state
    )

    # --- Define all regression models to be tested ---
    strategies_info = {
        "OLS": {"model": LinearRegression()},
        "Ridge": {"model": Ridge(alpha=1.0)},
        "GradientBoosting": {"model": GradientBoostingRegressor(random_state=random_state)}
    }
    
    # --- Loop through and train each model ---
    for name, info in strategies_info.items():
        logging.info(f"Training {name} model...")
        model = info["model"]
        model.fit(X_train, y_train)

        # Generate predictions and signals for each model
        df[f"{name.lower()}_predicted_return"] = model.predict(df[features])
        df[f"{name.lower()}_strategy_signal"] = (
            (df[f"{name.lower()}_predicted_return"] > 0) &
            (df["sentiment_index"] > sentiment_threshold)
        ).astype(int)
        
        # Calculate the actual return of the strategy
        df[f"{name.lower()}_strategy_return"] = df[f"{name.lower()}_strategy_signal"] * df[target_col]

    # --- Add the Equal Weights (EW) benchmark strategy ---
    df["ew_strategy_signal"] = 1 # Always invested
    df["ew_strategy_return"] = df[target_col] # Return is the asset's actual return

    out_path = data_dir / filename
    df.to_csv(out_path, index=False)
    logging.info("Saved regression results -> %s (%.2f s)", out_path.name, time.time() - tic)
    return df


def stage3_returns_strategies_plots(
    df_regress_results: pd.DataFrame,
    fig_dir: Path,
) -> pd.DataFrame:
    """
    Generates key visualisations to compare sentiment impact and portfolio strategy performance.
    Saves plots in the given directory.
    This also accounts for each portfolio optimisation strategy

    Parameters:
        df_regress_results (pd.DataFrame): DataFrame with regression results and strategy info.
        fig_dir (Path): Directory to save output plots.

    Returns:
        pd.DataFrame: The same input DataFrame (unchanged), for possible chaining.
    """
    
    logging.info("Generating visualisations for Stage 3 results...")
    fig_dir.mkdir(parents=True, exist_ok=True)

    df_regress_results = df_regress_results.copy()
    df_regress_results['date'] = pd.to_datetime(df_regress_results['date']).dt.normalize()

    strategy_prefix = "gradientboosting"
    predicted_return_col = f"{strategy_prefix}_predicted_return"
    strategy_signal_col = f"{strategy_prefix}_strategy_signal"
    strategy_return_col = f"{strategy_prefix}_strategy_return"

    df_temp = df_regress_results.copy()
    # The return of a strategy is its signal (0 or 1) multiplied by the *actual* return of the asset
    df_temp['strategy_return_gb'] = df_temp[strategy_signal_col] * df_temp['return']
    
    df_temp = df_temp.groupby(['symbol', 'date'], as_index=False).agg({
        'strategy_return_gb': 'sum',
        predicted_return_col: 'sum'
    })
    df_temp['cumulative_strategy'] = df_temp.groupby('symbol')['strategy_return_gb'].cumsum()
    final_returns = df_temp.groupby('symbol')['cumulative_strategy'].last()
    top5_symbols = final_returns.sort_values(ascending=False).head(5).index

    # --- 1. Daily average sentiment line graph for top 5 coins ---
    sentiment_daily = df_regress_results[df_regress_results['symbol'].isin(top5_symbols)]
    sentiment_daily = sentiment_daily.groupby(['date', 'symbol'])['sentiment_index'].mean().reset_index()
    plt.figure(figsize=(10, 6))
    sns.lineplot(data=sentiment_daily, x='date', y='sentiment_index', hue='symbol')
    plt.title("Daily Average Sentiment by Coin")
    plt.ylabel("Sentiment Index")
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(fig_dir / "sentiment_daily_line.png")
    plt.close()
    
    # --- 2. Average sentiment bar graph for all coins VS market ---
    mean_sentiments_all = df_regress_results.groupby('symbol')['sentiment_index'].mean()
    sorted_coins = mean_sentiments_all.sort_values(ascending=True)
    
    # Load and calculate mean market sentiment
    market_sentiment_path = fig_dir.parent / "clean_data" / "market_sentiment.csv"
    if market_sentiment_path.exists():
        market_df = pd.read_csv(market_sentiment_path)
        if not market_df.empty:
            mean_market_sentiment = market_df['market_sentiment_index'].mean()
            # Create a series for the market and append it to the sorted coins
            market_series = pd.Series([mean_market_sentiment], index=['MARKET'])
            combined_sentiments = pd.concat([sorted_coins, market_series])
            
            # Create custom color palette based on sentiment values
            custom_palette = []
            for sentiment in combined_sentiments.values:
                if sentiment < 0.2: custom_palette.append("#8B0000")
                elif sentiment < 0.4: custom_palette.append("#FF4500")
                elif sentiment < 0.6: custom_palette.append("#FFD700")
                elif sentiment < 0.8: custom_palette.append("#90EE90")
                elif sentiment < 0.99: custom_palette.append("#006400")
                else: custom_palette.append("blue")

            plt.figure(figsize=(15, 7))
            ax = sns.barplot(x=combined_sentiments.index, y=combined_sentiments.values, palette=custom_palette)
            ax.axhline(0, color='grey', linewidth=0.8)
            
            # Add black border to the last bar, which is MARKET
            market_bar = ax.patches[-1]
            market_bar.set_edgecolor('black')
            market_bar.set_linewidth(2)

            plt.title("Mean Daily Sentiment (All Coins vs. Market)")
            plt.ylabel("Average Sentiment Index")
            plt.xlabel("Symbol")
            plt.xticks(rotation=90)
            
            for label in ax.get_xticklabels():
                if label.get_text() == 'MARKET':
                    label.set_fontweight('bold')
                    
            plt.tight_layout()
            plt.savefig(fig_dir / "mean_sentiment_all_vs_market.png")
            plt.close()

    # --- 3. Fear-Greed bar graph for all coins VS market ---
    latest_sentiments_all = df_regress_results.groupby('symbol')['sentiment_index'].last()
    sorted_coins = latest_sentiments_all.sort_values(ascending=True)
    
    # Load and calculate latest market sentiment
    market_sentiment_path = fig_dir.parent / "clean_data" / "market_sentiment.csv"
    if market_sentiment_path.exists():
        market_df = pd.read_csv(market_sentiment_path)
        if not market_df.empty:
            latest_market_sentiment = market_df['market_sentiment_index'].iloc[-1]
            market_series = pd.Series([latest_market_sentiment], index=['MARKET'])
            combined_sentiments = pd.concat([sorted_coins, market_series])
            
            # Create custom color palette based on sentiment values
            fear_greed_colours = []
            for sentiment in combined_sentiments.values:
                if sentiment < 0.2: fear_greed_colours.append("#8B0000")
                elif sentiment < 0.4: fear_greed_colours.append("#FF4500")
                elif sentiment < 0.6: fear_greed_colours.append("#FFD700")
                elif sentiment < 0.8: fear_greed_colours.append("#90EE90")
                elif sentiment < 0.99: fear_greed_colours.append("#006400")
                else: fear_greed_colours.append("blue")

            plt.figure(figsize=(15, 7))
            ax = sns.barplot(x=combined_sentiments.index, y=combined_sentiments.values, palette=fear_greed_colours)
            ax.axhline(0, color='grey', linewidth=0.8)
            
            # Add black border to the last bar, which is MARKET
            market_bar = ax.patches[-1]
            market_bar.set_edgecolor('black')
            market_bar.set_linewidth(2)

            plt.title("Fear-Greed Index (Latest Sentiment by Coin)")
            plt.ylabel("Sentiment Index")
            plt.xticks(rotation=90)
            
            for label in ax.get_xticklabels():
                if label.get_text() == 'MARKET':
                    label.set_fontweight('bold')
                    
            plt.tight_layout()
            plt.savefig(fig_dir / "fear_greed_bar_chart.png")
            plt.close()
    
    # --- 4A. Daily average sentiment line graph for each coin sentiment ---
    sentiment_daily = df_regress_results.groupby(['date', 'symbol'])['sentiment_index'].mean().reset_index()
    
    symbols = sentiment_daily['symbol'].unique()
    for symbol in symbols:
        symbol_data = sentiment_daily[sentiment_daily['symbol'] == symbol]
        plt.figure(figsize=(10, 6))
        sns.lineplot(data=symbol_data, x='date', y='sentiment_index')
        plt.title(f"Daily Average Sentiment - {symbol}")
        plt.ylabel("Sentiment Index")
        plt.xlabel("Date")
        plt.xticks(rotation=45)
        plt.tight_layout()
        plt.savefig(fig_dir / f"sentiment_daily_line_{symbol}.png")
        plt.close()
        
    # --- 4B. Daily average sentiment line graph for market sentiment ---
    market_sentiment_path = fig_dir.parent / "clean_data" / "market_sentiment.csv"
    if market_sentiment_path.exists():
        market_df = pd.read_csv(market_sentiment_path)
        market_df['date'] = pd.to_datetime(market_df['date'])
        if not market_df.empty:
            plt.figure(figsize=(10, 6))
            sns.lineplot(data=market_df, x='date', y='market_sentiment_index')
            plt.title("Daily Average Market Sentiment")
            plt.ylabel("Sentiment Index")
            plt.xlabel("Date")
            plt.xticks(rotation=45)
            plt.tight_layout()
            plt.savefig(fig_dir / "sentiment_daily_line_MARKET.png")
            plt.close()
        
    # --- 5A. Fear-Greed Gauge for each coin sentiment ---
    logging.info("Generating fear-greed gauge for each coin...")
    latest_sentiments = df_regress_results.groupby('symbol')['sentiment_index'].last()
    
    for symbol, sentiment_score in latest_sentiments.items():
        gauge_title = f"{symbol} Fear & Greed Index"
        output_file = fig_dir / f"fear_greed_gauge_{symbol}.png"
        create_polar_gauge(sentiment_score, gauge_title, output_file)
    logging.info("Finished generating fear-greed gauges.")    
    
    # --- 5B. Fear-Greed Gauge for market sentiment ---
    market_sentiment_path = fig_dir.parent / "clean_data" / "market_sentiment.csv"
    if market_sentiment_path.exists():
        logging.info("Generating fear-greed gauge for market sentiment...")
        market_df = pd.read_csv(market_sentiment_path)
        if not market_df.empty:
            latest_market_sentiment = market_df['market_sentiment_index'].iloc[-1]
            create_polar_gauge(
                latest_market_sentiment,
                "Overall Market Fear & Greed Index",
                fig_dir / "fear_greed_gauge_MARKET.png"
            )
            logging.info("Finished generating market sentiment gauge.")
            
    # --- 6. Cumulative returns for the top 5 coins ---
    df = df_temp[df_temp['symbol'].isin(top5_symbols)]
    plt.figure(figsize=(12, 6))
    for symbol, grp in df.groupby('symbol'):
        plt.plot(grp['date'], grp['cumulative_strategy'], label=symbol)

    ax = plt.gca()
    ax.xaxis.set_major_locator(mdates.AutoDateLocator())
    ax.xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m-%d'))
    plt.legend()
    plt.title("Cumulative Strategy Returns by Coin (Top 5)")
    plt.ylabel("Cumulative Return")
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(fig_dir / "cumulative_returns_top5_coins.png")
    plt.close()
    
    # --- 7. Cumulative returns for each strategy VS buy-hold (EW) ---
    all_strategy_cols = [col for col in df_regress_results.columns if '_strategy_return' in col]
    portfolio_daily_returns = df_regress_results.groupby('date')[all_strategy_cols].mean()
    portfolio_cumulative_returns = (1 + portfolio_daily_returns).cumprod()
    
    plt.figure(figsize=(12, 6))
    
    for col in portfolio_cumulative_returns.columns:
        strategy_name = col.replace('_strategy_return', '').upper()
        linestyle = '--' if strategy_name == 'EW' else '-'
        label = "Buy & Hold (EW)" if strategy_name == 'EW' else f"{strategy_name} Strategy"
        plt.plot(portfolio_cumulative_returns.index, portfolio_cumulative_returns[col], label=label, linestyle=linestyle)

    ax = plt.gca()
    ax.xaxis.set_major_locator(mdates.AutoDateLocator())
    ax.xaxis.set_major_formatter(mdates.DateFormatter('%Y-%m-%d'))
    plt.legend()
    plt.title("All Strategies vs. Buy & Hold")
    plt.ylabel("Cumulative Portfolio Return")
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(fig_dir / "strategy_vs_buyhold_all.png")
    plt.close()

    # --- 8. Strategy VS buy-hold for top 5 coins ---
    df['cumulative_return'] = df.groupby('symbol')[predicted_return_col].cumsum()
    plt.figure(figsize=(12, 6))
    for symbol, grp in df.groupby('symbol'):
        plt.plot(grp['date'], grp['cumulative_return'], linestyle='--', label=f"BuyHold-{symbol}")
        plt.plot(grp['date'], grp['cumulative_strategy'], linestyle='-', label=f"Strategy-{symbol}")

    plt.legend()
    plt.title("Cumulative Return: Strategy vs Buy & Hold")
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.savefig(fig_dir / "strategy_vs_buyhold_top5_coins.png")
    plt.close()

    # --- 9. Performance heat map for top 5 EW factors ---
    df_regress_results["date"] = pd.to_datetime(df_regress_results["date"])
    df_regress_results.sort_values("date", inplace=True)
    
    returns_by_symbol = df_regress_results.groupby("symbol")[predicted_return_col].sum().nlargest(5)
    top_symbols_heatmap = returns_by_symbol.index.tolist()
    df_top = df_regress_results[df_regress_results["symbol"].isin(top_symbols_heatmap)]
    
    df_monthly = df_top.copy()
    df_monthly["month"] = df_monthly["date"].dt.to_period("M")
    heatmap_data = df_monthly.groupby(["symbol", "month"])[predicted_return_col].mean().unstack().fillna(0)
    
    plt.figure(figsize=(14, 6))
    sns.heatmap(heatmap_data, cmap="coolwarm", center=0, annot=True, fmt=".2f")
    plt.title("Monthly Avg Returns Heatmap – Top 5 EW Factors")
    plt.tight_layout()
    plt.savefig(fig_dir / "performance_heatmap_top5_coins.png")
    plt.close()
    
    # --- 10. Performance heat map for each strategy ---
    all_strategy_cols = [col for col in df_regress_results.columns if '_strategy_return' in col]
    daily_returns_agg = df_regress_results.groupby('date')[all_strategy_cols].mean()
    
    # Prepare data for heatmap
    df_strat_monthly = daily_returns_agg.copy()
    df_strat_monthly['year'] = df_strat_monthly.index.year
    df_strat_monthly['month'] = df_strat_monthly.index.month
    
    num_strategies = len(all_strategy_cols)
    fig, axes = plt.subplots(num_strategies, 1, figsize=(10, 3 * num_strategies), sharex=True)
    if num_strategies == 1:
        axes = [axes] # Make it iterable if there's only one subplot

    for ax, col in zip(axes, all_strategy_cols):
        strategy_name = col.replace('_strategy_return', '').upper()
        
        monthly_returns = df_strat_monthly.groupby(['year', 'month'])[col].sum().unstack().fillna(0)
        
        sns.heatmap(monthly_returns, ax=ax, cmap="vlag", center=0, annot=True, fmt=".2f", linewidths=.5)
        ax.set_title(f"Monthly Returns Heatmap – {strategy_name} Strategy")
        ax.set_ylabel("Year")
        ax.set_xlabel("Month")

    plt.tight_layout()
    plt.savefig(fig_dir / "performance_heatmap_strategies.png")
    plt.close()
    
    # --- 11. Performance metrics radar chart ---
    logging.info("Generating multi-strategy performance radar chart...")
    
    all_strategy_returns = [col for col in df_regress_results.columns if '_strategy_return' in col]
    daily_returns = df_regress_results.groupby('date')[all_strategy_returns].mean()

    metrics = {}
    for col in daily_returns.columns:
        strategy_name = col.replace('_strategy_return', '').upper()
        returns = daily_returns[col]
        
        cumulative = (1 + returns).cumprod()
        peak = cumulative.cummax()
        drawdown = (cumulative - peak) / peak
        max_drawdown = drawdown.min()
        
        downside_returns = returns[returns < 0]
        downside_deviation = downside_returns.std() * np.sqrt(365)
        
        annualized_return = returns.mean() * 365
        annualized_vol = returns.std() * np.sqrt(365)
        
        sharpe_ratio = annualized_return / (annualized_vol + 1e-9)

        # --- FIX: Handle case where there are no downside returns ---
        if pd.isna(downside_deviation) or downside_deviation == 0:
            sortino_ratio = 0.0  # Set to 0 if there's no downside deviation
        else:
            sortino_ratio = annualized_return / downside_deviation
        
        metrics[strategy_name] = {
            'Return': annualized_return,
            'Volatility': annualized_vol,
            'Sharpe Ratio': sharpe_ratio,
            'Max Drawdown': max_drawdown,
            'Sortino Ratio': sortino_ratio
        }
        
    metrics_df = pd.DataFrame(metrics).T
    
    ranked_df = metrics_df.rank(ascending=True)
    ranked_df['Volatility'] = ranked_df['Volatility'].rank(ascending=False)
    ranked_df['Max Drawdown'] = ranked_df['Max Drawdown'].rank(ascending=False)
    
    labels = ranked_df.columns.tolist()
    num_vars = len(labels)
    
    angles = np.linspace(0, 2 * np.pi, num_vars, endpoint=False).tolist()
    angles += angles[:1]

    fig, ax = plt.subplots(figsize=(8, 8), subplot_kw=dict(polar=True))
    
    for i, row in ranked_df.iterrows():
        data = row.tolist()
        data += data[:1]
        ax.plot(angles, data, label=i)
        ax.fill(angles, data, alpha=0.1)

    ax.set_thetagrids(np.degrees(angles[:-1]), labels)
    ax.set_title("Strategy Performance Comparison", size=20, color='black', y=1.1)
    ax.legend(loc='upper right', bbox_to_anchor=(1.3, 1.1))
    
    plt.tight_layout()
    plt.savefig(fig_dir / "performance_metrics_radar_strategy.png")
    plt.close()
    
    # --- 12. Monthly return “candles” for top 5 EW factors ---
    plt.figure(figsize=(12, 6))
    df_monthly["month_str"] = df_monthly["month"].astype(str)
    sns.boxplot(data=df_monthly, x="month_str", y=predicted_return_col, hue="symbol")
    plt.title("Monthly Return Distribution – Top 5 EW Factors"); plt.xticks(rotation=45)
    plt.tight_layout(); plt.savefig(fig_dir / "monthly_return_candles_top5.png"); plt.close()
    
    # --- 13 & 14. Rolling Volatility and Sharpe Ratio for top 5 coins ---
    df_top_rolling = df_regress_results[df_regress_results["symbol"].isin(top5_symbols)]
    plt.figure(figsize=(12, 6))
    for symbol in top5_symbols:
        df_symbol = df_top_rolling[df_top_rolling["symbol"] == symbol].set_index("date")
        rolling_vol = df_symbol[predicted_return_col].rolling(window=14).std()
        plt.plot(rolling_vol.index, rolling_vol, label=symbol)
    plt.title("14-Day Rolling Volatility – Top 5 Factors"); plt.legend()
    plt.tight_layout(); plt.savefig(fig_dir / "rolling_volatility_top5.png"); plt.close()

    plt.figure(figsize=(12, 6))
    for symbol in top5_symbols:
        df_symbol = df_top_rolling[df_top_rolling["symbol"] == symbol].set_index("date")
        rolling_mean = df_symbol[predicted_return_col].rolling(window=14).mean()
        rolling_std = df_symbol[predicted_return_col].rolling(window=14).std()
        sharpe = rolling_mean / (rolling_std + 1e-9)
        plt.plot(sharpe.index, sharpe, label=symbol)
    plt.title("14-Day Rolling Sharpe Ratio – Top 5 Factors"); plt.legend()
    plt.tight_layout(); plt.savefig(fig_dir / "rolling_sharpe_ratio_top5.png"); plt.close()

    # --- 15 & 16. Rolling Volatility and Sharpe Ratio for each strategy ---
    all_strategy_cols = [col for col in df_regress_results.columns if '_strategy_return' in col]
    daily_returns_agg = df_regress_results.groupby('date')[all_strategy_cols].mean()

    plt.figure(figsize=(12, 6))
    for col in daily_returns_agg.columns:
        strategy_name = col.replace('_strategy_return', '').upper()
        rolling_vol = daily_returns_agg[col].rolling(window=14).std() * np.sqrt(365)
        plt.plot(rolling_vol.index, rolling_vol, label=strategy_name)
    plt.title("14-Day Rolling Volatility – All Strategies")
    plt.ylabel("Annualized Volatility")
    plt.legend()
    plt.tight_layout()
    plt.savefig(fig_dir / "rolling_volatility_strategies.png")
    plt.close()

    plt.figure(figsize=(12, 6))
    for col in daily_returns_agg.columns:
        strategy_name = col.replace('_strategy_return', '').upper()
        rolling_mean = daily_returns_agg[col].rolling(window=14).mean() * 365
        rolling_std = daily_returns_agg[col].rolling(window=14).std() * np.sqrt(365)
        sharpe = rolling_mean / (rolling_std + 1e-9)
        plt.plot(sharpe.index, sharpe, label=strategy_name)
    plt.title("14-Day Rolling Sharpe Ratio – All Strategies")
    plt.ylabel("Annualized Sharpe Ratio")
    plt.legend()
    plt.tight_layout()
    plt.savefig(fig_dir / "rolling_sharpe_ratio_strategies.png")
    plt.close()

    # --- 17 & 18. Return Distribution and Correlation for top 5 coins ---
    df_top_dist = df_regress_results[df_regress_results["symbol"].isin(top5_symbols)]
    plt.figure(figsize=(12, 6))
    sns.histplot(data=df_top_dist, x=predicted_return_col, hue="symbol", element="step", stat="density", common_norm=False, bins=50)
    plt.title("Return Distribution – Top 5 Factors")
    plt.tight_layout(); plt.savefig(fig_dir / "return_distribution_top5.png"); plt.close()

    pivot_df = df_top_dist.pivot_table(index="date", columns="symbol", values=predicted_return_col)
    corr_matrix = pivot_df.corr()
    plt.figure(figsize=(8, 6)); sns.heatmap(corr_matrix, annot=True, cmap="vlag", center=0)
    plt.title("Correlation Matrix – Top 5 Factors")
    plt.tight_layout(); plt.savefig(fig_dir / "correlation_matrix_top5.png"); plt.close()
    
    # --- 19 & 20. Return Distribution and Correlation for each strategy ---
    all_strategy_cols = [col for col in df_regress_results.columns if '_strategy_return' in col]
    daily_returns_agg = df_regress_results.groupby('date')[all_strategy_cols].mean()
    
    all_strategy_returns_melted = pd.melt(daily_returns_agg, var_name='Strategy', value_name='Return')
    all_strategy_returns_melted['Strategy'] = all_strategy_returns_melted['Strategy'].str.replace("_strategy_return", "").str.upper()

    plt.figure(figsize=(12, 6))
    sns.histplot(data=all_strategy_returns_melted, x='Return', hue='Strategy', element="step", stat="density", common_norm=False, bins=50)
    plt.title("Return Distribution for All Strategies")
    plt.tight_layout()
    plt.savefig(fig_dir / "return_distribution_all_strategies.png")
    plt.close()

    corr_matrix_strategies = daily_returns_agg.corr()
    corr_matrix_strategies.columns = [col.replace('_strategy_return', '').upper() for col in corr_matrix_strategies.columns]
    corr_matrix_strategies.index = [idx.replace('_strategy_return', '').upper() for idx in corr_matrix_strategies.index]
    
    plt.figure(figsize=(8, 6))
    sns.heatmap(corr_matrix_strategies, annot=True, cmap="vlag", center=0)
    plt.title("Correlation Matrix – All Strategies")
    plt.tight_layout()
    plt.savefig(fig_dir / "correlation_matrix_all_strategies.png")
    plt.close()
    
    # Finally...
    logging.info("Saved sentiment and strategy plots to %s", fig_dir)
    return df_regress_results

    
def export_txt_tables(
    df_regress_results: pd.DataFrame,
    data_dir: Path,
) -> None:
    """
    Calculates and exports four key data tables as txt files for a frontend application.
    """
    logging.info("Exporting tables for frontend...")

    # --- 1. Strategy Performance Summary ---
    all_strategy_returns = [col for col in df_regress_results.columns if '_strategy_return' in col]
    daily_returns = df_regress_results.groupby('date')[all_strategy_returns].mean()
    
    metrics = {}
    for col in daily_returns.columns:
        strategy_name = col.replace('_strategy_return', '').upper()
        returns = daily_returns[col]
        
        cumulative = (1 + returns).cumprod()
        peak = cumulative.cummax()
        drawdown = (cumulative - peak) / peak
        max_drawdown = drawdown.min()
        
        downside_returns = returns[returns < 0]
        downside_deviation = downside_returns.std() * np.sqrt(365)
        
        annualized_return = returns.mean() * 365
        annualized_vol = returns.std() * np.sqrt(365)
        sharpe_ratio = annualized_return / (annualized_vol + 1e-9)
        
        if pd.isna(downside_deviation) or downside_deviation == 0:
            sortino_ratio = 0.0
        else:
            sortino_ratio = annualized_return / downside_deviation
        
        metrics[strategy_name] = {
            'Return': annualized_return,
            'Volatility': annualized_vol,
            'Sharpe Ratio': sharpe_ratio,
            'Max Drawdown': max_drawdown,
            'Sortino Ratio': sortino_ratio
        }
    metrics_df = pd.DataFrame(metrics).T
    metrics_df.to_csv(data_dir / "strategy_performance_summary.txt", sep='\t', float_format='%.3f')
    logging.info("Exported strategy_performance_summary.txt")

    # --- 2. Latest Sentiment Scores ---
    latest_sentiments_all = df_regress_results.groupby('symbol')['sentiment_index'].last()
    
    market_sentiment_path = data_dir / "market_sentiment.csv"
    if market_sentiment_path.exists():
        market_df = pd.read_csv(market_sentiment_path)
        if not market_df.empty:
            latest_market_sentiment = market_df['market_sentiment_index'].iloc[-1]
            market_series = pd.Series([latest_market_sentiment], index=['MARKET'])
            latest_sentiments_all = pd.concat([latest_sentiments_all, market_series])
            
    latest_sentiments_df = latest_sentiments_all.to_frame(name='LatestSentiment')
    latest_sentiments_df.to_csv(data_dir / "latest_sentiment_scores.txt", sep='\t', float_format='%.3f')
    logging.info("Exported latest_sentiment_scores.txt")

    # --- 3. Coin Performance Ranking (Gradient Boosting Strategy) ---
    df_temp = df_regress_results.copy()
    df_temp['strategy_return_gb'] = df_temp['gradientboosting_strategy_signal'] * df_temp['return']
    df_temp['cumulative_strategy'] = df_temp.groupby('symbol')['strategy_return_gb'].cumsum()
    final_returns = df_temp.groupby('symbol')['cumulative_strategy'].last().sort_values(ascending=False)
    final_returns_df = final_returns.to_frame(name='FinalStrategyReturn')
    final_returns_df.to_csv(data_dir / "coin_performance_summary.txt", sep='\t', float_format='%.3f')
    logging.info("Exported coin_performance_summary.txt")
    
    # --- 4. Current Strategy Signals ---
    latest_date = df_regress_results['date'].max()
    current_signals_df = df_regress_results[df_regress_results['date'] == latest_date]
    
    signal_cols = ['symbol'] + [col for col in df_regress_results.columns if '_strategy_signal' in col]
    current_signals_df = current_signals_df[signal_cols]
    
    melted_signals = current_signals_df.melt(
        id_vars=['symbol'], 
        var_name='Strategy', 
        value_name='Signal'
    )
    melted_signals['Strategy'] = melted_signals['Strategy'].str.replace('_strategy_signal', '').str.upper()
    
    active_signals = melted_signals[melted_signals['Signal'] == 1][['Strategy', 'symbol']]
    active_signals.to_csv(data_dir / "current_strategy_signals.txt", sep='\t', index=False)
    logging.info("Exported current_strategy_signals.txt")
    
    