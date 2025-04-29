-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS bank_management;

-- Use the bank_management database
USE bank_management;

-- Create accounts table - combined user and account information
CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(5) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    pin VARCHAR(64) NOT NULL,  -- Storing hashed pin (SHA-256)
    balance DECIMAL(15,2) DEFAULT 0.00
);

-- Create transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(5) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    recipient_account VARCHAR(5),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

-- Optional: Insert some sample data for testing
-- Sample accounts (PIN '1234' hashed with SHA-256)
INSERT INTO accounts (account_number, name, phone, email, pin, balance) VALUES
('12345', 'John Doe', '1234567890', 'john@example.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 1000.00),
('67890', 'Jane Smith', '0987654321', 'jane@example.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 500.00);

-- Sample transactions
INSERT INTO transactions (account_number, transaction_type, amount, recipient_account) VALUES
('12345', 'DEPOSIT', 1000.00, NULL),
('67890', 'DEPOSIT', 500.00, NULL),
('12345', 'WITHDRAW', 200.00, NULL),
('12345', 'TRANSFER', 300.00, '67890');