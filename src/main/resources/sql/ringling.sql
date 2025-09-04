ALTER DATABASE ringling
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE user (
    id             INT             AUTO_INCREMENT          COMMENT '유저 고유 ID',
    nickname       VARCHAR(15)     NOT NULL                COMMENT '사용자 닉네임',
    social_id      BIGINT          NOT NULL                COMMENT '소셜 로그인 제공사의 고유 ID',
    social_type    ENUM('KAKAO')   NOT NULL                COMMENT '소셜 로그인 타입',
    refresh_token  VARCHAR(255)    DEFAULT NULL            COMMENT '리프레시 토큰',
    last_login_at  DATETIME        DEFAULT NULL            COMMENT '마지막 로그인 일시',
    created_at     DATETIME        NOT NULL                COMMENT '생성 일시',
    updated_at     DATETIME        NOT NULL                COMMENT '수정 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uk_member_social_id (social_id)
) COMMENT='사용자 정보 테이블';

CREATE TABLE summary (
    id              INT             AUTO_INCREMENT                COMMENT '요약 고유 ID',
    url             VARCHAR(512)    NOT NULL                      COMMENT '원본 URL',
    summary_title   TEXT                                          COMMENT '요약 제목',
    summary_status  ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL COMMENT '요약 처리 상태',
    created_at      DATETIME        NOT NULL                      COMMENT '생성 일시',
    updated_at      DATETIME        NOT NULL                      COMMENT '수정 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uq_summary_url (url)
) COMMENT='요약 정보 테이블';

CREATE TABLE snap (
    id              INT             AUTO_INCREMENT      COMMENT '스냅샷 고유 ID',
    summary_id      INT             NOT NULL            COMMENT '요약 ID(FK)',
    user_id			INT				NOT NULL			COMMENT '유저 고유 ID',
    created_at      DATETIME        NOT NULL            COMMENT '생성 일시',
    updated_at      DATETIME        NOT NULL            COMMENT '수정 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uq_snap_user_summary (user_id, summary_id),
    CONSTRAINT fk_snap_summary FOREIGN KEY (summary_id) REFERENCES summary(id),
    CONSTRAINT fk_snap_user    FOREIGN KEY (user_id)    REFERENCES user(id)
) COMMENT='스냅샷 정보 테이블';