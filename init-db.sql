-- Initialize all databases for Book Store Microservices

-- Create databases
CREATE DATABASE IF NOT EXISTS identity_db;
CREATE DATABASE IF NOT EXISTS product_db;
CREATE DATABASE IF NOT EXISTS order_db;
CREATE DATABASE IF NOT EXISTS inventory_db;

-- Grant privileges (root already has all privileges, but explicit for clarity)
GRANT ALL PRIVILEGES ON identity_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON product_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON order_db.* TO 'root'@'%';
GRANT ALL PRIVILEGES ON inventory_db.* TO 'root'@'%';

FLUSH PRIVILEGES;

-- Show created databases
SHOW DATABASES;
