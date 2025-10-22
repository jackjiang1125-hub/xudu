-- ONVIF enhancement: add self reference and metadata columns to ipc_camera_device
ALTER TABLE `ipc_camera_device`
  ADD COLUMN `parent_id` varchar(32) DEFAULT NULL COMMENT 'Parent device id when representing an NVR channel' AFTER `webrtc_api`,
  ADD COLUMN `role` varchar(32) DEFAULT 'camera' COMMENT 'Device role (nvr/channel/camera)' AFTER `parent_id`,
  ADD COLUMN `channel_index` int DEFAULT NULL COMMENT 'Channel index for NVR channel devices' AFTER `role`,
  ADD COLUMN `source_token` varchar(128) DEFAULT NULL COMMENT 'ONVIF source token' AFTER `channel_index`,
  ADD COLUMN `profile_token` varchar(128) DEFAULT NULL COMMENT 'Selected ONVIF profile token' AFTER `source_token`,
  ADD COLUMN `profile_kind` varchar(32) DEFAULT NULL COMMENT 'ONVIF profile kind' AFTER `profile_token`,
  ADD COLUMN `profile_name` varchar(128) DEFAULT NULL COMMENT 'ONVIF profile name' AFTER `profile_kind`,
  ADD COLUMN `onvif_rtsp_url` varchar(255) DEFAULT NULL COMMENT 'RTSP url discovered from ONVIF service' AFTER `profile_name`,
  ADD COLUMN `media_version` int DEFAULT NULL COMMENT 'ONVIF media service version' AFTER `onvif_rtsp_url`,
  ADD COLUMN `channel_count` int DEFAULT NULL COMMENT 'Total channels reported by ONVIF service' AFTER `media_version`,
  ADD COLUMN `state_hash` varchar(128) DEFAULT NULL COMMENT 'ONVIF device state hash' AFTER `channel_count`,
  ADD COLUMN `device_manufacturer` varchar(128) DEFAULT NULL COMMENT 'ONVIF device manufacturer' AFTER `state_hash`,
  ADD COLUMN `device_model` varchar(128) DEFAULT NULL COMMENT 'ONVIF device model' AFTER `device_manufacturer`,
  ADD COLUMN `device_firmware_version` varchar(128) DEFAULT NULL COMMENT 'ONVIF device firmware version' AFTER `device_model`,
  ADD COLUMN `device_serial_number` varchar(128) DEFAULT NULL COMMENT 'ONVIF device serial number' AFTER `device_firmware_version`,
  ADD COLUMN `device_hardware_id` varchar(128) DEFAULT NULL COMMENT 'ONVIF device hardware id' AFTER `device_serial_number`,
  ADD COLUMN `capabilities_json` text COMMENT 'Serialized ONVIF capabilities payload' AFTER `device_hardware_id`,
  ADD COLUMN `raw_onvif_payload` longtext COMMENT 'Full ONVIF payload snapshot' AFTER `capabilities_json`,
  ADD COLUMN `onvif_username` varchar(128) DEFAULT NULL COMMENT 'ONVIF service username' AFTER `raw_onvif_payload`,
  ADD COLUMN `onvif_password` varchar(128) DEFAULT NULL COMMENT 'ONVIF service password' AFTER `onvif_username`;

ALTER TABLE `ipc_camera_device`
  ADD KEY `idx_ipc_camera_device_parent` (`parent_id`),
  ADD KEY `idx_ipc_camera_device_role` (`role`),
  ADD KEY `idx_ipc_camera_device_source` (`source_token`);

ALTER TABLE `ipc_camera_device`
  ADD CONSTRAINT `fk_ipc_camera_device_parent`
    FOREIGN KEY (`parent_id`) REFERENCES `ipc_camera_device` (`id`)
    ON DELETE SET NULL ON UPDATE CASCADE;
