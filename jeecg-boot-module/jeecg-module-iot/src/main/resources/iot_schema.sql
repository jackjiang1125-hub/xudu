-- -----------------------------------------------------
-- IoT Module table definitions generated from entity models
-- -----------------------------------------------------

-- Table: iot_device
CREATE TABLE IF NOT EXISTS iot_device (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    sn              VARCHAR(64)     NOT NULL COMMENT 'Device serial number',
    device_type     VARCHAR(32)     NULL COMMENT 'Device type',
    device_name     VARCHAR(128)    NULL COMMENT 'Device name',
    firmware_version VARCHAR(64)    NULL COMMENT 'Firmware version',
    push_version    VARCHAR(32)     NULL COMMENT 'Push protocol version',
    lock_count      INT             NULL COMMENT 'Supported lock count',
    reader_count    INT             NULL COMMENT 'Supported reader count',
    machine_type    INT             NULL COMMENT 'Machine type identifier',
    ip_address      VARCHAR(64)     NULL COMMENT 'Configured IPv4 address',
    gateway_ip      VARCHAR(64)     NULL COMMENT 'Configured gateway IPv4 address',
    net_mask        VARCHAR(64)     NULL COMMENT 'Netmask',
    init_payload    TEXT            NULL COMMENT 'Raw initialization payload',
    registry_payload TEXT           NULL COMMENT 'Raw registry payload',
    last_known_ip   VARCHAR(64)     NULL COMMENT 'Last known IPv4 address',
    last_init_time  DATETIME        NULL COMMENT 'Last initialization handshake time',
    last_registry_time DATETIME     NULL COMMENT 'Last registry time',
    last_heartbeat_time DATETIME    NULL COMMENT 'Last heartbeat time',
    status          VARCHAR(32)     NULL COMMENT 'Authorization status',
    authorized      TINYINT(1)      NULL DEFAULT 0 COMMENT 'Whether the device is authorized',
    registry_code   VARCHAR(64)     NULL COMMENT 'Registry code returned to device',
    remark          VARCHAR(255)    NULL COMMENT 'Remark',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT devices';
CREATE UNIQUE INDEX idx_iot_device_sn ON iot_device (sn);

-- Table: iot_device_command
CREATE TABLE IF NOT EXISTS iot_device_command (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    sn              VARCHAR(64)     NOT NULL COMMENT 'Device serial number',
    command_code    VARCHAR(64)     NULL COMMENT 'Command identifier',
    command_content TEXT            NULL COMMENT 'Command payload',
    status          VARCHAR(32)     NULL COMMENT 'Lifecycle status',
    enqueue_time    DATETIME        NULL COMMENT 'Enqueue time',
    sent_time       DATETIME        NULL COMMENT 'Sent time',
    ack_time        DATETIME        NULL COMMENT 'Acknowledgement time',
    result_code     VARCHAR(64)     NULL COMMENT 'Result code from device',
    result_message  VARCHAR(255)    NULL COMMENT 'Result message from device',
    last_report_payload TEXT        NULL COMMENT 'Latest report payload',
    last_report_ip  VARCHAR(64)     NULL COMMENT 'Latest report IP address',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Commands queued for IoT devices';
CREATE INDEX idx_iot_device_command_sn ON iot_device_command (sn);
CREATE INDEX idx_iot_device_command_code ON iot_device_command (command_code);

-- Table: iot_device_command_report
CREATE TABLE IF NOT EXISTS iot_device_command_report (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    sn              VARCHAR(64)     NOT NULL COMMENT 'Device serial number',
    command_id      VARCHAR(64)     NULL COMMENT 'Command ID',
    command_content TEXT            NULL COMMENT 'Command content',
    result_code     VARCHAR(64)     NULL COMMENT 'Execution result code',
    result_message  VARCHAR(255)    NULL COMMENT 'Execution result message',
    report_time     DATETIME        NULL COMMENT 'Report timestamp',
    raw_payload     TEXT            NULL COMMENT 'Raw report payload',
    client_ip       VARCHAR(64)     NULL COMMENT 'Client IP address',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Command execution reports from IoT devices';
CREATE INDEX idx_iot_device_command_report_sn ON iot_device_command_report (sn);
CREATE INDEX idx_iot_device_command_report_cmd_id ON iot_device_command_report (command_id);

-- Table: iot_device_options
CREATE TABLE IF NOT EXISTS iot_device_options (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    device_id       VARCHAR(32)     NULL COMMENT '关联设备ID',
    sn              VARCHAR(64)     NULL COMMENT 'Device serial number',
    param_name      VARCHAR(128)    NULL COMMENT 'Parameter name',
    param_value     VARCHAR(512)    NULL COMMENT 'Parameter value',
    param_type      VARCHAR(32)     NULL COMMENT 'Parameter type',
    report_time     DATETIME        NULL COMMENT 'Report time',
    raw_payload     TEXT            NULL COMMENT 'Raw payload',
    client_ip       VARCHAR(64)     NULL COMMENT 'Client IP address',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT device reported options';
CREATE INDEX idx_iot_device_options_sn ON iot_device_options (sn);
CREATE INDEX idx_iot_device_options_name ON iot_device_options (param_name);

-- Table: iot_device_photo
CREATE TABLE IF NOT EXISTS iot_device_photo (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    sn              VARCHAR(64)     NULL COMMENT 'Device serial number',
    pin             VARCHAR(32)     NULL COMMENT 'User PIN',
    photo_name      VARCHAR(255)    NULL COMMENT 'Photo file name',
    file_size       INT             NULL COMMENT 'Photo file size',
    photo_base64    LONGTEXT        NULL COMMENT 'Photo data in Base64',
    photo_path      VARCHAR(255)    NULL COMMENT 'Stored photo path',
    uploaded_time   DATETIME        NULL COMMENT 'Upload time',
    raw_payload     MEDIUMTEXT      NULL COMMENT 'Raw payload',
    client_ip       VARCHAR(64)     NULL COMMENT 'Client IP address',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT device uploaded photos';
CREATE INDEX idx_iot_device_photo_sn ON iot_device_photo (sn);
CREATE INDEX idx_iot_device_photo_pin ON iot_device_photo (pin);

-- Table: iot_device_rtlog
CREATE TABLE IF NOT EXISTS iot_device_rtlog (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    sn              VARCHAR(64)     NULL COMMENT 'Device serial number',
    log_time        DATETIME        NULL COMMENT 'Record time',
    pin             VARCHAR(32)     NULL COMMENT 'User PIN',
    card_no         VARCHAR(64)     NULL COMMENT 'Card number',
    event_addr      INT             NULL COMMENT 'Event address',
    event_code      INT             NULL COMMENT 'Event code',
    inout_status    INT             NULL COMMENT 'In/out status',
    verify_type     INT             NULL COMMENT 'Verify type',
    record_index    INT             NULL COMMENT 'Record index',
    site_code       INT             NULL COMMENT 'Site code',
    link_id         INT             NULL COMMENT 'Link ID',
    mask_flag       INT             NULL COMMENT 'Mask flag',
    temperature     INT             NULL COMMENT 'Temperature',
    conv_temperature INT            NULL COMMENT 'Converted temperature',
    raw_payload     MEDIUMTEXT      NULL COMMENT 'Raw payload',
    client_ip       VARCHAR(64)     NULL COMMENT 'Client IP address',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT device real-time logs';
CREATE INDEX idx_iot_device_rtlog_sn ON iot_device_rtlog (sn);
CREATE INDEX idx_iot_device_rtlog_log_time ON iot_device_rtlog (log_time);

-- Table: iot_device_state
CREATE TABLE IF NOT EXISTS iot_device_state (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    sn              VARCHAR(64)     NULL COMMENT 'Device serial number',
    log_time        DATETIME        NULL COMMENT 'Record time',
    sensor          VARCHAR(255)    NULL COMMENT 'Sensor status',
    relay           VARCHAR(255)    NULL COMMENT 'Relay status',
    alarm           VARCHAR(255)    NULL COMMENT 'Alarm status',
    door            VARCHAR(255)    NULL COMMENT 'Door status',
    raw_payload     TEXT            NULL COMMENT 'Raw payload',
    client_ip       VARCHAR(64)     NULL COMMENT 'Client IP address',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='IoT device state snapshots';
CREATE INDEX idx_iot_device_state_sn ON iot_device_state (sn);
CREATE INDEX idx_iot_device_state_log_time ON iot_device_state (log_time);

