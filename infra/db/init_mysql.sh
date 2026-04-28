#!/bin/bash

echo "Downloading and initializing MySQL server container..."

# Remove existing container if it exists
docker rm -f mysql8 2>/dev/null || true

# Run latest mysql with specified users
docker run -d \
  --name mysql8 \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_USER=finny \
  -e MYSQL_PASSWORD=finny \
  -e MYSQL_DATABASE=finny \
  mysql:latest

echo "Waiting for MySQL to finish initialization..."
# Wait for mysql to be ready
until docker exec mysql8 mysqladmin ping -h"localhost" --silent; do
    sleep 2
    echo "Still waiting for MySQL..."
done

echo "MySQL container 'mysql8' is up and running!"
