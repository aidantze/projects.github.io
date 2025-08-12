# TILT.ai — Crypto Market News Portfolio Optimisation Investment Product

This project is a cryptocurrency portfolio optimisation service, designed to rebalance holdings in response to abnormal sentiment shifts detected in crypto-related news. It retrieves OHLCV and news data from the CryptoCompare (CoinDesk) API, processes the data through a feature engineering and text pipeline, and performs VADER sentiment analysis and data aggregation to recommend optimal portfolio weights for a user's Crypto portfolio, before returning the results for visualisation in a simple web frontend.

The project is divided into four stages based on the Data Factory Floor for financial market data analytics:
1. Stage 1: data collection, retrieval and preprocessing
2. Stage 2: feature engineering and text processing/cleaning
3. Stage 3: model design and analytics
4. Stage 4: implementation

---

## Project Structure

```
crypto-feature-engineering/
├── src/
│ ├── data/
│ │ ├── results/
│ │ │ ├── clean_data/
│ │ │ │ ├── <SYMBOL>_ohlcv.csv
│ │ │ │ ├── stage_1_news_raw.csv
│ │ │ │ ├── stage_2_crypto_data.csv
│ │ │ │ ├── stage_2_common_words.csv
│ │ └─└─└── stage_2_news_clean.csv
│ │
│ ├── stage1_2.py
│ └── run_stage1_2.py
│
├── utils/
│ └── config.py
│
└── README.md
```

---

## Data Design and Analysis — Stages 1 and 2

This section performs data collection from the CoinDesk API, processes the data, cleans the data and performs feature engineering. It outputs a series of CSV files

### Preparation

Firstly, ensure you create a fork of the repository and clone it.

Be sure you have Python 3.10+ installed.

You will likely be required to create an account in [CoinDesk](https://developers.coindesk.com/documentation/data-api/introduction) in order to get an API key, which you will need to actually collect the data.

Place your API key inside `run_stage1_2.py` here. You may also like to configure these other user-defined variables below:
```py
# ---------------------------------------------------------------------------
# User-adjustable inputs -----------------------------------------------------
API_KEY = "<place-your-API-key-here>"  # required
PAGES = [1]                            # which pages of the top-list to pull
TOP_LIMIT = 50                         # coins per page
HISTORY_LIMIT = 2000                   # days of history per coin
CURRENCY = "USD"                       # quote currency
YEARS_AGO = 0                          # no. years to query articles from
MONTHS_AGO = 3                         # no. months to query articles from
DAYS_AGO = 0                           # no. days to query articles from
WEEK_FOLDER = "data"                   # root for outputs
# ---------------------------------------------------------------------------
```

Once these are configured, you may also need to import the following libraries:
- pandas
- numpy
- datetime and time
- tqdm
- re
- bs4 (for BeautifulSoup)
- nltk (for stopwords, word_tokenise and WordNetLemmatizer)

The `nltk` libraries also require an additional download of the following packages, which is provided in the code itself, but only needs to be run once. After the first run, you can comment these lines out.
```py
# nltk downloads (need only run these once)
nltk.download('punkt_tab')
nltk.download('wordnet')
nltk.download("stopwords")
```

After that, feel free to run `run_stage1_2.py` to carry out stages 1 and 2 of the project:
`python run_stage1_2.py`

_Note: after running the code once to collect the data into csv files, you don't need to run this part of the code again. You can then uncomment and comment some lines in `run_stage1_2.py`, as demonstrated below._

**From this...**
```py
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
```

**...to this...**
```py
# --- Get top coins ---
# coins = get_top_coins(
#     api_key=API_KEY,
#     pages=PAGES,
#     limit=TOP_LIMIT,
#     sort_by="CIRCULATING_MKT_CAP_USD",
# )

# # --- Stage 1: Raw Data Download ---
# df_prices, df_news = stage_1_etl(
#     coins=coins,
#     api_key=API_KEY,
#     start_dt=start_dt,
#     end_dt=end_dt,
#     data_dir=data_dir,
#     history_limit=HISTORY_LIMIT,
#     currency=CURRENCY,
# )

# uncomment the code below if stage 1 already complete, to save on querying
# from CoinDesk API again (due to request limits)
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
df_news = pd.read_csv("./data/results/clean_data/stage_1_news_raw.csv");

# --- Stage 2: Feature Engineering ---
stage_2_ohlcv_feature_engineering(
    tidy_prices=df_prices,
    data_dir=data_dir,
)

stage2_news_clean_text(
    df_raw=df_news,
    data_dir=data_dir
)
```

You may also like to tweak the following variables in `stage1_2.py`:
```py
# Macroeconomic phrases to find in articles
MACRO_KEYWORDS = [
    "interest rate", "inflation", "regulation", "SEC", "cbdc", ...
]

# Spam phrases to filter out
PROMO_STRINGS = [
    "Are You Chasing New Coins?", ...
]
```

### Outputs

You can find all results inside src/data/results/clean_data/ folder:
- `<SYMBOL>_ohlcv.csv` – historical OHLCV data for each coin
- `stage_1_news_raw.csv` – all crypto news data
- `stage_2_crypto_data.csv` – combined historical OHLCV data for all coins after feature engineering
- `stage_2_common_words.csv` – common words found in all news articles and their frequencies
- `stage_2_news_clean.csv` – cleaned crypto news data

---
## Model Design and Implementation — Stages 3 and 4

TBA...

## Authors & Acknowledgements

This project was developed as part of an academic FinTech project course at UNSW, FINS3645. Inspired by techniques in financial NLP, signal processing, and econometrics, all crypto feature engineering using hybrid market and news data was written by myself but various parts taken from or inspired by Dr Alex Dickerson, lecturer in charge and developer of the python for portfolio optimisation library. 

### License

MIT License — feel free to use and adapt for educational or research purposes.