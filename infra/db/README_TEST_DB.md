# Test Database Setup

This directory contains scripts to set up and manage the test database for the SchoolDay application.

## Files

- `create_test_schema.sh` - Creates the test database and applies the schema
- `setup_test_db.sh` - Main setup script that calls the schema creation script
- `reset_test_db.sh` - Resets the test database (drops and recreates)
- `README_TEST_DB.md` - This documentation file

## Quick Setup

To set up the test database, run:

```bash
# From the project root
./infra/db/setup_test_db.sh
```

Or from the db directory:

```bash
cd infra/db
./setup_test_db.sh
```

## Manual Setup

If you prefer to run the steps manually:

1. **Create the test database:**
   ```bash
   ./infra/db/create_test_schema.sh
   ```

2. **Verify the setup:**
   ```bash
   mysql -u schoolday -p -h localhost -P 3306 schoolday_test -e "SHOW TABLES;"
   ```

## Reset Test Database

To reset the test database (useful for clean test runs):

```bash
# From the project root
./infra/db/reset_test_db.sh
```

Or from the db directory:

```bash
cd infra/db
./reset_test_db.sh
```

## Test Configuration

The test database is configured in `src/test/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/schoolday_test
spring.datasource.username=schoolday
spring.datasource.password=schoolday
```

## Running Tests

After setting up the test database, you can run tests with:

```bash
./mvnw test
```

## Troubleshooting

### Database Connection Issues

1. **Check if MySQL is running:**
   ```bash
   docker ps | grep mysql
   ```

2. **Check if the test database exists:**
   ```bash
   mysql -u schoolday -p -h localhost -P 3306 -e "SHOW DATABASES;" | grep schoolday_test
   ```

3. **Check database permissions:**
   ```bash
   mysql -u schoolday -p -h localhost -P 3306 -e "SHOW GRANTS FOR 'schoolday'@'%';"
   ```

### Permission Issues

If you get permission errors when running the scripts:

```bash
chmod +x infra/db/*.sh
```

### Port Conflicts

If port 3306 is already in use, you may need to:
1. Stop other MySQL instances
2. Or modify the port in the scripts and application.properties

## Best Practices

1. **Always reset the test database** before running a full test suite to ensure clean state
2. **Use the setup scripts** rather than manual database creation
3. **Check the logs** if tests fail to identify database-related issues
4. **Keep the test database separate** from your development database

## Database Schema

The test database uses the same schema as the main database, defined in:
- `infra/db/sql/schema.sql` - Database schema
- `infra/db/sql/basedata.sql` - Base data (roles, etc.)

The test database is named `schoolday_test` to distinguish it from the main `schoolday` database. 