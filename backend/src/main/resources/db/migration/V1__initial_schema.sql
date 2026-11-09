-- Initial HireForge schema for a fresh MySQL database.
-- Existing databases with application data must be backed up before any schema change.

CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE IF NOT EXISTS job_applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    company VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    job_description TEXT NULL,
    application_date DATE NOT NULL,
    notes TEXT NULL,
    user_id BIGINT NULL,
    PRIMARY KEY (id),
    KEY idx_job_applications_user_id (user_id),
    CONSTRAINT fk_job_applications_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
