-- 用户表初始化脚本
CREATE TABLE sys_user (
    `id`            BIGINT UNSIGNED  NOT NULL COMMENT '主键',
    `username`      VARCHAR(50)      NOT NULL COMMENT '用户名',
    `password`      VARCHAR(100)     NOT NULL COMMENT '密码（BCrypt 加盐哈希）',
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

-- 种子用户（密码为 BCrypt 哈希，对应明文 admin123 / test123）
INSERT INTO sys_user (`id`, `username`, `password`, `nickname`, `status`, `creator_id`, `creator_name`, `created_time`) VALUES
(1, 'admin', '$2a$10$Q1/yGotbo0klPVrenzQZVOeTQag4Y.mRBGKNwuCIK.spY/C9kRNYm', '管理员', 1, 0, 'system', NOW()),
(2, 'test', '$2a$10$5mgOIs0e2HlsRWrVYqqzs.6HuCWHIhL0KlBqOIl6djIvBvpkGGmrS', '测试用户', 1, 0, 'system', NOW());
