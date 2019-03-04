import os
import warnings

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

import ic
import wta
from settings import data_dir, smooth_csv, trend_csv, period_csv


def list_datafile(csv_per_dir=100):
    csv_l = []
    for d in os.listdir(data_dir):
        child = os.path.join(data_dir, d)
        if os.path.isdir(child):
            for csv in os.listdir(child)[:csv_per_dir]:
                if csv != '.DS_Store':
                    csv_l.append((d, csv))
    return csv_l


def read_csv(path):
    return pd.read_csv(os.path.join(data_dir, *path))


def load_data(*args):
    """输入路径，返回时间序列"""
    ts_l = []
    for path in args:
        if isinstance(path, tuple) or isinstance(path, list):
            df = pd.read_csv(os.path.join(data_dir, *path))
        elif isinstance(path, str):
            df = pd.read_csv(os.path.join(data_dir, path))
        else:
            raise RuntimeError('not support type for path %s' % path)
        ts_l.append(df)
    return wta.pre(*ts_l)


def load_sp():
    """加载代表"""
    return load_data(smooth_csv, trend_csv, period_csv)


def get_data_detail():
    total_num_of_datafile = {}
    total_type = {}
    wl_sm = {}
    wl_tr = {}
    wl_pe = {}
    for d in os.listdir(data_dir):
        child = os.path.join(data_dir, d)
        if os.path.isdir(child):
            wl_sm_l = []
            wl_tr_l = []
            wl_pe_l = []
            num_of_datafile = 0
            for csv in os.listdir(child):
                if csv != '.DS_Store':
                    num_of_datafile += 1
                    ts = load_data((d, csv))
                    wl_type, period = wta.workload_aware(*ts)
                    if wl_type == 'SMOOTH':
                        wl_sm_l.append((d, csv, 'SMOOTH'))
                    if wl_type == 'TREND':
                        wl_tr_l.append((d, csv, 'TREND'))
                    if wl_type == 'PERIOD':
                        wl_pe_l.append((d, csv, period, 'PERIOD'))
            wl_sm[d] = wl_sm_l
            wl_tr[d] = wl_tr_l
            wl_pe[d] = wl_pe_l
            total_num_of_datafile[d] = num_of_datafile
    total_type['SMOOTH'] = wl_sm
    total_type['TREND'] = wl_tr
    total_type['PERIOD'] = wl_pe
    return total_num_of_datafile, total_type


def split_train_test(ts, train_size=200):
    """返回上下界训练、测试数据机，以及测试训练数据集的真值"""
    # 判断ts的类型
    wl_type, period = wta.workload_aware(ts)
    # 构建ts的区间
    ts_up, ts_low = ic.ic(ts, wl_type, period)
    if len(ts_up) == len(ts) and len(ts_low) == len(ts):
        sample_size = len(ts)
        if train_size < sample_size // 2:
            warnings.warn('train_size(%d) less than half of sample_size(%d)'
                          % (train_size, sample_size), RuntimeWarning)
        if train_size >= sample_size:
            raise RuntimeError('train_size(%d) large than sample_size(%d)' % (train_size, sample_size))
        ts_u_train, ts_u_test = ts_up[:train_size], ts_up[train_size:]
        ts_l_train, ts_l_test = ts_low[:train_size], ts_low[train_size:]
        ts_train, ts_test = np.array(ts[:train_size]), np.array(ts[train_size:])
        return ts_u_train, ts_u_test, ts_l_train, ts_l_test, ts_train, ts_test


def ts_to_arr(tss):
    return map(lambda ts: np.array(ts), tss)


def plot_train_test(ts_u_train, ts_u_test, ts_l_train, ts_l_test, ts_train, ts_test, figsize=(15, 6)):
    """画出训练和测试数据集"""
    plt.plot(figsize=figsize)
    plt.subplot(211)
    plot_ts_ul(ts_train, ts_u_train, ts_l_train, title='Workload Train')
    plt.subplot(211)
    plot_ts_ul(ts_test, ts_u_test, ts_l_test, title='Workload Test')


def plot_ts_ul(ts, ts_u, ts_l, st=None, size=None, title=''):
    if size is None:
        size = min(len(ts), len(ts_u), len(ts_l))
    if st is None:
        st = 0
    ed = st + size

    plt.title(title)
    plt.plot(ts[st:ed], color='green', marker='x')
    plt.plot(ts_u[st:ed], color='gray', linestyle='--')
    plt.plot(ts_l[st:ed], color='gray', linestyle='--')


def split_to_xy(ts, d=range(1, 3)):
    """将原时序数据转变为x,y两个数列,
    x（向量，每个分量x[i]滞后期为d[i]）作为svm回归的自变量
    """
    x = np.empty((0, len(d)))
    y = np.array([])
    for i in range(len(ts)):
        # 相应滞后的值在原序列中的索引
        idx = []
        for j in d:
            k = i - j
            if k >= 0:
                idx.append(k)
            else:
                idx = []
                break
        x_d = []
        for j in idx:
            x_d.append(ts[j])
        if len(x_d) != 0:
            x_d = np.array([x_d])
            x = np.row_stack((x, x_d))
            y = np.append(y, ts[i])
    return x, y


def split_to_Xy(ts, ts_u, ts_l, d=range(1, 3)):
    """将数据变成矩阵形式，X是4列矩阵，前2是up，后2是low，y是3列矩阵，0是up，1是low，2是真值"""
    X1, y1 = split_to_xy(ts_u, d)
    X2, y2 = split_to_xy(ts_l, d)
    y3 = ts[len(d):]
    X = np.column_stack((X1, X2))
    y = np.column_stack((y1, y2, y3))
    return X, y
