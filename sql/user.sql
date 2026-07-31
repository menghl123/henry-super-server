-- 用户服务数据库初始化脚本（演示用）
-- 注意：示例密码为明文，生产环境务必使用 BCrypt 等哈希算法存储

CREATE DATABASE IF NOT EXISTS henry_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE henry_user;

DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    username    VARCHAR(50)  NOT NULL COMMENT '用户名',
    password    VARCHAR(100) NOT NULL COMMENT '密码（演示用明文，生产建议 BCrypt）',
    nickname    VARCHAR(50)  DEFAULT NULL COMMENT '昵称',
    status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    deleted     TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-未删 1-已删',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '用户表';

INSERT INTO sys_user (username, password, nickname) VALUES
('admin', 'admin123', '管理员'),
('test', 'test123', '测试用户');
