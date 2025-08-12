from datetime import datetime
import pandas as pd

from stage1_2 import (
    get_top_coins,
    stage_1_etl,
    stage_2_ohlcv_feature_engineering,
    stage2_news_clean_text,
    build_week_dirs,
)

# ---------------------------------------------------------------------------
# User-adjustable inputs -----------------------------------------------------
API_KEY = ""                           # required
PAGES = [1]                            # which pages of the top-list to pull
TOP_LIMIT = 50                         # coins per page
HISTORY_LIMIT = 200                    # days of history per coin
CURRENCY = "USD"                       # quote currency
YEARS_AGO = 0                          # no. years to query articles from
MONTHS_AGO = 6                         # no. months to query articles from
DAYS_AGO = 0                           # no. days to query articles from
OUTPUT_FOLDER = "data"                 # root for outputs
# ---------------------------------------------------------------------------


def main() -> None:
    """Run Stage 1 then Stage 2 with the constants above."""
    data_dir = build_week_dirs(OUTPUT_FOLDER)

    end_dt = datetime.now()
    start_dt = end_dt.replace(
        hour=0, minute=0, second=0, microsecond=0
    ) - pd.DateOffset(years=YEARS_AGO, months=MONTHS_AGO, days=DAYS_AGO)

    # --- Get top coins ---
    coins = get_top_coins(
        api_key=API_KEY,
        pages=PAGES,
        limit=TOP_LIMIT,
        sort_by="CIRCULATING_MKT_CAP_USD",
    )

    # --- Stage 1: Raw Data Download ---
    df_prices, df_news = stage_1_etl(
        coins=coins,
        api_key=API_KEY,
        start_dt=start_dt,
        end_dt=end_dt,
        data_dir=data_dir,
        history_limit=HISTORY_LIMIT,
        currency=CURRENCY,
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


if __name__ == "__main__":  # required on Windows
    main()
