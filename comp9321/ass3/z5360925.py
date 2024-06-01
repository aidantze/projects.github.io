"""
z5360925.py
Predicts the age of the policyholder using regression analysis.
Predicts if a policyholder will lodge a claim using classification.

"""
# <<< IMPORTS >>>
# === Other modules ===
import pandas as pd
import numpy as np
import sys
from matplotlib import pyplot as plt
import seaborn as sns
from scipy.stats import zscore

# === imblearn ===
from imblearn.over_sampling import RandomOverSampler
from imblearn.under_sampling import RandomUnderSampler
from imblearn.over_sampling import SMOTE

# === sklearn fundamentals ===
from sklearn.metrics import mean_squared_error, f1_score
from sklearn.metrics import precision_score, accuracy_score, recall_score, confusion_matrix
from sklearn.feature_selection import SelectKBest
from sklearn.feature_selection import mutual_info_regression
from sklearn.model_selection import cross_val_score, KFold
from sklearn.preprocessing import StandardScaler

# === Regression and Classification models ===

# Linear Regression
from sklearn.linear_model import LinearRegression

# Ridge Classifier
from sklearn.linear_model import RidgeClassifier

# Elastic Net
from sklearn.linear_model import ElasticNet

# Bayesian Ridge
from sklearn.linear_model import BayesianRidge

# XGBoost Regressor
from xgboost.sklearn import XGBRegressor

# Random Forest
from sklearn.ensemble import RandomForestRegressor, RandomForestClassifier

# Lasso
from sklearn.linear_model import Lasso

# Decision Tree
from sklearn.tree import DecisionTreeRegressor

# KNN
from sklearn.neighbors import KNeighborsRegressor, KNeighborsClassifier

# Gradient Boosting
from sklearn.ensemble import GradientBoostingRegressor, GradientBoostingClassifier
from sklearn.ensemble import HistGradientBoostingRegressor, HistGradientBoostingClassifier

# Linear Discriminant Analysis
from sklearn.discriminant_analysis import LinearDiscriminantAnalysis

# Logistic Regression
from sklearn.linear_model import LogisticRegression

# Gaussian Naive Bayes
from sklearn.naive_bayes import GaussianNB

# Decision Tree
from sklearn.tree import DecisionTreeClassifier


# Constants
zid = 'z5360925'


# <<< HELPER FUNCTIONS >>>
# === Data manipulation for cleansing ===
def load_data(train_path, test_path):
    train_df = pd.read_csv(train_path)
    test_df = pd.read_csv(test_path)
    return train_df, test_df


def isSanitised(df):
    """
    Returns True if dataframe is sanitised, false otherwise
    """
    return replaceCols(df, ' ', '_').equals(df) and lowerCols(df).equals(df)


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


def clean(df):
    """
    Clean the dataframe to ensure no format or spelling mistakes by:
    - performing sanity checks
    - dropping unnecessary columns (such as policyID)
    - assigning categorical values to numerical values
        - categorical columns assigned to a dict of dicts which maps the
        numerical values in dataframe to original categorical values
    """
    # sanity check
    if not isSanitised(df):
        df = replaceCols(df, ' ', '_')
        df = lowerCols(df)

    columnsToDisregard = ['unnamed:_0',
                          'age_of_policyholder',
                          'age_of_car',
                          'max_torque',
                          'max_power']

    # drop unwanted columns (hard-coded)
    if 'policy_id' in list(df.columns):
        colsToDrop = ['policy_id']
        for col in colsToDrop:
            df = df.drop(col, axis=1)

    # add new column for age_of_car in months  (hard-coded)
    if 'age_of_car' in list(df.columns):
        carAge = list(df['age_of_car'])
        newDates = []
        for w in carAge:
            w = w.split()
            if len(w) == 4:
                newDates.append(int(w[0]) * 12 + int(w[2]))
            elif w[1] == 'months' and w[0] != 0:
                newDates.append(int(w[0]))
            elif w[1] == 'years':
                newDates.append(int(w[0]) * 12)
        df['age_of_car_in_months'] = newDates

    # separate max_torque and max_power to two separate columns representing each unit
    if 'max_torque' in list(df.columns):
        maxTorque = list(df['max_torque'])
        torque = []
        torque_rpm = []
        for t in maxTorque:
            t = t.strip('rpm').split('Nm@')
            torque.append(t[0])
            torque_rpm.append(t[1])
        df['torque'] = torque
        df['torque_rpm'] = torque_rpm

    if 'max_power' in list(df.columns):
        maxPower = list(df['max_power'])
        power = []
        power_rpm = []
        for t in maxPower:
            t = t.strip('rpm').split('bhp@')
            power.append(float(t[0]))
            power_rpm.append(float(t[1]))
        df['power'] = power
        df['power_rpm'] = power_rpm

    # assign categorical values to numeric
    categories = {}
    colTypes = df.dtypes.to_list()
    # assumption: order of list of columns same as colTypes
    for t in range(len(colTypes)):
        if colTypes[t] == "object":
            colName = list(df.columns)[t]
            # filter out the index and policy_id
            if colName in columnsToDisregard:
                continue
            # colValues = list(df[colName])
            valuesMap = {}

            # get only unique values and add to dict
            uniques = df[colName].unique().tolist()
            i = 0
            for u in uniques:
                valuesMap[i] = u
                i += 1

            # map every value to new dict value in one go
            for k, v in valuesMap.items():
                df[colName] = df[colName].replace(v, k)

            categories[colName] = valuesMap

    return df, categories


def df_split(train_df, test_df, col):
    """
    Split test and train data into x and y, where x is all other columns
    and y is the column we are trying to predict, col
    :param train_df:
    :param test_df:
    :return:
    """

    x_train = train_df.drop(col, axis=1).values
    y_train = train_df[col].values
    x_test = test_df.drop(col, axis=1).values
    y_test = test_df[col].values
    return x_train, y_train, x_test, y_test


# === Data manipulation for analytics ===
def standardise(df, col):
    """
    Normalises the parameters using StandardScaler()
    We do not want to normalise col: the col to predict

    Also observes the data skewness and adjusts as needed by log transform or inverse transform
    """

    drop_df = df.drop(col, axis=1)
    scaled_features = StandardScaler().fit_transform(drop_df.values)
    scaled_df = pd.DataFrame(scaled_features, index=drop_df.index, columns=drop_df.columns)
    scaled_df[col] = df[col]

    return scaled_df


def manage_outliers(df, categories, predCol=None):    # categories
    """
    Replaces outliers with the mean value of each column of the dataframe.
    Also replaces NaN values with mean value of each column of the dataframe.
    """
    for col in df.columns:
        if col in categories.keys():
            continue
        if predCol is not None:
            continue

        # Approach: drop NaN values, replace outliers with mean, and put values back
        s = df[col].dropna()
        out = np.where(np.abs(zscore(s)) < 3, s, s.mean())
        df.loc[df[col].notna(), col] = out
        df[col] = df[col].fillna(df[col].mean())
    return df


# === Feature Selection ===
def select_features(X_train, y_train, X_test):
    """
    Uses SelectKBest to select best features
    :param X_train:
    :param y_train:
    :param X_test:
    :return:
    """
    # configure to select all features
    fs = SelectKBest(score_func=mutual_info_regression, k='all')
    # learn relationship from training data
    fs.fit(X_train, y_train)
    # transform train input data
    X_train_fs = fs.transform(X_train)
    # transform test input data
    X_test_fs = fs.transform(X_test)
    return X_train_fs, X_test_fs, fs


def display_features(x_train, y_train, x_test, n):
    """
    Displays features on a bar chart and return top n features, where n is
    a positive integer.

    Each execution of the select_features function returns a different result, so
    I executed this function 10 times and found features with highest average count.
    This was manually performed, due to high runtime and execution of this part
    """
    # feature selection
    X_train_fs, X_test_fs, fs = select_features(x_train, y_train, x_test)
    # what are scores for the features

    features = {}
    for i in range(len(fs.scores_)):
        # print('Feature %d: %f' % (i, fs.scores_[i]))
        features[i] = fs.scores_[i]

    # uncomment the code below to plot the scores on a bar chart
    # plt.bar([i for i in range(len(fs.scores_))], fs.scores_)
    # plt.show()

    top_n = sorted(features.items(), key=lambda f: f[1], reverse=True)[:n]
    # print(top_n)
    return [x[0] for x in top_n]


def get_top_features(train_df, test_df, n, t):
    """
    Filters the top n selected features, repeats this t times and averages the result
    :param train_df:
    :param test_df:
    :param n:
    :return:
    """
    x_train, y_train, x_test, y_test = df_split(train_df, test_df, 'age_of_policyholder')
    total = []
    for i in range(t):
        top_n = display_features(x_train, y_train, x_test, n)
        # print(top_n)
        total.append(top_n)
    total = [x for xs in total for x in xs]
    unique = list(set(total))
    sort_dict = {}
    for i in unique:
        col = list(train_df.columns)[i]
        # print(col, total.count(i))
        sort_dict[col] = total.count(i)
    sort_dict = sorted(sort_dict.items(), key=lambda f: f[1], reverse=True)
    sorted_dict = {x[0]: x[1] for x in sort_dict}
    return sorted_dict


# === Resampling ===
def over_sample(x_train, y_train):
    """
    Over-sampling on the given data for classification
    """
    oversampler = RandomOverSampler(sampling_strategy=0.5)
    x_resampled, y_resampled = oversampler.fit_resample(x_train, y_train)
    return x_resampled, y_resampled


def under_sample(x_train, y_train):
    """
    Under-sampling the given data for classification
    """
    undersampler = RandomUnderSampler(sampling_strategy=0.1)
    x_resampled, y_resampled = undersampler.fit_resample(x_train, y_train)
    return x_resampled, y_resampled


# <<< PART 1 REGRESSION >>>
def age_regression(train_file, test_file):
    """
    Perform linear regression to predict the age of the policyholder

    """
    colToPredict = 'age_of_policyholder'

    # Load data and make a copy
    og_train_df, og_test_df = load_data(train_file, test_file)
    train_df = og_train_df.copy()
    test_df = og_test_df.copy()

    policy_ids = test_df['policy_id']

    # Clean data
    train_df, train_categories = clean(train_df)
    test_df, test_categories = clean(test_df)
    if train_categories.keys() != test_categories.keys():
        print("Columns between test and train data do not match")
        exit(1)

    columnsToDisregard = ['unnamed:_0',
                          'age_of_car',
                          'max_torque',
                          'max_power']
    train_df = train_df.filter(items=[
        x for x in train_df.columns if x not in columnsToDisregard
    ])
    test_df = test_df.filter(items=[
        x for x in test_df.columns if x not in columnsToDisregard
    ])

    # Feature selection (uncomment the code below to see how I select features)
    # Method 1: using SelectKBest
    # sort_dict = get_top_features(train_df, test_df, 10, 10)
    # print(sort_dict)
    # exit()

    # Method 2: correlation heatmap
    # plt.figure(figsize=(8, 12))
    # heatmap = sns.heatmap(train_df.corr()[[colToPredict]].sort_values(by=colToPredict, ascending=False),
    #                       vmin=-1, vmax=1, annot=True, cmap='BrBG')
    # heatmap.set_title(f'Features Correlating with {colToPredict}')  # fontdict={'fontsize': 18}, pad=16
    # plt.show()
    # exit()

    topFeatures = ['policy_tenure',
                   'model',
                   'displacement',
                   'gross_weight',
                   'power',
                   'area_cluster',
                   'engine_type',
                   'length']
    # other helpful columns:
    # 'population_density',
    # 'width'

    # filter train and test data to only include topFeatures
    train_df = train_df.filter(items=topFeatures + [colToPredict])
    test_df = test_df.filter(items=topFeatures + [colToPredict])

    # standardise data
    # this was not helpful in reducing MSE whatsoever :(
    # train_df = standardise(train_df, colToPredict)
    # test_df = standardise(test_df, colToPredict)

    # Replace outliers and NaN values
    train_df = manage_outliers(train_df, train_categories)
    test_df = manage_outliers(test_df, test_categories)

    # Split the datasets
    x_train, y_train, x_test, y_test = df_split(train_df, test_df, colToPredict)

    # Attempt all regression models and select the lowest mse model
    # regressors = [LinearRegression(),
    #               ElasticNet(),
    #               RandomForestRegressor(),
    #               BayesianRidge(),
    #               Lasso(),
    #               DecisionTreeRegressor(),
    #               KNeighborsRegressor(),
    #               XGBRegressor(),
    #               GradientBoostingRegressor(),
    #               HistGradientBoostingRegressor()]
    regressors = [GradientBoostingRegressor(learning_rate=0.05, n_estimators=92)]

    predData = {}
    predMSE = {}
    for model in regressors:
        model.fit(x_train, y_train)

        # predict the test set and calculate mean squared error
        y_pred = model.predict(x_test)
        mse = mean_squared_error(y_test, y_pred)

        print(f"Model, {model}")
        # for i in range(len(y_test)):
        #     print("Expected:", y_test[i], "Predicted:", y_pred[i])
        print("Mean squared error: %.2f" % mse)

        predMSE[model] = mse
        predData[model] = {
            "policy_id": policy_ids,
            "age": [round(y, 1) for y in y_pred]
        }

        # Summary of top-performing regression models: each value is the average
        # GradientBoostingRegressor() gives mse = 91.07
        # HistGradientBoostingRegressor() gives mse = 91.87
        # LinearRegression() gives mse = 92.42
        # ElasticNet() gives mse = 92.40
        # Lasso() gives mse = 92.78
        # BayesianRidge() gives mse = 92.42

    model = min(predMSE, key=predMSE.get)
    newData = predData[model]
    # print(f"Choosing {model}")

    # add predicted to dataframe and return csv
    pred_df = pd.DataFrame(newData)
    pred_df = pred_df.set_index('policy_id')
    pred_df.to_csv(zid + '.PART1.output.csv')


# <<< PART 2 CLASSIFICATION >>>
def claim_classification(train_file, test_file):
    """
    Use classification to predict if a policyholder will lodge a claim.
    :return:
    """
    colToPredict = 'is_claim'

    # Load data and make a copy
    og_train_df, og_test_df = load_data(train_file, test_file)
    train_df = og_train_df.copy()
    test_df = og_test_df.copy()

    policy_ids = test_df['policy_id']

    # Clean data
    train_df, train_categories = clean(train_df)
    test_df, test_categories = clean(test_df)
    if train_categories.keys() != test_categories.keys():
        print("Columns between test and train data do not match")
        exit(1)

    columnsToDisregard = ['unnamed:_0',
                          'age_of_car',
                          'max_torque',
                          'max_power']
    train_df = train_df.filter(items=[
        x for x in train_df.columns if x not in columnsToDisregard
    ])
    test_df = test_df.filter(items=[
        x for x in test_df.columns if x not in columnsToDisregard
    ])

    # Feature selection (uncomment the code below to see how I select features)
    # Method 1: using SelectKBest
    # sort_dict = get_top_features(train_df, test_df, 10, 10)
    # print(sort_dict)
    # exit()

    # Method 2: correlation heatmap
    # plt.figure(figsize=(8, 12))
    # heatmap = sns.heatmap(train_df.corr()[[colToPredict]].sort_values(by=colToPredict, ascending=False),
    #                       vmin=-1, vmax=1, annot=True, cmap='BrBG')
    # heatmap.set_title(f'Features Correlating with {colToPredict}')  # fontdict={'fontsize': 18}, pad=16
    # plt.show()
    # exit()

    topFeatures = ['policy_tenure',
                   'area_cluster',
                   'length',
                   'steering_type',
                   'turning_radius',
                   'height',
                   'torque',
                   'segment',
                   'age_of_car_in_months']
    # other factors:
    # 'width',
    # 'age_of_policyholder',
    # 'fuel_type',
    # 'torque_rpm',
    # 'rear_brakes_type',
    # 'make'

    # filter train and test data to only include topFeatures
    train_df = train_df.filter(items=topFeatures + [colToPredict])
    test_df = test_df.filter(items=topFeatures + [colToPredict])

    # standardise data
    # this was not helpful in increasing F1-score whatsoever :(
    # train_df = standardise(train_df, colToPredict)
    # test_df = standardise(test_df, colToPredict)

    # Replace outliers and NaN values
    train_df = manage_outliers(train_df, train_categories, colToPredict)
    test_df = manage_outliers(test_df, test_categories, colToPredict)

    # split the datasets
    x_train, y_train, x_test, y_test = df_split(train_df, test_df, colToPredict)

    # oversample the minority class to match majority class
    smote = SMOTE(random_state=42)
    x_train, y_train = smote.fit_resample(x_train, y_train)

    # Attempt all classification models and select the highest f1-score model
    # classifiers = [KNeighborsClassifier(),
    #                LinearDiscriminantAnalysis(),
    #                DecisionTreeClassifier(),
    #                GaussianNB(),
    #                RandomForestClassifier(),
    #                RidgeClassifier(),
    #                GradientBoostingClassifier(),
    #                HistGradientBoostingClassifier()]
    classifiers = [GradientBoostingClassifier(learning_rate=0.12, n_estimators=131)]

    predData = {}
    predF1 = {}
    for model in classifiers:
        model.fit(x_train, y_train)

        # predict the test set
        y_pred = model.predict(x_test)
        f1 = f1_score(y_test, y_pred, average='macro')

        print(f"Model: {model}")
        # print("confusion_matrix:\n", confusion_matrix(y_test, y_pred))
        # print("precision:\t", precision_score(y_test, y_pred, average=None))
        # print("recall:\t\t", recall_score(y_test, y_pred, average=None))
        # print("accuracy:\t", accuracy_score(y_test, y_pred))

        # for i in range(len(y_test)):
        #     print("Expected:", y_test[i], "Predicted:", y_pred[i])
        print('Model F1 Score: %.3f' % f1)

        # perform cross validation to check for overfitting
        # cv = KFold(n_splits=10)
        # scores = cross_val_score(model, x_train, y_train, scoring='f1_micro', cv=cv, n_jobs=-1)
        # print('Cross Val F1 Score: %.3f' % scores.mean())

        predF1[model] = f1
        predData[model] = {
            "policy_id": policy_ids,
            "is_claim": y_pred
        }

        # Summary of top-performing classification models: each value is the average
        # KNeighborsClassifier() gives f1 = 0.501
        # DecisionTreeClassifier() gives f1 = 0.514
        # RidgeClassifier() gives f1 = 0.426
        # GradientBoostingClassifier() gives f1 = 0.539
        # HistGradientBoostingClassifier() gives f1 = 0.527

    model = max(predF1, key=predF1.get)
    newData = predData[model]
    # print(f"Choosing {model}")

    # add predicted to dataframe and return csv
    pred_df = pd.DataFrame(newData)
    pred_df = pred_df.set_index('policy_id')
    pred_df.to_csv(zid + '.PART2.output.csv')


# <<< MAIN >>>
if __name__ == '__main__':
    if len(sys.argv) != 3:
        print("Usage: python3 z5360925.py train.csv test.csv")
        exit(1)

    train_file = sys.argv[1]
    test_file = sys.argv[2]
    age_regression(train_file, test_file)
    claim_classification(train_file, test_file)
