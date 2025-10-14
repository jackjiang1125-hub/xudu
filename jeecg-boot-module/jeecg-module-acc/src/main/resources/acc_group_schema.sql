-- -----------------------------------------------------
-- ACC Module - 权限组相关表定义
-- -----------------------------------------------------

-- Table: acc_group
CREATE TABLE IF NOT EXISTS acc_group (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    group_name      VARCHAR(64)     NOT NULL COMMENT '权限组名称',
    period_id       VARCHAR(32)     NULL COMMENT '关联时间段ID',
    remark          VARCHAR(255)    NULL COMMENT '备注',
    PRIMARY KEY (id),
    INDEX idx_acc_group_period (period_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='门禁权限组';

-- Table: acc_group_member
CREATE TABLE IF NOT EXISTS acc_group_member (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    group_id        VARCHAR(32)     NOT NULL COMMENT '权限组ID',
    member_id       VARCHAR(64)     NOT NULL COMMENT '人员ID',
    PRIMARY KEY (id),
    INDEX idx_acc_group_member_gid (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限组-人员关联';

-- Table: acc_group_device
CREATE TABLE IF NOT EXISTS acc_group_device (
    id              VARCHAR(32)     NOT NULL COMMENT 'Primary key',
    create_by       VARCHAR(32)     NULL COMMENT '创建人',
    create_time     DATETIME        NULL COMMENT '创建时间',
    update_by       VARCHAR(32)     NULL COMMENT '更新人',
    update_time     DATETIME        NULL COMMENT '更新时间',
    group_id        VARCHAR(32)     NOT NULL COMMENT '权限组ID',
    device_id       VARCHAR(64)     NOT NULL COMMENT '设备ID',
    PRIMARY KEY (id),
    INDEX idx_acc_group_device_gid (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限组-设备关联';