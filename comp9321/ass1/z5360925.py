#! /usr/bin/env python3
# -*- coding: utf-8 -*-

# Third-party libraries
# NOTE: You may **only** use the following third-party libraries:
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd 
from thefuzz import fuzz
from thefuzz import process
# NOTE: It isn't necessary to use all of these to complete the assignment, 
# but you are free to do so, should you choose.

# Standard libraries
# NOTE: You may use **any** of the Python 3.11 or Python 3.12 standard libraries:
# https://docs.python.org/3.11/library/index.html
# https://docs.python.org/3.12/library/index.html
from pathlib import Path
# ... import your standard libraries here ...


######################################################
# NOTE: DO NOT MODIFY THE LINE BELOW ...
######################################################
studentid = Path(__file__).stem

######################################################
# NOTE: DO NOT MODIFY THE FUNCTION BELOW ...
######################################################
def log(question, output_df, other):
    print(f"--------------- {question}----------------")

    if other is not None:
        print(question, other)
    if output_df is not None:
        df = output_df.head(5).copy(True)
        for c in df.columns:
            df[c] = df[c].apply(lambda a: a[:20] if isinstance(a, str) else a)

        df.columns = [a[:10] + "..." for a in df.columns]
        print(df.to_string())


######################################################
# NOTE: YOU MAY ADD ANY HELPER FUNCTIONS BELOW ...
######################################################


def write_csv(df, filename, index=False):
    """
    Save the dataframe df to a csv file (override if exists)
    By default, saves without the index
    """
    df.to_csv(filename, sep=',', encoding='utf-8', index=index)


def isSanitised(df):
    """
    Returns True if dataframe is sanitised, false otherwise
    """
    return replaceCols(df, ' ', '_').equals(df)


def lowerCols(df):
    """
    Convert all col names to lowercase
    """
    df.columns = [x.lower() for x in df.columns]
    return df


def replaceCols(df, str1, str2):
    """
    Replace all instances of str1 in all col names with str2
    """
    df.columns = [x.replace(str1, str2) for x in df.columns]
    return df


def replaceAll(df, str1, str2):
    """
    Replace all instances of str1 in data with str2
    """
    df = replaceCols(df, str1, str2)
    df = df.replace(str1, str2)
    return df


def experienceMap(value):
    """
    Used to map experience level to rating in jobs_df
    """
    if value == "EN":
        return 1
    elif value == "MI":
        return 2
    elif value == "SE":
        return 3
    elif value == "EX":
        return 4


def question2a(cost_csv, df):
    """
    Continuation of question 2 after file checking
    """
    df = lowerCols(df)
    df = replaceCols(df, ' ', '_')
    # save as CSV file
    write_csv(df, cost_csv)


def question3a(currency_csv, df):
    """
    Continuation of question 3 after file checking
    """
    # drop "Nearest actual exchange rate" and top header row
    df = df.drop("Nearest actual exchange rate", level=0, axis=1)
    df = df.droplevel(0, axis=1)

    # replace non-breaking spaces
    df = replaceAll(df, u"\u00A0", ' ')

    # remove 30 Jun 23 col and rename 31 Dec 23 col to "rate"
    df = df.drop("30 Jun 23", axis=1)
    df = df.rename({"31 Dec 23": "rate"}, axis=1)

    # convert all cols to lowercase
    df = lowerCols(df)

    # save as CSV file
    write_csv(df, currency_csv)
    return df


def question4a(country_csv, df):
    """
    Continuation from question 4 after file checking
    """
    # Remove columns Year, ccTLD and Notes
    colsToDrop = ["Year", "ccTLD", "Notes"]
    df = df.drop(colsToDrop, axis=1)

    # Rename columns to country and code
    df = df.rename({"Country name (using title case)": "country",
                    "Code": "code"}, axis=1)

    # save as CSV file
    write_csv(df, country_csv)
    return df


def fuzzy_match(
    df_left, df_right, column_left, column_right, threshold=90, limit=1
):
    """
    This function was taken from:
    https://stackoverflow.com/questions/13636848/is-it-possible-to-do-fuzzy-match-merge-with-python-pandas/56315491#56315491
    It performs the matching to assist in the merge in question_10
    """

    # Creates a series with id from df_left and column name _column_left_, with _limit_ matches per item
    series_matches = df_left[column_left].apply(
        lambda x: process.extract(x, df_right[column_right], limit=limit)
    )

    # Convert matches to a tidy dataframe
    df_matches = series_matches.to_frame()
    df_matches = df_matches.explode(column_left)     # Convert list of matches to rows
    df_matches[
        ['match_string', 'match_score', 'df_right_id']
    ] = pd.DataFrame(df_matches[column_left].tolist(), index=df_matches.index)       # Convert match tuple to columns
    df_matches.drop(column_left, axis=1, inplace=True)      # Drop column of match tuples

    # Reset index, as in creating a tidy dataframe we've introduced multiple rows per id,
    # so that no longer functions well as the index
    if df_matches.index.name:
        index_name = df_matches.index.name     # Stash index name
    else:
        index_name = 'index'        # Default used by pandas
    df_matches.reset_index(inplace=True)
    # The previous index has now become a column: rename for ease of reference
    df_matches.rename(columns={index_name: 'df_left_id'}, inplace=True)

    # Drop matches below threshold
    df_matches.drop(
        df_matches.loc[df_matches['match_score'] < threshold].index,
        inplace=True
    )
    return df_matches


def createPivotMean(df, value, index, column):
    """
    Creates a pivot table with the given dataframe
    """
    df = pd.pivot_table(df,
                        values=[value],
                        index=[index],
                        columns=[column],
                        aggfunc="mean",
                        fill_value=0)
    # convert to integers
    df = df.astype(int)
    print(df.head(10))
    return df


######################################################
# QUESTIONS TO COMPLETE BELOW ...
######################################################

######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_1(jobs_csv):
    """Read the data science jobs CSV file into a DataFrame.

    See the assignment spec for more details.

    Args:
        jobs_csv (str): Path to the jobs CSV file.

    Returns:
        DataFrame: The jobs DataFrame.
    """
    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    df = pd.read_csv(jobs_csv)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 1", output_df=df, other=df.shape)
    return df



######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_2(cost_csv, cost_url):
    """Read the cost of living CSV into a DataFrame.  If the CSV file does not 
    exist, scrape it from the specified URL and save it to the CSV file.

    See the assignment spec for more details.

    Args:
        cost_csv (str): Path to the cost of living CSV file.
        cost_url (str): URL of the cost of living page.

    Returns:
        DataFrame: The cost of living DataFrame.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    try:
        # read cost of living csv
        df = pd.read_csv(cost_csv)
    except FileNotFoundError:
        # scrape from specified URL
        df = pd.read_html(cost_url)[0]
        question2a(cost_csv, df)
    else:
        if ((not isSanitised(df)) | (not lowerCols(df).equals(df))
        ):
            question2a(cost_csv, df)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 2", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_3(currency_csv, currency_url):
    """Read the currency conversion rates CSV into a DataFrame.  If the CSV 
    file does not exist, scrape it from the specified URL and save it to 
    the CSV file.

    See the assignment spec for more details.

    Args:
        cost_csv (str): Path to the currency conversion rates CSV file.
        cost_url (str): URL of the currency conversion rates page.

    Returns:
        DataFrame: The currency conversion rates DataFrame.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    try:
        # read currency exchange csv
        df = pd.read_csv(currency_csv)
    except FileNotFoundError:
        # scrape from specified URL
        df = pd.read_html(currency_url)[0]
        df = question3a(currency_csv, df)
    else:
        if ((not isSanitised(df)) | (not lowerCols(df).equals(df))
        ):
            df = question3a(currency_csv, df)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 3", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_4(country_csv, country_url):
    """Read the country codes CSV into a DataFrame.  If the CSV file does not 
    exist, it will be scrape the data from the specified URL and save it to the 
    CSV file.

    See the assignment spec for more details.

    Args:
        cost_csv (str): Path to the country codes CSV file.
        cost_url (str): URL of the country codes page.

    Returns:
        DataFrame: The country codes DataFrame.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    try:
        # read currency exchange csv
        df = pd.read_csv(country_csv)
    except FileNotFoundError:
        # scrape from specified URL
        df = pd.read_html(country_url)[0]
        df = question4a(country_csv, df)
    else:
        if ((not isSanitised(df)) | (not lowerCols(df).equals(df))
        ):
            df = question4a(country_csv, df)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 4", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_5(jobs_df):
    """Summarise some dimensions of the jobs DataFrame.

    See the assignment spec for more details.

    Args:
        jobs_df (DataFrame): The jobs DataFrame returned in question 1.

    Returns:
        DataFrame: The summary DataFrame.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    # convert columns of jobs_df to index in new dataframe
    newCols = ["observations", "distinct", "missing"]
    size = jobs_df.shape[0]
    data = []
    for c in jobs_df:
        observations = jobs_df[c].count()
        missing = size - observations
        distinct = jobs_df[c].nunique()
        data.append([observations, distinct, missing])

    df = pd.DataFrame(data, index=jobs_df.columns, columns=newCols)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 5", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_6(jobs_df):
    """Add an experience rating column to the jobs DataFrame.

    See the assignment spec for more details.

    Args:
        jobs_df (DataFrame): The jobs DataFrame returned in question 1.

    Returns:
        DataFrame: The jobs DataFrame with the experience rating column added.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    df = jobs_df
    df['experience_rating'] = df['experience_level'].apply(experienceMap)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 6", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_7(jobs_df, country_df):
    """Merge the jobs and country codes DataFrames.

    See the assignment spec for more details.

    Args:
        jobs_df (DataFrame): The jobs DataFrame returned in question 6.
        country_df (DataFrame): The country codes DataFrame returned in 
                                question 4.

    Returns:
        DataFrame: The merged DataFrame.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    df = pd.merge(jobs_df, country_df, how='left', left_on='employee_residence', right_on='code')
    df = df.drop("code", axis=1)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 7", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_8(jobs_df, currency_df):
    """Add an Australian dollar salary column to the jobs DataFrame.

    See the assignment spec for more details.

    Args:
        jobs_df (DataFrame): The jobs DataFrame returned in question 7.
        currency_df (DataFrame): The currency conversion rates DataFrame 
                                 returned in question 3.

    Returns:
        DataFrame: The jobs DataFrame with the Australian dollar salary column
                   added.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    # Filter only 2023 work year
    df = jobs_df.query("work_year == 2023")
    print(df.tail(10))

    # Convert to Australian dollar
    tmp_df = currency_df.set_index('country')
    rate = tmp_df.loc['United States', 'rate']
    rate = 1 / float(rate)  # we go from USD to AUD

    df['salary_in_aud'] = df['salary_in_usd'].apply(lambda x : x * rate)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 8", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_9(cost_df):
    """Re-scale the cost of living DataFrame to be relative to Australia.

    See the assignment spec for more details.

    Args:
        cost_df (DataFrame): The cost of living DataFrame returned in question 2.

    Returns:
        DataFrame: The re-scaled cost of living DataFrame.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    # remove all unwanted columns
    colsToDrop = list(cost_df.columns)
    colsToDrop.remove('country')
    colsToDrop.remove('cost_of_living_plus_rent_index')
    df = cost_df.drop(colsToDrop, axis=1)

    # recalculate cost_of_living_plus_rent_index
    # round to 1 d.p.
    tmp_df = df.set_index('country')
    ausIndex = tmp_df.loc['Australia', 'cost_of_living_plus_rent_index']

    df['cost_of_living_plus_rent_index'] = df['cost_of_living_plus_rent_index'].apply(
        lambda x : round(x / float(ausIndex) * 100, 1)
    )

    # sort dataframe by increasing cost_of_living_plus_rent_index
    df = df.sort_values(by=['cost_of_living_plus_rent_index'])
    
    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 9", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_10(jobs_df, cost_df):
    """Merge the jobs and cost of living DataFrames.

    See the assignment spec for more details.

    Args:
        jobs_df (DataFrame): The jobs DataFrame returned in question 8.
        cost_df (DataFrame): The cost of living DataFrame returned in question 9.

    Returns:
        DataFrame: The merged DataFrame.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    df_matches = fuzzy_match(
        jobs_df,
        cost_df,
        'country',
        'country',
        threshold=90,
        limit=1
    )

    df = jobs_df.merge(
        df_matches,
        how='left',
        left_index=True,
        right_on='df_left_id'
    ).merge(
        cost_df,
        how='left',
        left_on='df_right_id',
        right_index=True,
        suffixes=['_df1', '_df2']
    )
    df.set_index('df_left_id', inplace=True)

    # drop unnecessary columns and missing values
    colsToDrop = ["match_string",
                  "match_score",
                  "df_right_id",
                  "country_df2"]
    df = df.drop(colsToDrop, axis=1)
    df = df.dropna()
    df = df.rename({"country_df1": "country",
                    "cost_of_living_plus_rent_index": "cost_of_living"},
                   axis=1)

    write_csv(df, "new_ds_jobs.csv")

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 10", output_df=df, other=df.shape)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_11(jobs_df):
    """Create a pivot table of the average salary in AUD by country and 
    experience rating.

    See the assignment spec for more details.

    Args:
        jobs_df (DataFrame): The jobs DataFrame returned in question 10.

    Returns:
        DataFrame: The pivot table.
    """

    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    df = createPivotMean(jobs_df, "salary_in_aud", "country", "experience_rating")

    # sort dataframe in a specific way
    df = df.sort_values(by=[('salary_in_aud', 1),
                            ('salary_in_aud', 2),
                            ('salary_in_aud', 3),
                            ('salary_in_aud', 4)],
                        ascending=False)

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    log("QUESTION 11", output_df=None, other=df)
    return df


######################################################
# NOTE: DO NOT MODIFY THE FUNCTION SIGNATURE BELOW ...
######################################################
def question_12(jobs_df):
    """Create a visualisation of data science jobs to help inform a decision
    about where to live, based (minimally) on salary and cost of living.

    See the assignment spec for more details.

    Args:
        jobs_df (DataFrame): The jobs DataFrame returned in question 10.
    """
    ######################################################
    # TODO: Your code goes here ...
    ######################################################

    fig = plt.figure()
    f, axes = plt.subplots(2, 2, figsize=(40, 30))

    # order below is designed for display convenience
    ax4 = axes[0][0]
    ax1 = axes[0][1]
    ax2 = axes[1][0]
    ax3 = axes[1][1]

    # df1 - Average salary_in_aud VS cost of living VS country
    df1 = jobs_df
    colsToDrop = list(jobs_df.columns)
    colsToDrop.remove('salary_in_aud')
    colsToDrop.remove('country')
    colsToDrop.remove('cost_of_living')
    df1 = df1.drop(colsToDrop, axis=1)

    df1 = df1.groupby("country")
    df1 = df1.agg({"salary_in_aud": "mean", "cost_of_living": "mean"})
    df1 = df1.sort_values("cost_of_living", ascending=False)

    x = df1['cost_of_living']
    y = df1['salary_in_aud']
    labels = df1.index  # country
    top10 = []
    for i in range(len(x)):
        if labels[i] == "Australia":
            ax1.scatter(x=x[i], y=y[i], c="olivedrab", s=100, label=labels[i], alpha=1, edgecolor="green", zorder=3)
            top10.append(labels[i])
        elif x[i]/y[i] < 0.0004: # high ratio
            ax1.scatter(x=x[i], y=y[i], c="limegreen", s=100, label=labels[i], alpha=1, edgecolor="green", zorder=3)
            top10.append(labels[i])
        else:
            ax1.scatter(x=x[i], y=y[i], c="lime", s=100, label=labels[i], alpha=0.5, edgecolor="green", zorder=3)

    for label, x, y in zip(labels, x, y):
        if label in top10:
            ax1.annotate(label, xy=(x, y), fontsize=16,
                         textcoords='offset points', xytext=(10, -6))

    ax1.grid(True, zorder=0)
    ax1.set_title("Countries in Salary to Cost of Living Ratio", size=36)
    ax1.set_xlabel("Cost of Living Index", fontsize=24)
    ax1.set_ylabel("Average Salary (AUD)", fontsize=24)
    ax1.tick_params(axis="x", labelsize=16)
    ax1.tick_params(axis="y", labelsize=16)

    # df2 - Average salary_in_aud VS country VS experience_rating
    df2 = question_11(jobs_df)

    x = df2.index  # country
    y = df2['salary_in_aud']
    labels = df2["salary_in_aud"].columns

    colors = ["yellow", "gold", "darkorange", "red"]
    for j in range(len(x)):
        for i, c in enumerate(colors):
            if y[labels[i]][j] == 0 and j != 0:
                continue
            ax2.scatter(x=x[j], y=y[labels[i]][j], c=c, s=100, label=c, alpha=0.5, edgecolor='grey', zorder=3)

    ax2.grid(True, zorder=0)
    ax2.set_title("Average Salary per Country and Experience Rating", size=36)
    ax2.set_xlabel("Country", fontsize=24)
    ax2.set_ylabel("Average Salary (AUD)", fontsize=24)
    ax2.set_xticklabels(x, rotation=45, fontsize=12, ha="right", rotation_mode="anchor")
    ax2.tick_params(axis="y", labelsize=16)
    leg2 = ax2.legend([1, 2, 3, 4], loc='upper left', fontsize=24)
    leg2.set_title('Exp. Rating', prop={'size': 24})

    # df3 - Average salary_in_aud VS job_title VS experience_rating
    df3 = createPivotMean(jobs_df, "salary_in_aud", "job_title", "experience_rating")
    df3 = df3.sort_values(by=[('salary_in_aud', 1),
                            ('salary_in_aud', 2),
                            ('salary_in_aud', 3),
                            ('salary_in_aud', 4)],
                        ascending=False)

    x = df3.index   # job title
    y = df3['salary_in_aud']
    labels = df3["salary_in_aud"].columns

    colors = ["cyan", "dodgerblue", "blue", "darkorchid"]
    for j in range(len(x)):
        for i, c in enumerate(colors):
            if y[labels[i]][j] == 0 and j != 0:
                continue
            ax3.scatter(x=x[j], y=y[labels[i]][j], c=c, s=100, label=c, alpha=0.5, edgecolor='grey', zorder=3)

    ax3.grid(True, zorder=0)
    ax3.set_title("Average Salary per Job Title and Experience Rating", size=36)
    ax3.set_xlabel("Job Title", fontsize=24)
    ax3.set_ylabel("Average Salary (AUD)", fontsize=24)
    ax3.set_xticklabels(x, rotation=45, fontsize=10, ha="right", rotation_mode="anchor")
    ax3.tick_params(axis="y", labelsize=16)
    leg3 = ax3.legend([1, 2, 3, 4], loc='upper left', fontsize=24)  # title="Exp. Rating"
    leg3.set_title('Exp. Rating', prop={'size': 24})

    # df4 - Average salary_in_aud VS job title
    df4 = jobs_df
    colsToDrop = list(jobs_df.columns)
    colsToDrop.remove('salary_in_aud')
    colsToDrop.remove('job_title')
    df4 = df4.drop(colsToDrop, axis=1)

    df4 = df4.groupby("job_title")
    df4 = df4.agg({"salary_in_aud": "mean"})
    df4 = df4.sort_values("salary_in_aud", ascending=True)
    df4 = df4.tail(10)

    y = df4.index
    w = df4['salary_in_aud']

    df4_plt = ax4.barh(y=y,
                       width=w,
                       align='center',
                       color='magenta',
                       edgecolor='mediumorchid',
                       zorder=3)
    ax4.grid(True, axis='x', zorder=0)
    ax4.set_title("Top 10 Jobs with Highest Average Salary", size=36)
    ax4.set_xlabel("Average Salary (AUD)", fontsize=24)
    ax4.set_ylabel("Job Title", fontsize=24)
    ax4.tick_params(axis="x", labelsize=16)
    ax4.tick_params(axis='y', rotation=45, labelsize=12)
    df4_plt[-1].set_color('mediumorchid')

    plt.suptitle('Best Markets to apply for Jobs', fontsize=96)
    # plt.show()

    ######################################################
    # NOTE: DO NOT MODIFY THE CODE BELOW ...
    ######################################################
    plt.savefig(f"{studentid}-Q12.png")


######################################################
# NOTE: DO NOT MODIFY THE MAIN FUNCTION BELOW ...
######################################################
if __name__ == "__main__":
    # data ingestion and cleaning
    df1 = question_1("ds_jobs.csv")
    df2 = question_2("cost_of_living.csv", 
                     "https://www.cse.unsw.edu.au/~cs9321/24T1/ass1/cost_of_living.html")
    df3 = question_3("exchange_rates.csv", 
                     "https://www.cse.unsw.edu.au/~cs9321/24T1/ass1/exchange_rates.html")
    df4 = question_4("country_codes.csv", 
                     "https://www.cse.unsw.edu.au/~cs9321/24T1/ass1/country_codes.html")

    # data exploration
    df5 = question_5(df1.copy(True))

    # data manipulation
    df6 = question_6(df1.copy(True))
    df7 = question_7(df6.copy(True), df4.copy(True))
    df8 = question_8(df7.copy(True), df3.copy(True))
    df9 = question_9(df2.copy(True))
    df10 = question_10(df8.copy(True), df9.copy(True))
    df11 = question_11(df10.copy(True))
    print(df11.head(10))

    # data visualisation
    # question_12(df10.copy(True))
