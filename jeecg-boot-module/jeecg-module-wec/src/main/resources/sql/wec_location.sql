CREATE TABLE `wec_location` (
  `id` varchar(32) NOT NULL,
  `location_name` varchar(100) NOT NULL,
  `remark` varchar(255) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sys_org_code` varchar(64) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
