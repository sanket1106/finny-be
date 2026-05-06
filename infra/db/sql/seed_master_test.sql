-- Seed master test data
-- Ensure tables exist (Hibernate might not have created them yet if @Sql runs early)
CREATE TABLE IF NOT EXISTS currency_codes (code VARCHAR(3) PRIMARY KEY, name VARCHAR(100));
CREATE TABLE IF NOT EXISTS tenants (id VARCHAR(36) PRIMARY KEY, name VARCHAR(100), db_name VARCHAR(100));
CREATE TABLE IF NOT EXISTS users (id VARCHAR(36) PRIMARY KEY, tenant_id VARCHAR(36), email VARCHAR(100), password_hash VARCHAR(255), first_name VARCHAR(100), last_name VARCHAR(100));
CREATE TABLE IF NOT EXISTS user_sessions (id BIGINT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(36), token VARCHAR(255), active BOOLEAN, created TIMESTAMP DEFAULT CURRENT_TIMESTAMP);

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE user_sessions;
TRUNCATE TABLE users;
TRUNCATE TABLE tenants;
TRUNCATE TABLE currency_codes;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO currency_codes (code, name) VALUES ('USD', 'US Dollar');
INSERT INTO currency_codes (code, name) VALUES ('EUR', 'Euro');
INSERT INTO currency_codes (code, name) VALUES ('INR', 'Indian Rupee');

INSERT INTO tenants (id, name, db_name) VALUES ('test-tenant-1', 'Test Tenant 1', 'finny_tenant_test_1');
INSERT INTO users (id, tenant_id, email, password_hash, first_name, last_name) VALUES ('test-user-1', 'test-tenant-1', 'test1@example.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00DMxs.TVuHOn2', 'Test', 'User 1');
