""" zid_project2_part8.py

Helper functions and methods used to answer the questions in part 8

"""
import os
import config as cfg
from zid_project2_main import portfolio_main


def create_csv_files():
    """
    Call portfolio_main, separates dict_ret into its two dataframes
    Returns
    -------

    """
    dict_ret, df_cha, df_portfolios = portfolio_main(
        cfg.TICMAP.keys(),
        '2000-12-29',
        '2021-08-31',
        'vol',
        ['Daily', ],
        3
    )
    daily_ret = dict_ret['Daily']
    monthly_ret = dict_ret['Monthly']

    daily_ret.to_csv(os.path.join(cfg.DATADIR, 'daily_ret.csv'))
    monthly_ret.to_csv(os.path.join(cfg.DATADIR, 'monthly_ret.csv'))
    df_cha.to_csv(os.path.join(cfg.DATADIR, 'df_cha.csv'))
    df_portfolios.to_csv(os.path.join(cfg.DATADIR, 'EW_LS_pf_df.csv'))
    return daily_ret, monthly_ret, df_cha, df_portfolios


if __name__ == "__main__":
    create_csv_files()