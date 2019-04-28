# predict

## 简介

在云计算场景中，云服务种类的多样造成不同容器其负载变化趋势的多样，甚至对同一容器来说，在不同时期，其负载变化趋势也是不同的。例如有些容器的负载是平
稳的、有些是具有上升/下降趋势的、还有些是不断进行周期性波动的。

针对这一问题，提出了基于趋势感知的区间预测模型（SAC-GPSO-SVM）。首先，通过分析容器负载时序数据的频谱特征（Spectrum Analysis）和
自相关系数（Autocorrelation Coefficient）（简称SAC规则），对容器负载变化特征进行分类，即趋势感知。再依据分类结果，采用不同方法将原先时序
数据的单值序列转变为时序数据的区间序列，这一步避免了如传统区间预测模型一样，对不同负载类型作同一种分布假设，从而提升了预测的准确性。
由于SVM方法具有能处理高维空间的非线性映射、剔除“冗余”样本增强鲁棒性以及简化回归提升算法效率等优点，因此，
更适合用于将预测结果作为资源配置和调度决策依据的场景。最后将基于SVM方法来给出容器负载的预测域，并采用带梯度信息的粒子群算法来优化模型参数。

实验采用的数据为：**PlanetLab从2011-03-03～2011-04-20的容器负载数据**

SAC-GPSO-SVM模型的主要流程为：
1. 区间构造。负载监控软件采样获得的容器负载数据一般是单值序列，通过区间构造，可将其转化为相应的区间序列，即包含负载上下界的时序数据；
2. 区间预测。使用SVM对上一过程生成的区间序列的上下界分别进行预测，并使用带梯度信息的粒子群算法对模型参数进行优化，最后获得容器负载的预测域。

![p-fig1](./predict/doc/fig1.png)

## 实验数据介绍
使用的实验数据为公开数据集PlanetLab从2011年03月03日到2011年04月20日共10天的11746个容器的CPU利用率的历史数据。
每份数据都记录了当日每5分钟内容器CPU利用率的平均值，共计287个数据点。如下表所示

|  编号 |   目录   | 文件数   |   
|-------|----------|--------|
|   0   | 20110403 |  1463  |
|   1   | 20110303 |  1052  |
|   2   | 20110420 |  1033  |
|   3   | 20110411 |  1233  |
|   4   | 20110409 |  1358  |
|   5   | 20110325 |  1078  |
|   6   | 20110322 |  1516  |
|   7   | 20110306 |  898   |
|   8   | 20110309 |  1061  |
|   9   | 20110412 |  1054  |
| Total |          | 11746  |

## 模块介绍
### python 模块
- read_data.py：读取数据模块
- adf_test.py：序列初始化分析以及平稳性检验模块
- wta.py：趋势感知模块
- ic.py：区间构造模块
- pso_svm.py：SAC-GPSO-SVM模型的参数优化和区间预测模块
- metrics.py：区间覆盖率与区间宽度等预测结果评价指标模块
- predict_fx.py：对比算法实现模块

### jupyter-book文件
jupyter-book文件可作为实验中的可执行文件，直接在服务器中运行完整实验

实验文件

- /Users/zedd/OpenSource/paperlab/predict/01_bayesian_predict.ipynb：使用贝叶斯模型进行预测
- /Users/zedd/OpenSource/paperlab/predict/02_linear_regression.ipynb：使用线性回归模型进行预测
- /Users/zedd/OpenSource/paperlab/predict/03_cart_predict.ipynb：使用回归树模型进行预测
- /Users/zedd/OpenSource/paperlab/predict/04_svm_predict.ipynb：使用SVM模型进行预测
- /Users/zedd/OpenSource/paperlab/predict/05_rfr_predict.ipynb：使用随机森林模型进行预测
- /Users/zedd/OpenSource/paperlab/predict/06_pso-svm_predict.ipynb：使用SAC-GPSO-SVM模型进行预测
- /Users/zedd/OpenSource/paperlab/predict/data_detail.ipynb：分析实验数据（PlanetLab的容器数据）规律
- /Users/zedd/OpenSource/paperlab/predict/grid_random_pso.ipynb：参数优化实验
- /Users/zedd/OpenSource/paperlab/predict/time_compare.ipynb：模型理论预测时间对比
- /Users/zedd/OpenSource/paperlab/predict/xxx_model_plot.ipynb：实验结果画图

测试文件

- /Users/zedd/OpenSource/paperlab/predict/cal_load_coef.ipynb：测试计算实验数据（PlanetLab）的自相关系数
- /Users/zedd/OpenSource/paperlab/predict/construct_interval.ipynb：测试构建区间
- /Users/zedd/OpenSource/paperlab/predict/find_period_load.ipynb：测试寻找负载数据周期性
- /Users/zedd/OpenSource/paperlab/predict/find_smooth_load.ipynb：测试寻找负载数据平稳性和趋势性
- /Users/zedd/OpenSource/paperlab/predict/svm_predict.ipynb：测试SVM预测方法

### 结果文件

- /Users/zedd/OpenSource/paperlab/predict/README.txt：原始数据特征分析结果
- /Users/zedd/OpenSource/paperlab/predict/fig：实验结果示意图
- /Users/zedd/OpenSource/paperlab/predict/fig_out：实验结果示意图（word级别）