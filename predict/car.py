#! /usr/bin/env python3
# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

df = pd.read_csv('../data/mock/ts_tendency.csv')
print(df.head(5))

df_count = df.drop(columns=['Month'], axis=1)
print(df_count.head(5))
df_norm = (df_count - df_count.min()) / (df_count.max() - df_count.min())
# 需要的时间序列
ts = 100 * df_norm
plt.plot(ts, color='gray', linestyle='--', marker='o')
plt.grid()
plt.show()
ts = ts.dropna()


# 平稳性检验
from statsmodels.tsa.stattools import adfuller


def test_stationarity(ts):
    rolmean = ts.rolling(12).mean()
    rolstd = ts.rolling(12).std()

    orig = plt.plot(ts, color='blue', label='Original')
    mean = plt.plot(rolmean, color='red', label='Rolling mean')
    std = plt.plot(rolstd, color='black', label='Rolling std')
    plt.legend(loc='best')
    plt.title('Rolling mean & Rolling std')
    plt.show(block=False)

    print("Result of Dickey-Fuller Test:")
    dftest = adfuller(ts['Count'])
    dfoutput = pd.Series(dftest[0:4], index=['Test Statistic', 'p-value', '#Lag Used', 'Number of Observations Used'])
    for key, value in dftest[4].items():
        dfoutput['Critical Value (%s)' % key] = value
    print(dfoutput)


test_stationarity(ts)
"""ADF检验结果：p-value > critical value 不能拒绝原假设，序列不平稳
Test Statistic                 -1.223813
p-value                         0.663269
#Lag Used                      12.000000
Number of Observations Used    95.000000
Critical Value (1%)            -3.501137
Critical Value (5%)            -2.892480
Critical Value (10%)           -2.583275
"""

ts_diff = ts.diff(2).dropna() + 40
plt.plot(ts_diff, color='gray', linestyle='--', marker='o')
plt.show()
test_stationarity(ts_diff)