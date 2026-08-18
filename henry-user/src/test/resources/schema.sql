-- H2（MySQL 兼容模式）下的用户表结构，与 DB/2026-08-18.sql 的列一一对应
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id            BIGINT        NOT NULL,
    username      VARCHAR(50)   NOT NULL,
    password      VARCHAR(100)  NOT NULL,
    nickname      VARCHAR(50),
    status        TINYINT       NOT NULL DEFAULT 1,
    creator_id    BIGINT        NOT NULL DEFAULT 0,
    creator_name  VARCHAR(255)  NOT NULL DEFAULT 'system',
    created_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    modifier_id   BIGINT,
    modifier_name VARCHAR(255),
    modified_time TIMESTAMP,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
);
CREATE INDEX username_index ON sys_user (username);
