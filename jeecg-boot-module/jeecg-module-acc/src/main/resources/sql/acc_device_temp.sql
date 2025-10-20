-- 门禁设备临时表
CREATE TABLE IF NOT EXISTS `acc_device_temp` (
  `id` varchar(32) NOT NULL COMMENT '主键ID',
  `create_by` varchar(32) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `sn` varchar(100) NOT NULL COMMENT '设备序列号',
  `device_name` varchar(200) DEFAULT NULL COMMENT '设备名称',
  `is_reboot` tinyint(1) DEFAULT '0' COMMENT '是否重启',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_acc_device_temp_sn` (`sn`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备临时表';