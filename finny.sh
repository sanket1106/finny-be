#!/bin/bash

# finny.sh
# Super script to manage Finny project resources.
# Usage: ./finny.sh <action> <env> <resource>

if [ "$#" -lt 2 ]; then
    echo "Usage: $0 <action> <env> [resource]"
    echo "Example: $0 start local mysql"
    echo "Example: $0 stop local mysql"
    echo "Example: $0 reset local db"
    echo "Example: $0 nuke local all"
    exit 1
fi

ACTION=$1
ENV=$2
RESOURCE=${3:-all}

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# We currently only support local environment
if [ "$ENV" != "local" ]; then
    echo "Error: Only 'local' environment is currently supported."
    exit 1
fi

case "$ACTION-$RESOURCE" in
    "start-mysql")
        echo "Starting local MySQL..."
        if docker ps -a --format '{{.Names}}' | grep -q '^mysql8$'; then
            if ! docker ps --format '{{.Names}}' | grep -q '^mysql8$'; then
                docker start mysql8
                echo "MySQL container started."
            else
                echo "MySQL is already running."
            fi
        else
            echo "MySQL container not found. Initializing..."
            "$SCRIPT_DIR/infra/db/init_mysql.sh"
        fi
        ;;
        
    "stop-mysql")
        echo "Stopping local MySQL..."
        if docker ps --format '{{.Names}}' | grep -q '^mysql8$'; then
            docker stop mysql8
            echo "MySQL container stopped."
        else
            echo "MySQL is not running."
        fi
        ;;
        
    "reset-db")
        echo "Resetting both main and test databases..."
        
        # Ensure mysql is running
        if ! docker ps --format '{{.Names}}' | grep -q '^mysql8$'; then
            echo "MySQL is not running. Starting it..."
            if docker ps -a --format '{{.Names}}' | grep -q '^mysql8$'; then
                docker start mysql8
                sleep 5
            else
                "$SCRIPT_DIR/infra/db/init_mysql.sh"
            fi
        fi

        # Load env vars to get passwords
        ENV_FILE="$SCRIPT_DIR/infra/.env"
        if [ -f "$ENV_FILE" ]; then
            source "$ENV_FILE"
        fi
        DB_PASS="${MYSQL_ROOT_PASSWORD:-root}"
        
        # Reset All Databases
        echo "--- Purging Databases ---"
        MASTER_DB="finny_master"
        
        # We need to query the master DB first to figure out which tenant DBs to drop!
        if docker exec -i mysql8 mysql -uroot -p"$DB_PASS" -e "USE $MASTER_DB;" 2>/dev/null; then
            TENANT_DBS=$(docker exec -i mysql8 mysql -uroot -p"$DB_PASS" -N -s $MASTER_DB -e "SELECT db_name FROM tenants;" 2>/dev/null || echo "")
            for DB_NAME in $TENANT_DBS; do
                DB_NAME=$(echo "$DB_NAME" | tr -d '\r')
                if [ -n "$DB_NAME" ]; then
                    echo "Dropping tenant database: $DB_NAME"
                    docker exec -i mysql8 mysql -uroot -p"$DB_PASS" -e "DROP DATABASE IF EXISTS $DB_NAME;"
                fi
            done
        fi
        
        echo "Dropping master database: $MASTER_DB"
        docker exec -i mysql8 mysql -uroot -p"$DB_PASS" -e "DROP DATABASE IF EXISTS $MASTER_DB;"
        
        echo "Applying bootstrap (schemas + seeds)..."
        "$SCRIPT_DIR/infra/db/seed_data.sh"

        echo "Database reset complete."
        ;;
        
    "nuke-all")
        echo "Nuking local environment!"
        echo "Stopping and deleting Podman VM..."
        podman machine stop finny-be 2>/dev/null || true
        podman machine rm -f finny-be 2>/dev/null || true
        
        echo "Creating new Podman VM..."
        "$SCRIPT_DIR/infra/create_vm.sh"
        
        echo "Starting new Podman VM..."
        "$SCRIPT_DIR/infra/db/start_vm.sh"
        
        echo "Initializing new MySQL server..."
        "$SCRIPT_DIR/infra/db/init_mysql.sh"
        
        echo "Bootstrapping schemas and data mapping..."
        "$SCRIPT_DIR/infra/db/seed_data.sh"
        
        echo "Nuke complete."
        ;;
        
    *)
        echo "Unsupported action and resource combination: $ACTION $RESOURCE"
        echo "Valid commands:"
        echo "- ./finny.sh start local mysql"
        echo "- ./finny.sh stop local mysql"
        echo "- ./finny.sh reset local db"
        echo "- ./finny.sh nuke local all"
        exit 1
        ;;
esac

echo "Done."
