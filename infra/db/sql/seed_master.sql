-- Seed Tenants
INSERT INTO `tenants` (`id`, `name`, `db_name`, `created`) VALUES ('family_smith_id', 'The Smith Family', 'finny_family_smith_id', NOW());
INSERT INTO `tenants` (`id`, `name`, `db_name`, `created`) VALUES ('family_jones_id', 'The Jones Family', 'finny_family_jones_id', NOW());

-- Seed Users for Smith Family
INSERT INTO `users` (`id`, `tenant_id`, `email`, `password_hash`, `first_name`, `last_name`, `created`) VALUES ('user_john_id', 'family_smith_id', 'john.smith@example.com', 'password', 'John', 'Smith', NOW());
INSERT INTO `users` (`id`, `tenant_id`, `email`, `password_hash`, `first_name`, `last_name`, `created`) VALUES ('user_jane_id', 'family_smith_id', 'jane.smith@example.com', 'password', 'Jane', 'Smith', NOW());

-- Seed User for Jones Family
INSERT INTO `users` (`id`, `tenant_id`, `email`, `password_hash`, `first_name`, `last_name`, `created`) VALUES ('user_bob_id', 'family_jones_id', 'bob.jones@example.com', 'password', 'Bob', 'Jones', NOW());

-- Seed Currency Codes
INSERT INTO `currency_codes` (`code`, `name`) VALUES ('USD', 'US Dollar');
INSERT INTO `currency_codes` (`code`, `name`) VALUES ('EUR', 'Euro');
INSERT INTO `currency_codes` (`code`, `name`) VALUES ('GBP', 'British Pound');
INSERT INTO `currency_codes` (`code`, `name`) VALUES ('INR', 'Indian Rupee');
INSERT INTO `currency_codes` (`code`, `name`) VALUES ('CAD', 'Canadian Dollar');
