CREATE TABLE IF NOT EXISTS order_outbox_message (
  id BIGINT NOT NULL,
  msg_key VARCHAR(128) NOT NULL,
  topic VARCHAR(128) NOT NULL,
  payload TEXT NOT NULL,
  status TINYINT NOT NULL DEFAULT 0 COMMENT '0-pending,1-sent,2-dead',
  retry_count INT NOT NULL DEFAULT 0,
  next_retry_time DATETIME NOT NULL,
  created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_msg_key (msg_key),
  KEY idx_status_next_retry (status, next_retry_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

