DROP DATABASE atm;
select * from users;

CREATE DATABASE ATM;

USE ATM;
show databases;
-- USERS TABLE
CREATE TABLE users (
    account_no INT PRIMARY KEY,
    name VARCHAR(100),
    pin INT,
    balance DOUBLE,
    status VARCHAR(10) DEFAULT 'active'
);

-- TRANSACTIONS TABLE
CREATE TABLE transactions (
    txn_id INT AUTO_INCREMENT PRIMARY KEY,
    account_no INT,
    type VARCHAR(20),	
    amount DOUBLE,
    txn_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_no) REFERENCES users(account_no)
);

-- Sample Data
INSERT INTO users (account_no, name, pin, balance) VALUES
(123456, 'Sohail', 1234, 10000),
(654321, 'Shafique', 4321, 8000),
(111111, 'Shardha', 1111, 12000);

SHOW COLUMNS FROM users;