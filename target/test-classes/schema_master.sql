CREATE TABLE IF NOT EXISTS `tenants` (
    `id` varchar(36) NOT NULL,
    `name` varchar(100) NOT NULL,
    `db_name` varchar(100) NOT NULL,
    `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated` timestamp,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_tenants_name` (`name`),
    UNIQUE KEY `uk_tenants_db_name` (`db_name`)
);

CREATE TABLE IF NOT EXISTS `users` (
    `id` varchar(36) NOT NULL,
    `tenant_id` varchar(36) NOT NULL,
    `email` varchar(255) NOT NULL,
    `password_hash` varchar(255) NOT NULL,
    `first_name` varchar(100),
    `last_name` varchar(100),
    `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated` timestamp,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users_email` (`email`),
    CONSTRAINT `fk_users_tenant_id` FOREIGN KEY (`tenant_id`) REFERENCES `tenants` (`id`)
);

CREATE TABLE IF NOT EXISTS `user_sessions` (
    `id` varchar(36) NOT NULL,
    `user_id` varchar(36) NOT NULL,
    `token` varchar(255) NOT NULL,
    `session_start` timestamp NOT NULL,
    `client_info` varchar(255),
    `active` boolean NOT NULL DEFAULT true,
    `invalidated_by` varchar(36),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_sessions_token` (`token`),
    CONSTRAINT `fk_user_sessions_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);

CREATE TABLE IF NOT EXISTS `currency_codes` (
    `code` varchar(3) NOT NULL,
    `name` varchar(100) NOT NULL,
    PRIMARY KEY (`code`)
);
