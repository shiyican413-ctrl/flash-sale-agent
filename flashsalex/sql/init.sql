-- FlashSaleX 数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS flashsalex DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE flashsalex;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    role VARCHAR(32) NOT NULL DEFAULT 'USER',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-正常 0-禁用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username),
    UNIQUE KEY uk_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS t_goods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_name VARCHAR(128) NOT NULL,
    goods_desc VARCHAR(512),
    original_price DECIMAL(10,2) NOT NULL,
    cover_url VARCHAR(512),
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-上架 0-下架',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 秒杀活动表
CREATE TABLE IF NOT EXISTS t_seckill_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    goods_id BIGINT NOT NULL,
    activity_name VARCHAR(128) NOT NULL,
    seckill_price DECIMAL(10,2) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT NOT NULL DEFAULT 0 COMMENT '0-草稿 1-待开始 2-进行中 3-已结束 4-已下线',
    per_user_limit INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_goods_id (goods_id),
    KEY idx_time_status (start_time, end_time, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

-- 秒杀库存表
CREATE TABLE IF NOT EXISTS t_seckill_stock (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    activity_id BIGINT NOT NULL,
    total_stock INT NOT NULL,
    available_stock INT NOT NULL,
    locked_stock INT NOT NULL DEFAULT 0,
    sold_stock INT NOT NULL DEFAULT 0,
    version INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_activity_id (activity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀库存表';

-- 订单表
CREATE TABLE IF NOT EXISTS t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    request_id VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    activity_id BIGINT NOT NULL,
    goods_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    pay_amount DECIMAL(10,2) NOT NULL,
    status VARCHAR(32) NOT NULL COMMENT 'WAIT_PAY/PAID/CANCELED/REFUNDED',
    expire_time DATETIME NOT NULL,
    paid_at DATETIME NULL,
    canceled_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_request_id (request_id),
    UNIQUE KEY uk_user_activity (user_id, activity_id),
    KEY idx_user_id (user_id),
    KEY idx_activity_id (activity_id),
    KEY idx_status_expire (status, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 库存流水表
CREATE TABLE IF NOT EXISTS t_stock_flow (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    flow_no VARCHAR(64) NOT NULL,
    activity_id BIGINT NOT NULL,
    order_no VARCHAR(64),
    user_id BIGINT,
    change_type VARCHAR(32) NOT NULL COMMENT 'PRE_DEDUCT/ORDER_CREATED/PAY_SUCCESS/CANCEL_RESTORE/COMPENSATE',
    change_quantity INT NOT NULL,
    before_stock INT,
    after_stock INT,
    remark VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_flow_no (flow_no),
    KEY idx_activity_id (activity_id),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存流水表';

-- MQ 消息幂等表
CREATE TABLE IF NOT EXISTS t_message_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    message_id VARCHAR(64) NOT NULL,
    business_type VARCHAR(64) NOT NULL,
    business_key VARCHAR(128) NOT NULL,
    status VARCHAR(32) NOT NULL COMMENT 'PROCESSING/SUCCESS/FAILED',
    retry_count INT NOT NULL DEFAULT 0,
    error_message VARCHAR(1024),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_message_id (message_id),
    KEY idx_business_key (business_key),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息幂等记录表';

-- 黑名单表
CREATE TABLE IF NOT EXISTS t_risk_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_type VARCHAR(32) NOT NULL COMMENT 'USER/IP',
    target_value VARCHAR(128) NOT NULL,
    reason VARCHAR(255),
    expire_time DATETIME,
    status TINYINT NOT NULL DEFAULT 1 COMMENT '1-生效 0-失效',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_target (target_type, target_value),
    KEY idx_status_expire (status, expire_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风控黑名单表';

-- Agent 分析任务表
CREATE TABLE IF NOT EXISTS t_agent_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_no VARCHAR(64) NOT NULL,
    task_type VARCHAR(64) NOT NULL COMMENT 'ACTIVITY_PRECHECK/STOCK_RISK/ABNORMAL_ORDER/PRESSURE_REPORT',
    input_json JSON NOT NULL,
    output_text TEXT,
    status VARCHAR(32) NOT NULL COMMENT 'PROCESSING/SUCCESS/FAILED',
    created_by BIGINT,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_no (task_no),
    KEY idx_task_type (task_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Agent分析任务表';

-- 初始化管理员用户 (密码: admin123, BCrypt加密)
INSERT INTO t_user (username, password_hash, role, status)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'ADMIN', 1);
