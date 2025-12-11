
CREATE TABLE `wec_consume_record` (
  `id` varchar(36) NOT NULL,
  `trade_no` varchar(64) DEFAULT NULL COMMENT '交易号',
  `card_no` varchar(32) DEFAULT NULL COMMENT '卡号',
  `user_id` varchar(36) DEFAULT NULL COMMENT '用户ID',
  `user_name` varchar(100) DEFAULT NULL COMMENT '用户姓名',
  `device_id` varchar(36) DEFAULT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `amount` decimal(10,2) DEFAULT NULL COMMENT '金额',
  `balance` decimal(10,2) DEFAULT NULL COMMENT '余额',
  `type` varchar(10) DEFAULT NULL COMMENT '类型(1:消费 2:充值 3:退款)',
  `status` varchar(10) DEFAULT NULL COMMENT '状态(1:成功 0:失败)',
  `consume_time` datetime DEFAULT NULL COMMENT '交易时间',
  `record_no` int(11) DEFAULT NULL COMMENT '设备记录序号',
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `sys_org_code` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录';
