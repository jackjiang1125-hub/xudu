-- 门禁时间段每日明细表
CREATE TABLE `acc_time_period_detail` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `period_id` varchar(32) NOT NULL COMMENT '时间段ID',
  `day_key` varchar(20) NOT NULL COMMENT '日期键(mon/tue/.../holiday1/2/3)',
  `label` varchar(50) DEFAULT NULL COMMENT '显示标签',
  `segment1_start` varchar(8) DEFAULT '00:00' COMMENT '区间1开始',
  `segment1_end` varchar(8) DEFAULT '00:00' COMMENT '区间1结束',
  `segment2_start` varchar(8) DEFAULT '00:00' COMMENT '区间2开始',
  `segment2_end` varchar(8) DEFAULT '00:00' COMMENT '区间2结束',
  `segment3_start` varchar(8) DEFAULT '00:00' COMMENT '区间3开始',
  `segment3_end` varchar(8) DEFAULT '00:00' COMMENT '区间3结束',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_period` (`period_id`),
  UNIQUE KEY `uk_period_day` (`period_id`, `day_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁时间段每日明细表';