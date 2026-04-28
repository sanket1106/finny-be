#!/bin/bash

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Ensure the database container is running
echo "Checking if mysql8 container is running..."
if ! docker ps --format '{{.Names}}' | grep -q '^mysql8$'; then
    echo "Starting mysql8 container..."
    docker start mysql8
    echo "Waiting for MySQL to initialize..."
    sleep 5
fi

# Load environment variables
ENV_FILE="$SCRIPT_DIR/../.env"
if [ -f "$ENV_FILE" ]; then
  source "$ENV_FILE"
fi

DB_USER="root"
DB_PASS="${MYSQL_ROOT_PASSWORD:-root}"
MASTER_DB_NAME="finny_master"

echo "=== INITIALIZING MASTER SCHEMA ==="
docker exec -i mysql8 mysql -u"$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS $MASTER_DB_NAME;"

MASTER_SQL_SCRIPT="${SCRIPT_DIR}/sql/schema_master.sql"
if [ -f "$MASTER_SQL_SCRIPT" ]; then
    cat "$MASTER_SQL_SCRIPT" | docker exec -i mysql8 sh -c "mysql -u$DB_USER -p$DB_PASS $MASTER_DB_NAME"
fi

MASTER_SEED_SCRIPT="${SCRIPT_DIR}/sql/seed_master.sql"
if [ -f "$MASTER_SEED_SCRIPT" ]; then
    echo "Applying Master Seed Data..."
    cat "$MASTER_SEED_SCRIPT" | docker exec -i mysql8 sh -c "mysql -u$DB_USER -p$DB_PASS $MASTER_DB_NAME"
fi

echo "=== INITIALIZING TENANT SCHEMAS ==="
# Pull tenant databases
TENANT_DBS=$(docker exec -i mysql8 mysql -u"$DB_USER" -p"$DB_PASS" -N -s $MASTER_DB_NAME -e "SELECT db_name FROM tenants;")

for DB_NAME in $TENANT_DBS; do
    # Strip carriage returns just in case
    DB_NAME=$(echo "$DB_NAME" | tr -d '\r')
    if [ -z "$DB_NAME" ]; then continue; fi
    
    echo "Processing Tenant Schema DB: $DB_NAME"
    docker exec -i mysql8 mysql -u"$DB_USER" -p"$DB_PASS" -e "CREATE DATABASE IF NOT EXISTS $DB_NAME;"
    
    TENANT_SQL_SCRIPT="${SCRIPT_DIR}/sql/finny_tenant_schema.sql"
    if [ -f "$TENANT_SQL_SCRIPT" ]; then
        cat "$TENANT_SQL_SCRIPT" | docker exec -i mysql8 sh -c "mysql -u$DB_USER -p$DB_PASS $DB_NAME"
    fi
done

echo "=== INITIALIZING TENANT SEED DATA ==="
TENANT_SEED_SCRIPT="${SCRIPT_DIR}/sql/seed_tenant_data.sql"
if [ -f "$TENANT_SEED_SCRIPT" ]; then
    # Note: seed_tenant_data.sql inherently contains USE `db_name` lines, routing the inserts correctly.
    cat "$TENANT_SEED_SCRIPT" | docker exec -i mysql8 sh -c "mysql -u$DB_USER -p$DB_PASS"
fi

echo "All schema and seed mapping completed successfully!"