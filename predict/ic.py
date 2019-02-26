# coding=utf-8
"""
根据不同负载类型构建区间
"""
import numpy as np
from scipy.optimize import leastsq


def ic_for_smooth(ts):
    """为平稳型负载构造区间
    """
    ts_max, ts_min, ts_len = ts.max(), ts.min(), len(ts)
    ts_up = np.array([0.5 * (ts[i] + ts_max) for i in range(ts_len)])
    ts_low = np.array([0.5 * (ts[i] + ts_min) for i in range(ts_len)])
    return ts_up, ts_low


def ic_for_trend(ts, sub_range=10):
    """为趋势型负载构造区间
    """
    global sub_ts_up, sub_ts_low
    ts_up, ts_low = np.array([]), np.array([])
    for i in range(0, len(ts), sub_range):
        # 子区域终点
        m = min(i + sub_range, len(ts))

        # 子区域
        sub_ts = ts[i:m]
        sub_ts_max, sub_ts_min, sub_ts_len = sub_ts.max(), sub_ts.min(), len(sub_ts)
        sub_interval = np.array([abs(sub_ts[j + 1] - sub_ts[j]) for j in range(i, m - 1)])
        sub_interval_mean = sub_interval.mean()

        # 最小二乘法判断趋势方向：上升/下降
        x = np.array([j for j in range(sub_ts_len)])
        y = np.array([sub_ts[j] for j in range(i, m)])
        tendency = get_tendency(x, y)

        if tendency == 'UP':
            sub_ts_up = np.array([(sub_ts[j] + sub_interval_mean + (sub_ts_max - sub_ts[j]) / (m - j))
                                  for j in range(i, m)])
            sub_ts_low = np.array([(sub_ts[j] - sub_interval_mean) for j in range(i, m)])
        if tendency == 'LOW':
            sub_ts_up = np.array([(sub_ts[j] + sub_interval_mean) for j in range(i, m)])
            sub_ts_low = np.array([(sub_ts[j] - sub_interval_mean - (sub_ts[j] - sub_ts_min) / (m - j))
                                   for j in range(i, m)])
        ts_up = np.append(ts_up, sub_ts_up)
        ts_low = np.append(ts_low, sub_ts_low)
    return ts_up, ts_low


def _trend_func(w, x):
    """趋势方向判别的回归函数
    """
    x = np.array([x, 1])
    return np.dot(w, x)


def _error(w, x, y):
    """最小二乘法误差
    """
    return _trend_func(w, x) - y


def get_tendency(x, y, w0=None, error=_error):
    """获得区域趋势：上升/下降
    """
    if w0 is None:
        w0 = [-10, 10]
    w = leastsq(error, w0, args=(x, y))
    k, b = w[0]
    if k >= 0:
        return 'UP'
    return 'LOW'


def ic_for_period(ts, period=0):
    """为周期型负载构造区间
    """
    global interval_mean
    ts_len, half_period = len(ts), int(period // 2)
    ts_up, ts_low = np.array([]), np.array([])
    for i in range(ts_len):
        if i <= half_period:
            interval = np.array([abs(ts[j + 1] - ts[j]) for j in range(period - 1)])
            interval_mean = interval.mean()
        if half_period < i < ts_len - half_period:
            interval = np.array([abs(ts[j + 1] - ts[j]) for j in range(i - half_period - 1, i + half_period - 1)])
            interval_mean = interval.mean()
        if i >= ts_len - half_period:
            interval = np.array([abs(ts[j + 1] - ts[j]) for j in range(ts_len - period - 1, ts_len - 1)])
            interval_mean = interval.mean()
        ts_up = np.append(ts_up, ts[i] + interval_mean)
        ts_low = np.append(ts_low, ts[i] - interval_mean)
    return ts_up, ts_low


def ic(ts, wl_type, period=0):
    if wl_type == 'SMOOTH':
        return ic_for_smooth(ts)
    if wl_type == 'TREND':
        return ic_for_trend(ts)
    if wl_type == 'PERIOD':
        if period == 0 or period is None:
            raise RuntimeError('period is empty for \'PERIOD\' type')
        return ic_for_period(ts, period)
    raise RuntimeError('workload type \'%s\' not support' % wl_type)
