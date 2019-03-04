import numpy as np


def cwcf(picp, pinew, mu=0.9, eta=10):
    gamma = 1
    if picp >= mu:
        gamma = 0
    cwc = pinew * (1 + gamma * np.exp(-eta * (picp - mu)))
    return cwc


def picpf(ts, pu, pl):
    c_num = len(ts)
    if c_num == 0:
        raise RuntimeError('time series is empty')
    c_sum = 0
    for i in range(c_num):
        val_t = ts[i]
        if pl[i] <= val_t <= pu[i]:
            c_sum += 1
    return c_sum / c_num


def pinewf(pu, pl):
    if len(pu) == 0 or len(pl) == 0:
        raise RuntimeError('up or low bound is empty')
    if len(pu) != len(pl):
        raise RuntimeError('size of up an low bound is not equal')
    max_u = pu.max()
    min_l = pl.min()
    r = max_u - min_l
    b_num = len(pu)
    b_sum = 0
    for i in range(b_num):
        b_sum += (pu[i] - pl[i]) / r
    return b_sum / b_num
