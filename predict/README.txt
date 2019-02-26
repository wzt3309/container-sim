找到平稳型代表：'20110306', 'planetlab1_cs_purdue_edu_purdue_2'
找到趋势形代表：'20110412', 'planet1_scs_cs_nyu_edu_uw_oneswarm'
找到周期型代表：'20110403', 'planetlab2-santiago_lan_redclara_net_ethzcs_tracegatherer'


```python
data_dir = '../data/planetlab'
smooth_csv = ('20110306', 'planetlab1_cs_purdue_edu_purdue_2')
trend_csv = ('20110412', 'planet1_scs_cs_nyu_edu_uw_oneswarm')
period_csv = ('20110403', 'planetlab2-santiago_lan_redclara_net_ethzcs_tracegatherer')

smooth_df = pd.read_csv(os.path.join(data_dir, *smooth_csv))
trend_df = pd.read_csv(os.path.join(data_dir, *trend_csv))
period_df = pd.read_csv(os.path.join(data_dir, *period_csv))
```