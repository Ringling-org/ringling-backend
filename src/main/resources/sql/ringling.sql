ALTER DATABASE ringling
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_general_ci;

CREATE TABLE summary (
    id              INT             AUTO_INCREMENT,
    url             VARCHAR(512)    NOT NULL,
    summary_title   TEXT,
    summary_status  ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') NOT NULL,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,

    PRIMARY KEY (id),
    UNIQUE  KEY (url)
);

CREATE TABLE snap (
    id              INT             AUTO_INCREMENT,
    summary_id      INT          NOT NULL,
    created_at      DATETIME        NOT NULL,
    updated_at      DATETIME        NOT NULL,
    CONSTRAINT fk_snap_summary  FOREIGN KEY (summary_id) REFERENCES summary(id),

    PRIMARY KEY (id)
);