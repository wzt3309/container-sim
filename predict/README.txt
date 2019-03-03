# 特殊代表
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

# 数据介绍
+-------+----------+--------+
|  编号 |   目录   | 文件数 |
+-------+----------+--------+
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
+-------+----------+--------+

+--------+----------+--------+
|  类型  |   目录   | 文件数 |
+--------+----------+--------+
| SMOOTH | 20110403 |  986   |
| SMOOTH | 20110303 |  731   |
| SMOOTH | 20110420 |  826   |
| SMOOTH | 20110411 |  894   |
| SMOOTH | 20110409 |  971   |
| SMOOTH | 20110325 |  764   |
| SMOOTH | 20110322 |  926   |
| SMOOTH | 20110306 |  585   |
| SMOOTH | 20110309 |  583   |
| SMOOTH | 20110412 |  620   |
| Total  |          |  7886  |
| TREND  | 20110403 |  466   |
| TREND  | 20110303 |  311   |
| TREND  | 20110420 |  186   |
| TREND  | 20110411 |  325   |
| TREND  | 20110409 |  371   |
| TREND  | 20110325 |  303   |
| TREND  | 20110322 |  577   |
| TREND  | 20110306 |  300   |
| TREND  | 20110309 |  461   |
| TREND  | 20110412 |  419   |
| Total  |          |  3719  |
| PERIOD | 20110403 |   11   |
| PERIOD | 20110303 |   10   |
| PERIOD | 20110420 |   21   |
| PERIOD | 20110411 |   14   |
| PERIOD | 20110409 |   16   |
| PERIOD | 20110325 |   11   |
| PERIOD | 20110322 |   13   |
| PERIOD | 20110306 |   13   |
| PERIOD | 20110309 |   17   |
| PERIOD | 20110412 |   15   |
| Total  |          |  141   |
+--------+----------+--------+

## Smooth 平稳型数据
略
## Trend 趋势型数据
略
## Period 周期型数据
{'20110403': [('20110403', 'planetlab-3_cmcl_cs_cmu_edu_uw_trs2', 5, 'PERIOD'),
  ('20110403', 'planetlab-1_cse_ohio-state_edu_arizona_nest', 5, 'PERIOD'),
  ('20110403',
   'planetlab2-santiago_lan_redclara_net_ethzcs_tracegatherer',
   47,
   'PERIOD'),
  ('20110403', 'node2_lbnl_nodes_planet-lab_org_root', 1, 'PERIOD'),
  ('20110403', 'planetlab-1_dis_uniroma1_it_arizona_owl', 2, 'PERIOD'),
  ('20110403', 'mercury_silicon-valley_ru_root', 1, 'PERIOD'),
  ('20110403', 'planetlab-3_cmcl_cs_cmu_edu_upmc_ts', 71, 'PERIOD'),
  ('20110403',
   'planetlab-1_amst_nodes_planet-lab_org_arizona_beta',
   5,
   'PERIOD'),
  ('20110403', 'planetlab2_aston_ac_uk_princeton_coblitz', 71, 'PERIOD'),
  ('20110403', 'planetlab-2_cmcl_cs_cmu_edu_howard_p2psip', 5, 'PERIOD'),
  ('20110403', 'planetlab2_exp-math_uni-essen_de_arizona_beta', 5, 'PERIOD')],
 '20110303': [('20110303',
   'node2_planetlab_albany_edu_howard_p2psip',
   5,
   'PERIOD'),
  ('20110303', 'planet2_l3s_uni-hannover_de_uw_trs2', 5, 'PERIOD'),
  ('20110303', 'planetlab1_rutgers_edu_root', 1, 'PERIOD'),
  ('20110303', 'planetlab1_cs_uoi_gr_root', 1, 'PERIOD'),
  ('20110303', 'peace_informatik_rwth-aachen_de_uw_trs2', 5, 'PERIOD'),
  ('20110303', 'planetlab1_informatik_uni-goettingen_de_uw_trs2', 5, 'PERIOD'),
  ('20110303', 'planet2_colbud_hu_root', 4, 'PERIOD'),
  ('20110303', 'earth_cs_brown_edu_root', 1, 'PERIOD'),
  ('20110303', 'planetlab1_unineuchatel_ch_uw_trs2', 5, 'PERIOD'),
  ('20110303', 'pli1-pa-5_hpl_hp_com_root', 2, 'PERIOD')],
 '20110420': [('20110420', 'planetlabpc1_upf_edu_ufl_test0', 2, 'PERIOD'),
  ('20110420', 'planet1_att_nodes_planet-lab_org_howard_p2psip', 5, 'PERIOD'),
  ('20110420', 'planet1_jaist_ac_jp_arizona_owl', 5, 'PERIOD'),
  ('20110420', 'planetlab3_cs_uchicago_edu_arizona_beta', 5, 'PERIOD'),
  ('20110420', 'planet0_jaist_ac_jp_howard_p2psip', 5, 'PERIOD'),
  ('20110420', 'planetlab1_informatik_uni-kl_de_uw_oneswarm', 71, 'PERIOD'),
  ('20110420',
   'planetlab11_millennium_berkeley_edu_arizona_beta',
   5,
   'PERIOD'),
  ('20110420', 'planetlab01_cs_tcd_ie_arizona_beta', 1, 'PERIOD'),
  ('20110420', 'plab2_engr_sjsu_edu_arizona_beta', 5, 'PERIOD'),
  ('20110420', 'plab2_engr_sjsu_edu_howard_p2psip', 5, 'PERIOD'),
  ('20110420',
   'host3_planetlab_informatik_tu-darmstadt_de_princeton_coblitz',
   71,
   'PERIOD'),
  ('20110420', 'planetlab2_exp-math_uni-essen_de_root', 71, 'PERIOD'),
  ('20110420',
   'planetlab-1_amst_nodes_planet-lab_org_arizona_beta',
   11,
   'PERIOD'),
  ('20110420', 'planet0_jaist_ac_jp_arizona_owl', 5, 'PERIOD'),
  ('20110420', 'planet1_att_nodes_planet-lab_org_arizona_beta', 5, 'PERIOD'),
  ('20110420', 'planetlab2_aston_ac_uk_princeton_coblitz', 10, 'PERIOD'),
  ('20110420', 'planet0_jaist_ac_jp_arizona_beta', 5, 'PERIOD'),
  ('20110420', 'planet1_jaist_ac_jp_howard_p2psip', 5, 'PERIOD'),
  ('20110420', 'prueba2_cti_espol_edu_ec_arizona_beta', 1, 'PERIOD'),
  ('20110420', 'planetlab2_aston_ac_uk_arizona_beta', 5, 'PERIOD'),
  ('20110420', 'nodeb_howard_edu_tsinghua_xyz', 1, 'PERIOD')],
 '20110411': [('20110411',
   'planetslug3_cse_ucsc_edu_nus_proxaudio',
   5,
   'PERIOD'),
  ('20110411', 'planetlab-1_cmcl_cs_cmu_edu_arizona_nest', 5, 'PERIOD'),
  ('20110411',
   'host2_planetlab_informatik_tu-darmstadt_de_princeton_coblitz',
   28,
   'PERIOD'),
  ('20110411', 'host4-plb_loria_fr_tsinghua_xyz', 9, 'PERIOD'),
  ('20110411', 'jupiter_cs_brown_edu_arizona_digdug2', 5, 'PERIOD'),
  ('20110411', 'planetslug3_cse_ucsc_edu_williams_gush', 5, 'PERIOD'),
  ('20110411', 'planetlab2_cs_uit_no_root', 1, 'PERIOD'),
  ('20110411', 'planetlab1_hiit_fi_arizona_nest', 5, 'PERIOD'),
  ('20110411', 'planetlab1_georgetown_edu_root', 1, 'PERIOD'),
  ('20110411', 'planetlab1_koganei_wide_ad_jp_princeton_codeen', 20, 'PERIOD'),
  ('20110411', 'orval_infonet_fundp_ac_be_uw_oneswarm', 47, 'PERIOD'),
  ('20110411', 'planet02_csc_ncsu_edu_arizona_owl', 5, 'PERIOD'),
  ('20110411', 'planet02_hhi_fraunhofer_de_uw_oneswarm', 71, 'PERIOD'),
  ('20110411', 'planetlab2_aston_ac_uk_princeton_coblitz', 28, 'PERIOD')],
 '20110409': [('20110409', 'planetlab1_tamu_edu_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'planet1_jaist_ac_jp_arizona_owl', 5, 'PERIOD'),
  ('20110409', 'planetlab1_iii_u-tokyo_ac_jp_uw_oneswarm', 71, 'PERIOD'),
  ('20110409', 'planet2_att_nodes_planet-lab_org_arizona_nest', 5, 'PERIOD'),
  ('20110409', 'plab2_engr_sjsu_edu_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'planet02_csc_ncsu_edu_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'planet1_jaist_ac_jp_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'planet2_att_nodes_planet-lab_org_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'server2_planetlab_iit-tech_net_arizona_nest', 5, 'PERIOD'),
  ('20110409', 'planet1_jaist_ac_jp_howard_p2psip', 5, 'PERIOD'),
  ('20110409', 'planetlab2_cs_uoi_gr_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'planetlab2_wiwi_hu-berlin_de_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'planetlab1_singaren_net_sg_princeton_coblitz', 71, 'PERIOD'),
  ('20110409', 'planetlab2_exp-math_uni-essen_de_arizona_beta', 5, 'PERIOD'),
  ('20110409', 'planetlab2_cs_uoi_gr_arizona_owl', 5, 'PERIOD'),
  ('20110409', 'planetlab1_otemachi_wide_ad_jp_arizona_beta', 5, 'PERIOD')],
 '20110325': [('20110325',
   'planetlab2_itwm_fhg_de_princeton_coblitz',
   71,
   'PERIOD'),
  ('20110325', 'planetlab4_mini_pw_edu_pl_uw_trs2', 5, 'PERIOD'),
  ('20110325',
   'planetlab-1_amst_nodes_planet-lab_org_arizona_guest28',
   5,
   'PERIOD'),
  ('20110325', 'planet-lab1_ufabc_edu_br_uw_trs2', 5, 'PERIOD'),
  ('20110325', 'planetlab-1_cmcl_cs_cmu_edu_arizona_nest', 5, 'PERIOD'),
  ('20110325', 'planetlab04_cnds_unibe_ch_root', 1, 'PERIOD'),
  ('20110325', 'planetlab-2_cs_ucy_ac_cy_arizona_guest34', 11, 'PERIOD'),
  ('20110325', 'plab-2_sinp_msu_ru_uw_trs2', 5, 'PERIOD'),
  ('20110325', 'gschembra4_diit_unict_it_root', 28, 'PERIOD'),
  ('20110325', 'planetlab3_eecs_umich_edu_umd_scriptroute', 5, 'PERIOD'),
  ('20110325',
   'planetlab3_millennium_berkeley_edu_princeton_coblitz',
   35,
   'PERIOD')],
 '20110322': [('20110322',
   'planetlab-1_amst_nodes_planet-lab_org_arizona_guest28',
   5,
   'PERIOD'),
  ('20110322', 'planetlab1_cs_columbia_edu_purdue_4', 6, 'PERIOD'),
  ('20110322', 'planetlab04_cnds_unibe_ch_root', 1, 'PERIOD'),
  ('20110322', 'planetlab-2_imperial_ac_uk_arizona_nest', 5, 'PERIOD'),
  ('20110322', 'planet1_l3s_uni-hannover_de_arizona_nest', 5, 'PERIOD'),
  ('20110322', 'planetlab-1_cs_uic_edu_root', 1, 'PERIOD'),
  ('20110322', 'planetlab0_otemachi_wide_ad_jp_rnp_dcc_ufjf', 71, 'PERIOD'),
  ('20110322', 'planetlab1_cs_columbia_edu_nus_proxaudio', 6, 'PERIOD'),
  ('20110322', 'planetlab03_mpi-sws_mpg_de_princeton_coblitz', 71, 'PERIOD'),
  ('20110322', 'planetlab1_cs_ucla_edu_root', 1, 'PERIOD'),
  ('20110322', 'planetlab1_cs_columbia_edu_williams_gush', 6, 'PERIOD'),
  ('20110322', 'planetlab1_eee_hku_hk_arizona_owl', 5, 'PERIOD'),
  ('20110322', 'planetlab2_urv_net_arizona_owl', 2, 'PERIOD')],
 '20110306': [('20110306', 'plnode-03_gpolab_bbn_com_root', 1, 'PERIOD'),
  ('20110306', 'planet3_prakinf_tu-ilmenau_de_uw_trs2', 2, 'PERIOD'),
  ('20110306', 'planetlab-2_cs_ucy_ac_cy_princeton_comon', 2, 'PERIOD'),
  ('20110306', 'server2_planetlab_iit-tech_net_root', 5, 'PERIOD'),
  ('20110306', 'planetlab1_unineuchatel_ch_uw_trs2', 2, 'PERIOD'),
  ('20110306', 'planetlab2_cis_upenn_edu_root', 1, 'PERIOD'),
  ('20110306', 'planetlab-2_ssvl_kth_se_howard_p2psip', 5, 'PERIOD'),
  ('20110306',
   'planetlab02_mpi-sws_mpg_de_arizona_stork_install',
   2,
   'PERIOD'),
  ('20110306', 'planetlab2_cs_colorado_edu_uw_trs2', 2, 'PERIOD'),
  ('20110306', 'planetlab1_cs_colorado_edu_uw_trs2', 5, 'PERIOD'),
  ('20110306', 'planetlab1_cnds_jhu_edu_uw_trs2', 5, 'PERIOD'),
  ('20110306', '147-179_surfsnel_dsl_internl_net_uw_trs2', 2, 'PERIOD'),
  ('20110306', 'planetlab2_cs_uiuc_edu_root', 2, 'PERIOD')],
 '20110309': [('20110309',
   'planetlab2_cs_columbia_edu_williams_gush',
   6,
   'PERIOD'),
  ('20110309', 'roam1_cs_ou_edu_root', 2, 'PERIOD'),
  ('20110309', 'plab-2_sinp_msu_ru_root', 1, 'PERIOD'),
  ('20110309', 'jupiter_cs_brown_edu_arizona_digdug2', 5, 'PERIOD'),
  ('20110309', 'planetlab1_millennium_berkeley_edu_purdue_4', 5, 'PERIOD'),
  ('20110309', 'planetlab-1_cs_uic_edu_root', 1, 'PERIOD'),
  ('20110309', 'plab2_engr_sjsu_edu_arizona_nest', 5, 'PERIOD'),
  ('20110309',
   'planet01_hhi_fraunhofer_de_arizona_stork_install',
   5,
   'PERIOD'),
  ('20110309', 'plane-lab-pb1_uni-paderborn_de_root', 7, 'PERIOD'),
  ('20110309', 'planetlab2_cs_columbia_edu_purdue_4', 6, 'PERIOD'),
  ('20110309', 'planet01_hhi_fraunhofer_de_uw_trs2', 5, 'PERIOD'),
  ('20110309', 'planetlab2_hiit_fi_uw_trs2', 5, 'PERIOD'),
  ('20110309', 'planetlab-2_iscte_pt_arizona_stork_install', 5, 'PERIOD'),
  ('20110309', 'cs-planetlab3_cs_surrey_sfu_ca_root', 1, 'PERIOD'),
  ('20110309', 'planetlab2_cs_uiuc_edu_root', 1, 'PERIOD'),
  ('20110309', 'freedom_informatik_rwth-aachen_de_root', 1, 'PERIOD'),
  ('20110309', 'planetlab2_urv_net_arizona_nest', 3, 'PERIOD')],
 '20110412': [('20110412',
   'planetlab2_exp-math_uni-essen_de_arizona_nest',
   5,
   'PERIOD'),
  ('20110412', 'planetlab3_millennium_berkeley_edu_uw_oneswarm', 71, 'PERIOD'),
  ('20110412', 'planetlab2_byu_edu_princeton_codeen', 47, 'PERIOD'),
  ('20110412', 'planetlab4_eecs_umich_edu_uw_oneswarm', 71, 'PERIOD'),
  ('20110412', 'planet1_jaist_ac_jp_arizona_owl', 5, 'PERIOD'),
  ('20110412', 'planetlab2_wiwi_hu-berlin_de_uw_trs2', 5, 'PERIOD'),
  ('20110412', 'planetlab-2_cse_ohio-state_edu_arizona_nest', 5, 'PERIOD'),
  ('20110412', 'planetlab1_aston_ac_uk_princeton_coblitz', 71, 'PERIOD'),
  ('20110412', 'planetslug3_cse_ucsc_edu_arizona_owl', 4, 'PERIOD'),
  ('20110412',
   'planetlab1_millennium_berkeley_edu_princeton_coblitz',
   71,
   'PERIOD'),
  ('20110412', 'planetlab2_hiit_fi_arizona_owl', 5, 'PERIOD'),
  ('20110412',
   'planetlab8_millennium_berkeley_edu_howard_p2psip',
   5,
   'PERIOD'),
  ('20110412', 'planetlab4_goto_info_waseda_ac_jp_root', 1, 'PERIOD'),
  ('20110412', 'planetlab1_pjwstk_edu_pl_pjwstkple_p2pp', 71, 'PERIOD'),
  ('20110412', 'planet1_jaist_ac_jp_howard_p2psip', 5, 'PERIOD')]}
  
  # 结果 (cwc,picp,pinew)
  linear: 0.527541, 0.662745, 0.527541
  bayesian: 0.546594, 0.650980, 0.546594
  catr: 0.937741, 0.521569, 0.315171
  svm: 0.728020, 0.674510, 0.728020
  rfr: 0.461823, 0.619608, 0.375995
  pso-svm: 0.3814049031305741, 0.6862745098039215, 0.3814049031305741
