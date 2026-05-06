-- Seed master test data
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
