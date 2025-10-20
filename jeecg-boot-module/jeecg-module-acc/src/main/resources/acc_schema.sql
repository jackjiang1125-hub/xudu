-- -----------------------------------------------------
-- ACC Module table definitions generated from entity models
-- -----------------------------------------------------

-- Table: acc_device
CREATE TABLE IF NOT EXISTS acc_device (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    sn              VARCHAR(64)     NOT NULL COMMENT '设备序列号',
    device_type     VARCHAR(32)     NULL COMMENT '设备类型',
    device_name     VARCHAR(128)    NULL COMMENT '设备名称',
    firmware_version VARCHAR(64)    NULL COMMENT '固件版本',
    push_version    VARCHAR(32)     NULL COMMENT '推送协议版本',
    lock_count      INT             NULL COMMENT '支持的锁数量',
    reader_count    INT             NULL COMMENT '支持的读头数量',
    machine_type    INT             NULL COMMENT '机器类型标识',
    ip_address      VARCHAR(64)     NULL COMMENT 'IPv4地址',
    gateway_ip      VARCHAR(64)     NULL COMMENT '网关IPv4地址',
    net_mask        VARCHAR(64)     NULL COMMENT '子网掩码',
    init_payload    TEXT            NULL COMMENT '原始初始化请求载荷',
    registry_payload TEXT           NULL COMMENT '原始注册载荷',
    last_known_ip   VARCHAR(64)     NULL COMMENT '最后检测到的IPv4地址',
    last_init_time  DATETIME        NULL COMMENT '最后初始化握手时间',
    last_registry_time DATETIME     NULL COMMENT '最后注册时间',
    last_heartbeat_time DATETIME    NULL COMMENT '最后心跳时间',
    status          VARCHAR(32)     NULL COMMENT '授权状态',
    authorized      TINYINT(1)      NULL DEFAULT 0 COMMENT '是否已授权',
    registry_code   VARCHAR(64)     NULL COMMENT '注册码',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备';
CREATE UNIQUE INDEX idx_acc_device_sn ON acc_device (sn);

-- Table: acc_device_options (可扩展设备管理字段存储)
CREATE TABLE IF NOT EXISTS acc_device_options (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    device_id       VARCHAR(32)     NULL COMMENT '关联 acc_device.id',
    sn              VARCHAR(64)     NOT NULL COMMENT '设备序列号',
    option_key      VARCHAR(64)     NOT NULL COMMENT '选项键（如 resetRequired）',
    option_value    VARCHAR(512)    NULL COMMENT '选项值（字符串存储）',
    option_type     VARCHAR(32)     NULL COMMENT '选项类型（STRING/BOOLEAN/NUMBER/JSON）',
    PRIMARY KEY (id),
    INDEX idx_acc_device_options_sn (sn),
    UNIQUE KEY uk_acc_device_option (sn, option_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁设备选项键值表';

