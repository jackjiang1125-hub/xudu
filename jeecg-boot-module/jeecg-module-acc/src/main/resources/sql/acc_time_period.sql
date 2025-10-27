-- 门禁时间段主表
CREATE TABLE `acc_time_period` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `name` varchar(64) NOT NULL COMMENT '时间段名称',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `sort_order` int DEFAULT NULL COMMENT '排序(从1开始递增)',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_acc_tp_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁时间段主表';