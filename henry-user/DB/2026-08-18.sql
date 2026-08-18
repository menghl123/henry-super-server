-- 用户表初始化脚本
CREATE TABLE sys_user (
    `id`            BIGINT UNSIGNED  NOT NULL COMMENT '主键',
    `username`      VARCHAR(50)      NOT NULL COMMENT '用户名',
    `password`      VARCHAR(100)     NOT NULL COMMENT '密码（明文演示，生产建议 BCrypt）',
    `nickname`      VARCHAR(50)      COMMENT '昵称',
    `status`        TINYINT UNSIGNED NOT NULL DEFAULT 1 COMMENT '状态 1-正常 0-禁用',
    `creator_id`    BIGINT UNSIGNED  NOT NULL COMMENT '创建人',
    `creator_name`  VARCHAR(255)     NOT NULL COMMENT '创建人信息',
    `created_time`  DATETIME         NOT NULL COMMENT '创建时间',
    `modifier_id`   BIGINT UNSIGNED  COMMENT '更新人',
    `modifier_name` VARCHAR(255)     COMMENT '更新人信息',
    `modified_time` DATETIME         COMMENT '更新时间',
    `deleted`       TINYINT UNSIGNED NOT NULL DEFAULT "0" COMMENT '逻辑删除',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='用户表' CHARSET=utf8mb4 collate = utf8mb4_unicode_ci;
CREATE INDEX `username_index` on sys_user (`username`);
