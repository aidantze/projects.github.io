import pandas as pd

from stage3_4 import (
    stage_3_sentiment_scores,
    merge_sentiment_ohlcv,
    stage3_model_predict_and_strategies,
    stage3_returns_strategies_plots,
    export_txt_tables,
    build_week_dirs,
    build_fig_dirs
)

# ---------------------------------------------------------------------------
# User-adjustable inputs -----------------------------------------------------
API_KEY = ""                           # required
PAGES = [1]                            # which pages of the top-list to pull
TOP_LIMIT = 50                         # coins per page
HISTORY_LIMIT = 2000                   # days of history per coin
CURRENCY = "USD"                       # quote currency
YEARS_AGO = 0                          # no. years to query articles from
MONTHS_AGO = 6                         # no. months to query articles from
DAYS_AGO = 0                           # no. days to query articles from
OUTPUT_FOLDER = "data"                 # root for outputs
# ---------------------------------------------------------------------------


# BASE_DIR = "./results/news_clean_data"

def main():
    """Run Stage 3 then Stage 4 with the constants above."""
    data_dir = build_week_dirs(OUTPUT_FOLDER)
    fig_dir = build_fig_dirs(OUTPUT_FOLDER)
    
    df_ohlcv = pd.read_csv('./data/results/clean_data/stage_2_crypto_data.csv')
    df_news = pd.read_csv('./data/results/clean_data/stage_2_news_clean.csv')
    
    # --- Stage 3s: News sentiment scoring ---
    df_sentiment = stage_3_sentiment_scores(
        df=df_news,
        api_key=API_KEY,
        data_dir=data_dir,
        filename="stage_3_news_sentiment.csv",
        use_hf_api=True
    )
    
    # uncomment the code below if stage 3A already complete, to save on querying
    # from CoinDesk API again (due to request limits)
    # df_sentiment = pd.read_csv('./data/results/clean_data/stage_3_news_sentiment.csv')
    
    # --- Stage 3b: News sentiment scoring ---
    df_merged = merge_sentiment_ohlcv(
        df_ohlcv=df_ohlcv,
        df_sentiment=df_sentiment,
        data_dir=data_dir,
        filename="stage_3_merged.csv",
        fill_method="zero",  # Options: "zero", "ffill", or "none"
    )
    
    df_regress_results = stage3_model_predict_and_strategies(
        df_merged=df_merged,
        features=[
            "open", "high", "low", "close", "usd_volume", "crypto_volume",
            "return_2w", "return_4w", "usd_v_7d", "usd_v_14d", 
            "momentum_14", "volatility_14", "VaR_5", "strev_weekly",
            "sentiment_index",
        ],
        data_dir=data_dir,
        filename="stage_3_regress_results.csv",
        target_col="return",
    )
    print(df_regress_results.head());
    
    stage3_returns_strategies_plots(
        df_regress_results=df_regress_results,
        fig_dir=fig_dir
    )
    
    export_txt_tables(
        df_regress_results=df_regress_results,
        data_dir=data_dir
    )
    
    print("Done!")
    print("Data ->", data_dir.resolve())

if __name__ == "__main__":
    main()