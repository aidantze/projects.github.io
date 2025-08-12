"""
z5360925
FINS3645 FinTech Project Guidelines
Cryptocurrency Investment Product
------------------------------------------------------------------------------

TILT.ai Crypto data pipeline – Stage 1 and Stage 2.

Stage 1 : 
    - Download daily OHLCV for the top coins
    - Download daily news articles about the top coins
Stage 2 :  
    - Derive volume shocks, log returns, momentum, volatility, max drawdown,
      value at risk, and weekly and multi-period returns, 
      and save a cleaned weekly data set.
    - Clean news article text to prepare for VADER sentiment analysis
    
All timestamps are in UTC for consistency across datasets
"""

from __future__ import annotations

import argparse
import logging
from datetime import datetime
import time
from pathlib import Path
from typing import List, Dict
from collections import Counter

from tqdm import tqdm
import numpy as np
import pandas as pd
import requests

import re
from bs4 import BeautifulSoup

import nltk
from nltk.corpus import stopwords
from nltk.tokenize import word_tokenize
from nltk.stem import WordNetLemmatizer

# nltk downloads (need only run these once)
nltk.download('punkt_tab')
nltk.download('wordnet')
nltk.download("stopwords")

# ---------------------------------------------------------------------------
# Logging --------------------------------------------------------------------
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)
tqdm.pandas()

# ---------------------------------------------------------------------------
# Directory helpers ----------------------------------------------------------


def _ensure_dir(root: Path, sub: str | Path) -> Path:
    """Return *root/sub* as Path, creating parents as needed."""
    path = root / sub
    path.mkdir(parents=True, exist_ok=True)
    return path


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


# ---------------------------------------------------------------------------
# API helpers ----------------------------------------------------------------

BASE_URL = "https://data-api.coindesk.com"


def _headers(api_key: str) -> dict[str, str]:
    return {"authorization": f"Apikey {api_key}"}


# ---------------------------------------------------------------------------
# Stage 1 – ETL --------------------------------------------------------------


def get_top_coins(
    api_key: str,
    pages: List[int],
    limit: int = 100,
    sort_by: str = "CIRCULATING_MKT_CAP_USD",
) -> Dict[str, str]:
    """
    Return a dict of coin symbols across *pages* sorted by *sort_by*.
    
    Format of dict: {
        symbol: name    
    }
    """
    coins: Dict[str, str] = {}

    for page in pages:
        url = (
            f"{BASE_URL}/asset/v1/top/list?"
            f"page={page}&page_size={limit}"
            f"&sort_by={sort_by}&sort_direction=DESC"
            "&groups=ID,BASIC,MKT_CAP"
        )
        resp = requests.get(url, headers=_headers(api_key), timeout=30)
        data = resp.json()

        if "Data" not in data or "LIST" not in data["Data"]:
            logging.warning("Page %d returned no data: %s", page, data.get("Message"))
            continue

        for coin in data["Data"]["LIST"]:
            symbol = coin.get("SYMBOL")
            name = coin.get("NAME")
            if symbol and name:
                coins[symbol] = name

        logging.info("Collected %d symbols from page %d", len(data["Data"]["LIST"]), page)

    if not coins:
        raise RuntimeError("No symbols retrieved. Check API key or parameters.")

    return coins


def get_daily_ohlcv(
    symbol: str,
    api_key: str,
    limit: int = 2000,
    currency: str = "USD",
) -> pd.DataFrame:
    """Return daily OHLCV for *symbol* or None on error."""
    url = (
        f"{BASE_URL}/index/cc/v1/historical/days"
        f"?market=cadli&instrument={symbol}-{currency}"
        f"&limit={limit}&aggregate=1&fill=true&apply_mapping=true"
    )
    try:
        resp = requests.get(url, headers=_headers(api_key), timeout=30)
        data = resp.json()
    except Exception as e:
        logging.warning("Unable to retrieve daily ohlcv for %s: %s", symbol, str(e))
        return None

    if data.get("Response") == "Error" or "Data" not in data:
        logging.warning("No data for %s: %s", symbol, data.get("Message"))
        return None

    df = pd.DataFrame(data["Data"])
    if df.empty or "TIMESTAMP" not in df.columns:
        logging.warning("Malformed or empty DataFrame for %s", symbol)
        return None
    
    # logging.info(df.head())
    df["date"] = pd.to_datetime(df["TIMESTAMP"], unit="s")
    df = df.rename(
        columns={
            "OPEN": "open",
            "HIGH": "high",
            "LOW": "low",
            "CLOSE": "close",
            "VOLUME": "crypto_volume",
            "QUOTE_VOLUME": "usd_volume",
        }
    )

    df = df[["date", "open", "high", "low", "close", "usd_volume", "crypto_volume"]].copy()
    df["usd_volume_mil"] = df["usd_volume"] / 1e6
    df["symbol"] = symbol
    df.set_index(["symbol", "date"], inplace=True)

    return df


def fetch_news_range(
    api_key: str | None,
    start_dt: datetime,
    end_dt: datetime,
    lang: str = "EN",
) -> pd.DataFrame:
    """
    Pull CoinDesk news between *start_dt* and *end_dt* (inclusive).
    Logs the query date at each step so you can track progress.
    """
    url = "https://data-api.coindesk.com/news/v1/article/list"
    out: list[pd.DataFrame] = []

    while end_dt > start_dt:
        query_ts  = int(end_dt.timestamp())
        query_day = end_dt.strftime("%Y-%m-%d")
        logging.info("Requesting articles up to %s (UTC)", query_day)

        resp = requests.get(f"{url}?lang={lang}&to_ts={query_ts}")
        if not resp.ok:
            logging.error("Request failed with status %s", resp.status_code)
            break

        d = pd.DataFrame(resp.json()["Data"])
        if d.empty:
            logging.info("No data returned for %s – stopping loop.", query_day)
            break

        d["date"] = pd.to_datetime(d["PUBLISHED_ON"], unit="s")
        out.append(d[d["date"] >= start_dt])

        # step backward to the day before the earliest article we just received
        end_dt = datetime.utcfromtimestamp(d["PUBLISHED_ON"].min() - 1)

    news = pd.concat(out, ignore_index=True) if out else pd.DataFrame()
    logging.info("Fetched %d articles in total", len(news))
    return news



def load_news(
    api_key: str | None,
    start_dt: datetime,
    end_dt: datetime,
    data_dir: Path,
    filename: str = "stage_1_news_raw.csv",
) -> pd.DataFrame:
    """
    Download CoinDesk news and save raw filtered fields to CSV.
    """
    tic = time.time()
    logging.info("Stage 1 – downloading news …")

    df = fetch_news_range(api_key, start_dt, end_dt)

    if df.empty:
        logging.warning("No news articles retrieved from CoinDesk.")
        return df

    drop_cols = [
        "GUID", "PUBLISHED_ON_NS", "IMAGE_URL", "SUBTITLE", "AUTHORS", "URL",
        "UPVOTES", "DOWNVOTES", "SCORE", "CREATED_ON", "UPDATED_ON",
        "SOURCE_DATA", "CATEGORY_DATA", "STATUS", "SOURCE_ID", "TYPE",
        "PUBLISHED_ON"
    ]
    df = df.drop(columns=[c for c in drop_cols if c in df.columns])
    df.columns = df.columns.str.lower()
    
    # combine title and body
    df['content'] = df['title'].astype(str) + '. ' + df['body'].astype(str)
    df = df.drop(columns=[c for c in ['title', 'body']])

    keep_order = ["date", "id"] + [c for c in df.columns if c not in ["date", "id"]]
    df = df[keep_order]

    out_path = data_dir / filename
    df.to_csv(out_path, index=False)
    logging.info("Saved raw news -> %s (%.2f s)", out_path.name, time.time() - tic)
    return df


# remaining articles without any mention of a coin could be talking about the
# crypto economy. Use this list to determine news article containing market-
# moving macroeconomic events
MACRO_KEYWORDS = [
    "regulation", "sec", "Federal Reserve", "interest rate", "inflation",
    "cpi", "monetary policy", "crypto ban", "bitcoin etf", "institutional",
    "lawsuit", "stablecoin regulation", "cbdc", "financial system", 
    "BlackRock", "crisis", "us Treasury", "debt ceiling", "volatility", 
    "crypto market crash", "exchange failures", "quantitative tightening"
]

SPAM_PHRASES = [
    "Are You Chasing New Coins?",
    "Click here to discover new altcoins",
    "Be the first to buy",
    "Make quick crypto profits",
    "1000x gains"
]

def is_macro_article(text: str) -> bool:
    return any(keyword.lower() in text.lower() for keyword in MACRO_KEYWORDS)


def match_coin_news(
    coins: dict[str, str],
    all_news_df: pd.DataFrame,
) -> pd.DataFrame:
    """
    Filters CoinDesk news for relevance to each coin.
    Adds a 'symbol' column to each matched article, including macroeconomic
    news
    
    Parameters:
        coins: Mapping of symbols to coin names.
        all_news_df: Raw DataFrame from load_news().
        
    Returns:
        DataFrame with matched articles, tagged with coin symbol.
    """
    df_news = all_news_df.copy()
    
    # Drop spam articles
    spam_pattern = "|".join(re.escape(p) for p in SPAM_PHRASES)
    df_news = df_news[~df_news["content"]
                      .str.contains(spam_pattern, case=False, na=False)]

    # Tag macroeconomic articles
    df_news["is_macro"] = df_news["content"].apply(is_macro_article)
    df_news["symbol"] = pd.NA  # initialize symbol column

    # Match coin mentions
    for symbol, name in coins.items():
        coin_mask = df_news["content"].str.contains(name, case=False, na=False)
        df_news.loc[coin_mask, "symbol"] = symbol

    # Assign 'MARKET' to macro articles that still have no symbol
    macro_mask = df_news["is_macro"] & df_news["symbol"].isna()
    df_news.loc[macro_mask, "symbol"] = "MARKET"

    # Keep only articles that were matched
    df_news = df_news.dropna(subset=["symbol"]).copy()

    if df_news.empty:
        logging.warning("No coin or macroeconomic news matched.")

    return df_news


def stage_1_etl(
    coins: dict[str, str],
    api_key: str | None,
    start_dt: datetime,
    end_dt: datetime,
    data_dir: str = "data",
    history_limit: int = 2000,
    currency: str = "USD"
) -> tuple[pd.DataFrame, pd.DataFrame]:
    """
    Stage 1 ETL for crypto portfolio optimizer.
    Downloads OHLCV for each coin and filters relevant CoinDesk news.
    
    Parameters:
        coins: Mapping of symbols to coin names (e.g., {"BTC": "Bitcoin"})
        api_key: API key for CoinDesk (can be None if not required)
        start_dt: Start date for historical news/articles
        end_dt: End date for news/articles
        data_dir: Directory to store CSV outputs
    """
    logging.info("Starting Stage 1 ETL")
    output_path = Path(data_dir)
    output_path.mkdir(parents=True, exist_ok=True)

    # Step 1: Process each coin
    for symbol, name in coins.items():
        logging.info("Processing %s (%s)", symbol, name)

        df_ohlcv = get_daily_ohlcv(
            symbol=symbol,
            api_key=api_key,
            limit=history_limit,  # need to pass this in stage_1_etl()
            currency=currency
        )
        if df_ohlcv is not None and not df_ohlcv.empty:
            df_ohlcv.to_csv(output_path / f"{symbol}_ohlcv.csv")
        else:
            logging.warning("No OHLCV data found for %s", symbol)
        
    logging.info("Completed processing of OHLCV data")
        
    # Step 2: Process recent news articles
    df_news = load_news(api_key, start_dt, end_dt, output_path)
    df_news = match_coin_news(coins, df_news)

    if df_news.empty:
        logging.warning("No relevant crypto news found.")
    else:
        out_path = output_path / "stage_1_news_raw.csv"
        df_news.to_csv(out_path, index=False)
        logging.info("Saved filtered news -> %s", out_path.name)

    logging.info("Stage 1 ETL complete.")
    
    # Step 3: return both dataframes for use in stage 2
    df_prices = pd.concat(
        [
            pd.read_csv(f)
            for f in data_dir.glob("*_ohlcv.csv")
            if f.name.endswith("_ohlcv.csv")
        ],
        axis=0,
        ignore_index=False,
    )
    df_prices.set_index(["symbol", "date"], inplace=True)
    return df_prices, df_news


# ---------------------------------------------------------------------------
# Stage 2 – feature engineering ---------------------------------------------


def stage_2_ohlcv_feature_engineering(
    tidy_prices: pd.DataFrame | None = None,
    csv_path: Path | None = None,
    data_dir: Path | None = None,
    filename: str = "stage_2_crypto_data.csv",
) -> pd.DataFrame:
    """
    Stage 2 – Feature Engineering (Structured Data)
    
    Generates core features from daily OHLCV data including:
    - Volume shocks (log deviation from rolling mean)
    - Log returns and return-based features (momentum, volatility, short-term reversal)
    - Value-at-Risk (5%)
    - Max Drawdown
    - Weekly and multi-period returns
    """

    if tidy_prices is None:
        if csv_path is None:
            raise ValueError("Provide either tidy_prices or csv_path.")
        logging.info("Reading Stage 1 CSV from %s", csv_path)
        tidy_prices = pd.read_csv(
            csv_path, index_col=["symbol", "date"], parse_dates=["date"]
        )

    df = tidy_prices.reset_index().sort_values(["symbol", "date"]).copy()
    
    # Weekly & multi-week returns
    df["return"] = df.groupby("symbol")["close"].pct_change()
    df["return"] = np.clip(df["return"], -1, 2)
    df["strev_weekly"] = df["return"]

    for w in [2, 4]:
        df[f"return_{w}w"] = (
            df.groupby("symbol")["close"]
            .pct_change(periods=w)
        )

    df = df.reset_index()

    # Volume shocks
    for m in [7, 14, 21, 28, 42]:
        rolling_mean = (
            df.groupby("symbol")["usd_volume"]
            .shift(1)
            .rolling(m, min_periods=m)
            .mean()
        )
        df[f"usd_v_{m}d"] = np.log(df["usd_volume"]) - np.log(rolling_mean)

    # Log returns
    df["log_return"] = np.log1p(df.groupby("symbol")["close"].pct_change())
    df["log_return"] = np.clip(df["log_return"], -2, 2)
    df = df.replace([-np.inf, np.inf], np.nan)

    # Momentum & Volatility
    for m in [14, 21, 28, 42, 90]:
        shifted = df.groupby("symbol")["log_return"].shift(7)
        df[f"momentum_{m}"] = (np.exp(shifted.rolling(m, min_periods=m).sum()) - 1.0)
        df[f"volatility_{m}"] = (
            df.groupby("symbol")["log_return"]
            .rolling(m, min_periods=m)
            .std()
            .reset_index(level=0, drop=True)
        ) * np.sqrt(365.0)

    # Value-at-Risk (5%)
    df["VaR_5"] = (
        df.groupby("symbol")["log_return"]
        .rolling(30, min_periods=20)
        .quantile(0.05)
        .reset_index(level=0, drop=True)
    )

    # Max Drawdown
    def compute_drawdown(series):
        cumulative = (1 + series).cumprod()
        max_roll = cumulative.cummax()
        drawdown = (cumulative - max_roll) / max_roll
        return drawdown

    df["drawdown"] = (
        df.groupby("symbol")["return"]
        .apply(lambda x: compute_drawdown(x.fillna(0)))
        .reset_index(level=0, drop=True)
    )

    # Short-term reversal
    df["strev_daily"] = df["log_return"]

    # Weekly Resample
    df["date"] = pd.to_datetime(df["date"])
    # dfw = (
    #     df.set_index(["symbol", "date"])
    #         .groupby([pd.Grouper(level="symbol"), pd.Grouper(level="date", freq="W-WED")])
    #         .last()
    #         .reset_index()
    # )

    # Filter out stablecoins and wrapped tokens
    stable_tickers = [
        "USD", "USDT", "USDC", "TUSD", "BUSD", "PAX", "USDP", "GUSD",
        "DAI", "SUSD", "USDN", "FRAX", "USDX", "USDJ", "XUSD", "USDD",
        "UST", "USTC",
        "EUR", "EURT", "EURS", "EUROC", "SEUR", "SEUR", "SEUR", "SEUR",
        "AEUR", "EURC", "AGEUR", "PAR","PAXG", "PYUSD", "USD1", "USDE"
    ]
    wrapped_tickers = [
        "WBTC", "WETH", "WBNB", "WSTETH", "WUSDC", "WUSDT",
        "WCRO", "WFTM", "WTRX", "WCELO", "WFIL", "WGLMR",
        "WXRP", "WLTC", "WSOL", "WADA",
    ]
    
    tickers_to_drop = {t.upper() for t in stable_tickers + wrapped_tickers}
    # tickers_to_drop = {t.upper() for t in stable_tickers.union(wrapped_tickers)}
    
    is_exact_drop = df["symbol"].str.upper().isin(tickers_to_drop)
    has_usd_substr = df["symbol"].str.upper().str.contains("USD", na=False)
    df = df[~(is_exact_drop | has_usd_substr)].copy()

    # Final cleaning
    df = df[df["return"] > -1.0]  # drop delist-like events
    df = df.replace([-np.inf, np.inf], np.nan)

    # Column ordering
    col_order = [  # Add more as needed
        "date", "symbol", "open", "high", "low", "close", "usd_volume", 
        "crypto_volume", "return", "return_2w", "return_4w", "usd_v_7d", 
        "usd_v_14d", "momentum_14", "volatility_14", "VaR_5",
        "drawdown", "strev_daily", "strev_weekly"
    ]
    df = df[[col for col in col_order if col in df.columns]]

    if data_dir is not None:
        out_path = data_dir / filename
        df.to_csv(out_path, index=False)
        logging.info("Stage 2 CSV written to %s", out_path)

    return df


_STOP = set(stopwords.words("english"))
_LEM = WordNetLemmatizer()


def _preprocess_text(txt: str) -> str:
    # Stage 1: basic cleaning
    txt = BeautifulSoup(txt, "html.parser").get_text()  # remove HTML
    txt = txt.encode("utf-8", errors="ignore").decode("utf-8", errors="ignore")  # UTF-8 clean
    txt = txt.lower()
    
    # Stage 2: tokenisation & normalisation
    tokens = word_tokenize(txt)
    keep = [t for t in tokens if t.isalpha() and t not in _STOP]
    lemmas = [_LEM.lemmatize(t) for t in keep]
    return " ".join(lemmas)


def _basic_tokens(txt: str) -> list[str]:
    tok = txt.split()
    return [w for w in tok if w.isalpha() and w not in _STOP]


def stage2_news_clean_text(
    df_raw: pd.DataFrame | None,
    data_dir: Path,
    max_common: int = 500,
    filename: str = "stage_2_news_clean.csv",
    csv_path: Path | None = None,
) -> pd.DataFrame:
    """
    Cleans raw news articles (from the 'content' column) and prepares them for sentiment analysis.
    Also outputs most common tokens.
    """
    tic = time.time()
    logging.info("Stage 2 – Cleaning news text...")
    
    if df_raw is None:
        if csv_path is None or not csv_path.exists():
            raise ValueError("csv_path must be provided and point to an existing file if df_raw is None.")
        logging.info(f"Reading raw news data from {csv_path}")
        df_raw = pd.read_csv(csv_path)

    # Ensure required columns exist
    required_cols = {"content", "date", "symbol"}
    missing_cols = required_cols - set(df_raw.columns)
    if missing_cols:
        raise ValueError(f"Missing required columns in input: {missing_cols}")

    df = df_raw.copy()
    df["date"] = pd.to_datetime(df["date"]).dt.date

    # Clean content and store in 'reviewText'
    df["reviewText"] = df["content"].progress_apply(_preprocess_text)

    # Identify most common words for inspection/report
    joined_tokens = " ".join(df["reviewText"])
    counts = Counter(_basic_tokens(joined_tokens)).most_common(max_common)

    pd.DataFrame(counts, columns=["word", "count"]).to_csv(
        data_dir / "stage_2_common_words.csv", index=False
    )
    
    df["n_words"] = df["reviewText"].astype(str).str.split().str.len()

    # Save cleaned text
    out_path = data_dir / filename
    df.to_csv(out_path, index=False)
    logging.info("Saved cleaned news -> %s (%.2f s)", out_path.name, time.time() - tic)

    return df


# ---------------------------------------------------------------------------
# CLI -----------------------------------------------------------------------

def _parse_args() -> argparse.Namespace:  # pragma: no cover
    p = argparse.ArgumentParser(
        prog="crypto_pipeline",
        description="Crypto ETL (Stage 1) and feature engineering (Stage 2)",
    )
    p.add_argument("--api_key", required=True, help="CryptoCompare API key")
    p.add_argument("--pages", type=int, nargs="+", default=[1], help="Pages for top coins")
    p.add_argument("--top_limit", type=int, default=100, help="Coins per page")
    p.add_argument("--history_limit", type=int, default=2000, help="Days of history")
    p.add_argument("--currency", default="USD", help="Quote currency")
    p.add_argument("--output_folder", default="data", help="Output folder")
    return p.parse_args()


def _main() -> None:
    args = _parse_args()
    data_dir = build_week_dirs(args.output_folder)

    # Set date range for retrieval of articles. Defaults to 3 months ago
    end_dt = datetime.utcnow()
    start_dt = end_dt.replace(
        hour=0, minute=0, second=0, microsecond=0
    ) - pd.DateOffset(years=0, months=3, days=0)

    # --- Get top coins ---
    coins = get_top_coins(
        api_key=args.api_key,
        pages=args.pages,
        limit=args.top_limit,
        sort_by="CIRCULATING_MKT_CAP_USD",
    )

    # --- Stage 1: Raw Data Download ---
    df_prices, df_news = stage_1_etl(
        coins=coins,
        api_key=args.api_key,
        start_dt=start_dt,
        end_dt=end_dt,
        data_dir=data_dir,
        history_limit=args.history_limit,
        currency=args.currency,
    )
    
    # uncomment the code below if stage 1 already complete, to save on querying
    # from CoinDesk API again (due to request limits)
    # df_prices = pd.concat(
    #     [
    #         pd.read_csv(f)
    #         for f in data_dir.glob("*_ohlcv.csv")
    #         if f.name.endswith("_ohlcv.csv")
    #     ],
    #     axis=0,
    #     ignore_index=False,
    # )
    # df_prices.set_index(["symbol", "date"], inplace=True)
    # df_news = pd.read_csv("./data/results/clean_data/stage_1_news_raw.csv");

    # --- Stage 2: Feature Engineering ---
    stage_2_ohlcv_feature_engineering(
        tidy_prices=df_prices,
        data_dir=data_dir,
    )
    
    stage2_news_clean_text(
        df_raw=df_news,
        data_dir=data_dir
    )

    print("Done!")
    print("Data ->", data_dir.resolve())


if __name__ == "__main__":  # pragma: no cover
    _main()
    