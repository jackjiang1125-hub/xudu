CREATE TABLE IF NOT EXISTS `wec_building` (
  `id` varchar(32) NOT NULL,
  `building_name` varchar(100) NOT NULL,
  `building_code` varchar(64) NOT NULL,
  `area_id` varchar(32) DEFAULT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sys_org_code` varchar(64) DEFAULT NULL,
  `tenant_id` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_building_code` (`building_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `wec_floor` (
  `id` varchar(32) NOT NULL,
  `floor_name` varchar(100) NOT NULL,
  `floor_code` varchar(64) NOT NULL,
  `building_id` varchar(32) NOT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sys_org_code` varchar(64) DEFAULT NULL,
  `tenant_id` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_floor_code` (`floor_code`),
  KEY `idx_floor_building` (`building_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `wec_room` (
  `id` varchar(32) NOT NULL,
  `room_name` varchar(100) NOT NULL,
  `room_code` varchar(64) NOT NULL,
  `floor_id` varchar(32) NOT NULL,
  `create_by` varchar(50) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_by` varchar(50) DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `sys_org_code` varchar(64) DEFAULT NULL,
  `tenant_id` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_room_code` (`room_code`),
  KEY `idx_room_floor` (`floor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
