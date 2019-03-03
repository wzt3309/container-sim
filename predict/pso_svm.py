import matplotlib.pyplot as plt
import numpy as np
from pyswarms.single.global_best import GlobalBestPSO
from sklearn import svm
from sklearn.model_selection import TimeSeriesSplit
from read_data import split_to_xy
from metrics import cwcf, picpf, pinewf

"""使用k-折交叉验证获得svm模型的最优参数gamma_u, C_u, gamma_l, C_l
"""


class PsoSvm:
    """
    Attributes
    ---------
    cv_t: 用于训练的原数据时序序列 ndarray
    cv_u: 时序序列cv_t的上边界 ndarray
    cv_l: 时序数据cv_t的下边界 ndarray
    d_u: 上边界差分范围，默认为(1,3)即 s[t] = svm(X=(s[t-1],s[t-2]))
    d_l: 下边界差分范围，同d_u
    cv:  交叉验证的fold数
    mu:  预测置信区间
    eta: 惩罚因子，表现算法偏好度。eta增加提升picp（覆盖率）；eta减小，提升pinew（区间宽度）
    """

    def __init__(self, cv_t, cv_u, cv_l, d_u=(1, 3), d_l=(1, 3), cv=3, mu=0.6, eta=10):
        self.cv_t = cv_t
        self.cv_u = cv_u
        self.cv_l = cv_l
        self.d_u = d_u
        self.d_l = d_l
        self.cv = cv
        self.mu = mu
        self.eta = eta

    def svm_predict(self, cv_tr_u, cv_te_u, cv_tr_l, cv_te_l, svr_u, svr_l, d_u, d_l):
        """返回对测试集 cv_te_u，cv_te_l 的预测
        cv_tr_u: 上边界训练集
        cv_te_u: 上边界测试集
        cv_tr_l: 下边界训练集
        cv_te_l: 下边界测试集
        svr_u:   上边界svm模型
        svr_l:   下边界svm模型
        """
        if d_u is None:
            d_u = self.d_u
        if d_l is None:
            d_l = self.d_l

        xu_tr, yu_tr = split_to_xy(cv_tr_u, d=range(*d_u))
        xu_te, yu_te = split_to_xy(cv_te_u, d=range(*d_u))
        svr_u.fit(xu_tr, yu_tr)
        pu = svr_u.predict(xu_te)

        xl_tr, yl_tr = split_to_xy(cv_tr_l, d=range(*d_l))
        xl_te, yl_te = split_to_xy(cv_te_l, d=range(*d_l))
        svr_l.fit(xl_tr, yl_tr)
        pl = svr_l.predict(xl_te)
        return pu, pl, len(self.d_u)

    def cross_cwcf(self, gamma_u, C_u, gamma_l, C_l, d_u=None, d_l=None, cv=3, mu=0.6, eta=10, isplt=False):
        """计算交叉验证的cwc（区间覆盖宽度标准） picp（区间覆盖率） pinew（区间宽度）
        cv: 折数
        mu: 预测置信区间
        eta: 惩罚因子，表现算法偏好度。eta增加提升picp；eta减小，提升pinew
        isplt: 是否将每折画图
        """
        tscv = TimeSeriesSplit(n_splits=cv)
        cwc_l = []
        picp_l = []
        pinew_l = []
        i_cv = 0
        plot_num = cv * 100 + 3 * 10
        for tridx, teidx in tscv.split(self.cv_t):
            cv_tr_u, cv_te_u = self.cv_u[tridx], self.cv_u[teidx]
            cv_tr_l, cv_te_l = self.cv_l[tridx], self.cv_l[teidx]
            cv_tr_t, cv_te_t = self.cv_t[tridx], self.cv_t[teidx]

            svr_u = svm.SVR(gamma=gamma_u, C=C_u)
            svr_l = svm.SVR(gamma=gamma_l, C=C_l)
            pu, pl, d = self.svm_predict(cv_tr_u, cv_te_u, cv_tr_l, cv_te_l, svr_u, svr_l, d_u, d_l)

            if isplt:
                plt.figure(figsize=(15, 6))
                plt.subplot(plot_num + 1)
                plt.title('cv train %d' % (i_cv))
                plt.plot(cv_tr_u, color='blue', marker='o')
                plt.plot(cv_tr_l, color='blue', marker='o')
                plt.plot(cv_tr_t, color='gray', marker='x')
                plt.subplot(plot_num + 2)
                plt.title('cv test %d' % (i_cv))
                plt.plot(cv_te_u, color='blue', marker='o')
                plt.plot(cv_te_l, color='blue', marker='o')
                plt.plot(cv_te_t, color='gray', marker='x')
                plt.subplot(plot_num + 3)
                plt.title('cv predict %d' % (i_cv))
                plt.plot(pu, color='blue', marker='o')
                plt.plot(pl, color='blue', marker='o')
                plt.plot(cv_te_t[d:], color='gray', marker='x')
            i_cv += 1
            plot_num += 3

            picp = picpf(cv_te_t[d:], pu, pl)
            picp_l.append(picp)
            pinew = pinewf(pu, pl)
            pinew_l.append(pinew)
            cwc = cwcf(picp, pinew, mu, eta)
            cwc_l.append(cwc)
        return np.array(cwc_l).mean(), np.array(picp_l).mean(), np.array(pinew_l).mean()

    def pso_fitf(self, x):
        """pso 优化的目标函数"""
        n_particles = x.shape[0]
        los = []
        for i in range(n_particles):
            cwc, picp, pinew = self.cross_cwcf(*x[i], self.cv, self.mu, self.eta)
            los.append(cwc)
        return los

    def pso_train(self, bounds, n_particles=10, options=None, iters=1000):
        """返回优化后的 cost=cwc_best, pos=(gamma_u, C_u, gamma_l, C_l), optimizer(pso优化器)
        :param bounds: 参数边界
        :param n_particles: 粒子数
        :param options: 粒子更新选项
        :param iters: 迭代次数
        :return:
        """
        if options is None:
            options = {'c1': 0.5, 'c2': 0.3, 'w': 0.9}
        optimizer = GlobalBestPSO(n_particles=n_particles, dimensions=4, options=options, bounds=bounds)
        cost, pos = optimizer.optimize(self.pso_fitf, iters=iters)
        return cost, pos, optimizer


def pso_bounds(gamma_u=(0.001, 1.0), C_u=(10, 30), gamma_l=(0.001, 1.0), C_l=(10, 30)):
    """gamma与C范围
    """
    gamma_u_bounds = np.array(gamma_u).reshape(2, 1)
    C_u_bounds = np.array(C_u).reshape(2, 1)
    gamma_l_bounds = np.array(gamma_l).reshape(2, 1)
    C_l_bounds = np.array(C_l).reshape(2, 1)

    bounds = np.column_stack((gamma_u_bounds, C_u_bounds, gamma_l_bounds, C_l_bounds))
    return bounds[0], bounds[1]
