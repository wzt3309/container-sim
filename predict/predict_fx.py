import numpy as np
import matplotlib.pylab as plt
from sklearn.model_selection import TimeSeriesSplit
from sklearn.linear_model import LinearRegression, BayesianRidge
from sklearn.tree import DecisionTreeRegressor
from sklearn.svm import SVR
from sklearn.ensemble import RandomForestRegressor

import read_data as rd
from metrics import cwcf, picpf, pinewf
from pyswarms.utils.plotters import plot_cost_history, plot_contour, plot_surface


def xxx_predict(train, test, xxx, d):
    X_t, y_t = rd.split_to_xy(train, d)
    X_e, y_e = rd.split_to_xy(test, d)
    xxx.fit(X_t, y_t)
    pred = xxx.predict(X_e)
    return pred


def xxx_cross_validation(t, u, l, xxx_u, xxx_l, mu=0.9, eta=10, cv=3, is_plot=False, plt_no=None):
    tscv = TimeSeriesSplit(n_splits=cv)
    cwc_l = []
    picp_l = []
    pinew_l = []
    plt_num = 0
    for tr_idx, te_idx in tscv.split(t):
        tr_u, te_u = u[tr_idx], u[te_idx]
        tr_l, te_l = l[tr_idx], l[te_idx]
        tr_t, te_t = t[tr_idx], t[te_idx]

        pred_u = xxx_predict(tr_u, te_u, xxx_u, range(1, 3))
        pred_l = xxx_predict(tr_l, te_l, xxx_l, range(3))
        d = max(len(te_t) - len(pred_u), len(te_t) - len(pred_l))

        if is_plot:
            plt.figure(figsize=(8, 6))
            plt.subplot(plt_no)
            rd.plot_ts_ul(te_t[d:], pred_u, pred_l, title='Cross validation %d' % plt_num)
            plt_no += 1
            plt_num += 1

        picp = picpf(te_t[d:], pred_u, pred_l)
        picp_l.append(picp)
        pinew = pinewf(pred_u, pred_l)
        pinew_l.append(pinew)
        cwc = cwcf(picp, pinew, mu, eta)
        cwc_l.append(cwc)
    return np.array(cwc_l).mean(), np.array(picp_l).mean(), np.array(pinew_l).mean()


def xxx_test_validation(test, u_train, u_test, l_train, l_test, xxx_u, xxx_l, mu=0.9, eta=10,
                        is_plot=False, plt_num=111):
    pred_u = xxx_predict(u_train, u_test, xxx_u, range(1,3))
    pred_l = xxx_predict(l_train, l_test, xxx_l, range(3))
    d = max(len(test) - len(pred_u), len(test) - len(pred_l))

    if is_plot:
        plt.subplot(plt_num)
        plt.figure(figsize=(8, 6))
        rd.plot_ts_ul(test[d:], pred_u, pred_l)

    picp = picpf(test[d:], pred_u, pred_l)
    pinew = pinewf(pred_u, pred_l)
    cwc = cwcf(picp, pinew, mu, eta)
    return cwc, picp, pinew, pred_u, pred_l, test[d:]


def predict_sp_diff(model='linear'):
    ts_sm, ts_tr, ts_pe = rd.load_sp()
    print("Using %s 'model' to predict" % model)
    res1 = np.array(predict_diff(ts_sm, model))
    res2 = np.array(predict_diff(ts_tr, model))
    res3 = np.array(predict_diff(ts_pe, model))

    res = np.row_stack((res1, res2, res3))
    res_mean = np.mean(res, axis=0)

    print('\nFinal Result:')
    print('cwc %f, picp %f, pinew %f' % (res_mean[0], res_mean[1], res_mean[2]))


def predict_diff(ts, model, mu=0.9, eta=10, cv=3, is_plt=False, plt_num=111):
    # split to train and test
    ts_u_train, ts_u_test, ts_l_train, ts_l_test, ts_train, ts_test = rd.split_train_test(ts)
    print(model)
    if model == 'linear':
        xxx_cv_u = LinearRegression()
        xxx_cv_l = LinearRegression()

        xxx_u = LinearRegression()
        xxx_l = LinearRegression()
    elif model == 'bayesian':
        xxx_cv_u = LinearRegression()
        xxx_cv_l = LinearRegression()

        xxx_u = BayesianRidge()
        xxx_l = BayesianRidge()
    elif model == 'cart':
        xxx_cv_u = DecisionTreeRegressor()
        xxx_cv_l = DecisionTreeRegressor()

        xxx_u = DecisionTreeRegressor()
        xxx_l = DecisionTreeRegressor()
    elif model == 'svm':
        xxx_cv_u = SVR(gamma=0.001, C=20)
        xxx_cv_l = SVR(gamma=0.001, C=20)

        xxx_u = SVR(gamma=0.001, C=20)
        xxx_l = SVR(gamma=0.001, C=20)
    elif model == 'rfr':
        xxx_cv_u = RandomForestRegressor(n_estimators=50)
        xxx_cv_l = RandomForestRegressor(n_estimators=50)

        xxx_u = RandomForestRegressor(n_estimators=50)
        xxx_l = RandomForestRegressor(n_estimators=50)
    else:
        raise RuntimeError('not support model type')

    print('Result of Cross validation: ')
    cv_cwc, cv_picp, cv_pinew = xxx_cross_validation(ts_train, ts_u_train, ts_l_train,
                                                     xxx_cv_u, xxx_cv_l,
                                                     mu, eta,
                                                     cv)
    print('cwc %f, picp %f, pinew %f' % (cv_cwc, cv_picp, cv_pinew))

    print('Result of Test validation: ')
    cwc, picp, pinew, pred_u, pred_l, t = xxx_test_validation(ts_test, ts_u_train, ts_u_test, ts_l_train, ts_l_test,
                                           xxx_u, xxx_l,
                                           mu, eta,
                                           is_plt, plt_num)
    print('cwc %f, picp %f, pinew %f' % (cwc, picp, pinew))
    return cwc, picp, pinew, pred_u, pred_l, t
