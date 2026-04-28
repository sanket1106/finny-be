-- ==========================================
-- Apply categories and data for The Smith Family
-- ==========================================
USE `finny_family_smith_id`;

-- Income Categories
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('SALARY_ID', 'family_smith_id', 'Salary', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('BONUS_ID', 'family_smith_id', 'Bonus', NOW());

-- Expense Categories (Top level)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('FOOD_AND_DINING_ID', 'family_smith_id', 'Food & Dining', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('TRANSPORTATION_ID', 'family_smith_id', 'Transportation', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('HOUSING_ID', 'family_smith_id', 'Housing', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('ENTERTAINMENT_ID', 'family_smith_id', 'Entertainment', NOW());

-- Sub-categories (Food & Dining)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('GROCERIES_ID', 'family_smith_id', 'Groceries', 'FOOD_AND_DINING_ID', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('RESTAURANTS_ID', 'family_smith_id', 'Restaurants', 'FOOD_AND_DINING_ID', NOW());

-- Sub-categories (Transportation)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('GAS_ID', 'family_smith_id', 'Gas', 'TRANSPORTATION_ID', NOW());

-- Sub-categories (Housing)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('MORTGAGE_ID', 'family_smith_id', 'Mortgage', 'HOUSING_ID', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('UTILITIES_ID', 'family_smith_id', 'Utilities', 'HOUSING_ID', NOW());

-- Accounts
INSERT INTO `accounts` (`id`, `tenant_id`, `name`, `type`, `currency`, `balance`, `status`, `created`) VALUES ('smith_checking_id', 'family_smith_id', 'Smith Joint Checking', 'BANK', 'USD', 5420.50, 'ACTIVE', NOW());
INSERT INTO `accounts` (`id`, `tenant_id`, `name`, `type`, `currency`, `balance`, `status`, `created`) VALUES ('smith_savings_id', 'family_smith_id', 'Smith Emergency Fund', 'BANK', 'USD', 15000.00, 'ACTIVE', NOW());
INSERT INTO `accounts` (`id`, `tenant_id`, `name`, `type`, `currency`, `balance`, `status`, `created`) VALUES ('smith_credit_id', 'family_smith_id', 'Smith Rewards Card', 'CREDIT', 'USD', -450.75, 'ACTIVE', NOW());
INSERT INTO `accounts` (`id`, `tenant_id`, `name`, `type`, `currency`, `balance`, `status`, `created`) VALUES ('smith_mortgage_id', 'family_smith_id', 'Smith Home Mortgage', 'LOAN', 'USD', -250000.00, 'ACTIVE', NOW());

-- Transactions
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `category_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_smith_1', 'family_smith_id', 'smith_checking_id', 'SALARY_ID', 3500.00, 'INCOME', 'Tech Corp Payroll', DATE_SUB(NOW(), INTERVAL 5 DAY), NOW());
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `category_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_smith_2', 'family_smith_id', 'smith_credit_id', 'GROCERIES_ID', 150.25, 'SPENDING', 'Whole Foods', DATE_SUB(NOW(), INTERVAL 2 DAY), NOW());
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `category_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_smith_3', 'family_smith_id', 'smith_credit_id', 'GAS_ID', 45.00, 'SPENDING', 'Shell Station', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `category_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_smith_4', 'family_smith_id', 'smith_checking_id', 'MORTGAGE_ID', 1800.00, 'SPENDING', 'Monthly Mortgage Payment', DATE_SUB(NOW(), INTERVAL 10 DAY), NOW());
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `to_account_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_smith_transfer', 'family_smith_id', 'smith_checking_id', 'smith_credit_id', 400.00, 'TRANSFER', 'Credit Card Payment', NOW(), NOW());


-- ==========================================
-- Apply categories and data for The Jones Family
-- ==========================================
USE `finny_family_jones_id`;

-- Income Categories
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('SALARY_ID', 'family_jones_id', 'Salary', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('BONUS_ID', 'family_jones_id', 'Bonus', NOW());

-- Expense Categories (Top level)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('FOOD_AND_DINING_ID', 'family_jones_id', 'Food & Dining', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('TRANSPORTATION_ID', 'family_jones_id', 'Transportation', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('HOUSING_ID', 'family_jones_id', 'Housing', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `created`) VALUES ('ENTERTAINMENT_ID', 'family_jones_id', 'Entertainment', NOW());

-- Sub-categories (Food & Dining)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('GROCERIES_ID', 'family_jones_id', 'Groceries', 'FOOD_AND_DINING_ID', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('RESTAURANTS_ID', 'family_jones_id', 'Restaurants', 'FOOD_AND_DINING_ID', NOW());

-- Sub-categories (Transportation)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('PUBLIC_TRANSIT_ID', 'family_jones_id', 'Public Transit', 'TRANSPORTATION_ID', NOW());

-- Sub-categories (Housing)
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('RENT_ID', 'family_jones_id', 'Rent', 'HOUSING_ID', NOW());
INSERT INTO `categories` (`id`, `tenant_id`, `name`, `parent_id`, `created`) VALUES ('UTILITIES_ID', 'family_jones_id', 'Utilities', 'HOUSING_ID', NOW());

-- Accounts
INSERT INTO `accounts` (`id`, `tenant_id`, `name`, `type`, `currency`, `balance`, `status`, `created`) VALUES ('jones_checking_id', 'family_jones_id', 'Jones Personal Checking', 'BANK', 'USD', 2150.00, 'ACTIVE', NOW());
INSERT INTO `accounts` (`id`, `tenant_id`, `name`, `type`, `currency`, `balance`, `status`, `created`) VALUES ('jones_savings_id', 'family_jones_id', 'Jones Vacation Fund', 'BANK', 'USD', 850.00, 'ACTIVE', NOW());

-- Transactions
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `category_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_jones_1', 'family_jones_id', 'jones_checking_id', 'SALARY_ID', 2800.00, 'INCOME', 'Design Studio Salary', DATE_SUB(NOW(), INTERVAL 3 DAY), NOW());
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `category_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_jones_2', 'family_jones_id', 'jones_checking_id', 'RENT_ID', 1200.00, 'SPENDING', 'Apartment Rent', DATE_SUB(NOW(), INTERVAL 14 DAY), NOW());
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `category_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_jones_3', 'family_jones_id', 'jones_checking_id', 'RESTAURANTS_ID', 65.50, 'SPENDING', 'Dinner with friends', DATE_SUB(NOW(), INTERVAL 1 DAY), NOW());
INSERT INTO `transactions` (`id`, `tenant_id`, `account_id`, `to_account_id`, `amount`, `type`, `description`, `transaction_date`, `created`) VALUES ('txn_jones_transfer', 'family_jones_id', 'jones_checking_id', 'jones_savings_id', 200.00, 'TRANSFER', 'Transfer to Vacation Fund', NOW(), NOW());
