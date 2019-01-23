import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

from os import chdir, listdir, path

chdir('D:\\data\\cloudsim\\workload\\planetlab')


def list_file_num():
    num_of_file = {}
    for fd in listdir('.'):
        fp = path.join(path.abspath('.'), fd)
        count = 0
        for f in listdir(fp):
            count += 1
        num_of_file[fd] = count

    print(num_of_file)


df = pd.read_csv(
    path.join(path.abspath('.'), '20110303', '75-130-96-12_static_oxfr_ma_charter_com_irisaple_wup'),
    header=None, names=['cpu'])
print(df.dtypes)

df['usage'] = df['cpu'] / 100
time_series = pd.date_range('20110303', '20110304', dtype='datetime64[ns]', freq='5min', closed='left')
df['time'] = time_series
