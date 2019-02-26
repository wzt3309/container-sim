# -*- coding: utf-8 -*-
"""
负载类型感知
使用方式见 ./construct_interval.ipynb
"""
import math

import numpy as np
from scipy import stats
from statsmodels.tsa.stattools import acf


def saf(ts):
    """spectrum analyse function
    返回分析时序数据频谱特征
    """
    # 快速傅立叶变化，值域->频域
    ts_dft = np.fft.fft(ts)
    m = len(ts_dft) // 2
    # 获得频谱特征
    Q = ts_dft ** 2 / m
    return Q


def is_period(ts):
    """判断是否周期型
    """
    Q = saf(ts)
    # 对频谱特征进行差分，使特征更加明显
    H = np.array([])
    for i in range(1, math.ceil(len(Q) / 2)):
        h = abs((Q[i] - Q[i + 1]) + (Q[i] - Q[i - 1]))
        H = np.append(H, h)
    # 3-𝜎规则判定是否属于周期型
    mean = np.mean(H)
    std = np.std(H)
    flag, period = False, 0
    # 排除H[0]（表示周期数为1，即将整个数据作为一个周期）
    for i in range(1, len(H)):
        if H[i] - mean > 3 * std:
            flag, period = True, int(len(H) / (i + 1))
    return flag, period


def is_smooth(ts, u=0, alpha=0.05):
    """判断是否平稳型
    """
    P = acf(ts)
    t, p = stats.ttest_1samp(P, u)
    flag = (p > alpha)
    return flag


def is_smooth_score(ts, u=0, alpha=0.05):
    """判断是否平稳型，并输出与平稳序列接近程度（score越小，越接近平稳）
    """
    P = acf(ts)
    t, p = stats.ttest_1samp(P, u)
    flag = (p > alpha)
    score = (P.mean() - u) / (P.std())
    return flag, score


def pre(*args):
    """数据预处理
    返回负载序列，共287个点，24小时，每5分钟一个点
    """
    loads = []
    for df in args:
        df.columns = ['load']
        df = df.dropna()
        loads.append(df['load'])
    return tuple(loads)


def workload_aware(ts):
    """负载趋势感知
    """
    flag, period = is_period(ts)
    if flag:
        return 'PERIOD', period
    if is_smooth(ts):
        return 'SMOOTH', 0
    return 'TREND', 0
