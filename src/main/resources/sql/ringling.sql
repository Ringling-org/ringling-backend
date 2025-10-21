ALTER DATABASE ringling
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE user (
    id             INT             AUTO_INCREMENT          COMMENT '유저 고유 ID',
    nickname       VARCHAR(15)     NOT NULL                COMMENT '사용자 닉네임',
    social_id      BIGINT          NOT NULL                COMMENT '소셜 로그인 제공사의 고유 ID',
    social_type    ENUM('KAKAO')   NOT NULL                COMMENT '소셜 로그인 타입',
    fcm_token      VARCHAR(512)    DEFAULT NULL            COMMENT 'FCM 디바이스 토큰',
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

CREATE TABLE ReminderNotification (
    id                     INT          AUTO_INCREMENT PRIMARY KEY COMMENT '알림 고유 ID',
    user_id                INT          NOT NULL COMMENT '유저 고유 ID',
    snap_id                INT          NOT NULL COMMENT '알림 대상 스냅',
    notification_time      DATETIME     NOT NULL COMMENT '알림 예정 시각',
    notification_status    ENUM('PENDING', 'PROCESSING', 'SENT', 'FAILED')
                                        NOT NULL DEFAULT 'PENDING' COMMENT '알림 전송 결과 상태',
    created_at             DATETIME     NOT NULL COMMENT '생성 일시',
    updated_at             DATETIME     NOT NULL COMMENT '수정 일시',

    PRIMARY KEY (id),
    UNIQUE KEY uq_reminder_user_snap (user_id, snap_id),
    CONSTRAINT fk_reminder_user FOREIGN KEY (user_id) REFERENCES user(id),
    CONSTRAINT fk_reminder_snap FOREIGN KEY (snap_id) REFERENCES snap(id)
) COMMENT='리마인더 알림 정보 테이블';

CREATE TABLE NotificationDelivery (
    id                      INT         AUTO_INCREMENT              COMMENT '전송 이력 ID',
    reference_id            INT         NOT NULL                    COMMENT '원본 알림 ID',
    attempted_at            DATETIME    NOT NULL                    COMMENT '전송 시도 일시',
    result                  ENUM('SUCCESS', 'FAILED') NOT NULL        COMMENT '전송 결과',
    error_code              ENUM('UNREGISTERED', 'INVALID_ARGUMENT', 'UNAVAILABLE', 'INTERNAL', 'UNKNOWN')
                                        DEFAULT NULL                COMMENT 'FCM 오류 코드 (Optional)',
    reason                  VARCHAR(255) DEFAULT NULL               COMMENT '실패 사유 (Optional)',
    created_at              DATETIME    NOT NULL                    COMMENT '생성 일시',
    updated_at              DATETIME    NOT NULL                    COMMENT '수정 일시',

    PRIMARY KEY (id)
) COMMENT='알림 전송 이력';
