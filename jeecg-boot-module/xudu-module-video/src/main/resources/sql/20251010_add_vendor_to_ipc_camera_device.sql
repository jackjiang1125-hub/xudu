ALTER TABLE `ipc_camera_device`
  ADD COLUMN `vendor` varchar(64) DEFAULT NULL COMMENT '厂商标识' AFTER `camera_type`;
