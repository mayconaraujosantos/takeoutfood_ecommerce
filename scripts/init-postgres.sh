#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- Create databases for each microservice
    CREATE DATABASE auth_db;
    CREATE DATABASE user_db;
    CREATE DATABASE restaurant_db;
    CREATE DATABASE menu_db;
    CREATE DATABASE order_db;
    CREATE DATABASE payment_db;
    CREATE DATABASE delivery_db;

    -- Grant privileges
    GRANT ALL PRIVILEGES ON DATABASE auth_db TO ifood_user;
    GRANT ALL PRIVILEGES ON DATABASE user_db TO ifood_user;
    GRANT ALL PRIVILEGES ON DATABASE restaurant_db TO ifood_user;
    GRANT ALL PRIVILEGES ON DATABASE menu_db TO ifood_user;
    GRANT ALL PRIVILEGES ON DATABASE order_db TO ifood_user;
    GRANT ALL PRIVILEGES ON DATABASE payment_db TO ifood_user;
    GRANT ALL PRIVILEGES ON DATABASE delivery_db TO ifood_user;
EOSQL

echo "PostgreSQL databases created successfully!"