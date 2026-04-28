CREATE TABLE `categories` (
    `id` varchar(36) NOT NULL,
    `tenant_id` varchar(36) NOT NULL,
    `parent_id` varchar(36) DEFAULT NULL,
    `name` varchar(100) NOT NULL,
    `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated` timestamp,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_categories_parent_id` FOREIGN KEY (`parent_id`) REFERENCES `categories` (`id`)
);

CREATE TABLE `accounts` (
    `id` varchar(36) NOT NULL,
    `tenant_id` varchar(36) NOT NULL,
    `name` varchar(100) NOT NULL,
    `type` ENUM('CREDIT', 'BANK', 'LOAN', 'CASH', 'GIFT_CARD') NOT NULL,
    `currency` varchar(3) NOT NULL,
    `balance` decimal(19,4) NOT NULL DEFAULT '0.0000',
    `status` ENUM('ACTIVE', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
    `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated` timestamp,
    PRIMARY KEY (`id`)
);

CREATE TABLE `transactions` (
    `id` varchar(36) NOT NULL,
    `tenant_id` varchar(36) NOT NULL,
    `account_id` varchar(36) NOT NULL,
    `to_account_id` varchar(36) DEFAULT NULL,
    `category_id` varchar(36) DEFAULT NULL,
    `amount` decimal(19,4) NOT NULL,
    `type` ENUM('INCOME', 'SPENDING', 'TRANSFER') NOT NULL,
    `description` varchar(255),
    `transaction_date` timestamp NOT NULL,
    `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated` timestamp,
    PRIMARY KEY (`id`),
    CONSTRAINT `fk_transactions_account_id` FOREIGN KEY (`account_id`) REFERENCES `accounts` (`id`),
    CONSTRAINT `fk_transactions_to_account_id` FOREIGN KEY (`to_account_id`) REFERENCES `accounts` (`id`),
    CONSTRAINT `fk_transactions_category_id` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
);
