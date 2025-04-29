
# 💳 Bank Management System (Java + MySQL)

A console-based **Bank Management System** developed using **Java** and **MySQL**, enabling users to create bank accounts, securely perform transactions, and view account history. Built with a focus on **data integrity**, **security**, and **modular object-oriented design** using JDBC.

## 📦 Features

- 🔐 **Secure Account Management**
  - Create accounts with name, phone, email, and 4-digit PIN
  - Automatically assigns a unique 5-digit account number
  - Phone number and email must be unique
  - (Planned) Enforce a maximum of 2 accounts per name
  - PINs are hashed using **SHA-256** for secure storage

- 💸 **Banking Operations**
  - Deposit funds
  - Withdraw funds (with PIN validation)
  - Transfer funds to another account (with PIN)
  - Realtime balance updates via database transactions

- 📄 **Transaction History**
  - View last 10 transactions with timestamps and types (Deposit, Withdraw, Transfer)

- 🛡️ **PIN Protection**
  - 2 attempts allowed for entering PIN before locking the operation
  - All sensitive operations require PIN verification

- 📁 **Modular Codebase**
  - Clean separation of concerns:
    - `AccountService` handles account-related operations
    - `TransactionService` manages deposits, withdrawals, and transfers
    - `Transaction` and `Account` are model classes

- 🎨 **CLI Interface**
  - Color-coded terminal messages using ANSI escape codes for better user experience

## 🧱 Project Structure

```
📁 bank-management-system/
├── Account.java              # Account model (POJO)
├── Transaction.java          # Transaction model (POJO)
├── AccountService.java       # Account creation, PIN, balance methods
├── TransactionService.java   # Deposit, withdraw, and transfer logic
└── BankManagementSystem.java # Main CLI application (menus, DB setup)
```

## 🗃️ Database Schema

### 🔹 `accounts`
| Column         | Type         | Description                     |
|----------------|--------------|---------------------------------|
| account_number | VARCHAR(5)   | Primary Key, randomly generated |
| name           | VARCHAR(100) | Full name of account holder     |
| phone          | VARCHAR(15)  | Unique                          |
| email          | VARCHAR(100) | Unique                          |
| pin            | VARCHAR(64)  | SHA-256 hashed 4-digit PIN      |
| balance        | DECIMAL      | Default: 0.00                   |

### 🔹 `transactions`
| Column             | Type                  | Description                             |
|--------------------|-----------------------|-----------------------------------------|
| id                 | INT                   | Auto-increment, primary key             |
| account_number     | VARCHAR(5)            | FK to `accounts.account_number`         |
| transaction_type   | ENUM                  | 'DEPOSIT', 'WITHDRAW', 'TRANSFER'       |
| amount             | DECIMAL               | Transaction amount                      |
| recipient_account  | VARCHAR(5) (nullable) | Recipient account for transfers         |
| timestamp          | TIMESTAMP             | Automatically generated on transaction  |


## 🔌 JDBC Dependency

This project uses JDBC to connect Java to MySQL. You must include the MySQL Connector/J in your project.

### ➤ Download JDBC Driver

1. Go to [https://dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)
2. Download the platform-independent `.zip` or `.tar.gz`
3. Extract it and copy the `mysql-connector-java-x.x.xx.jar` file
4. Include this `.jar` in your classpath when compiling and running

#### Example (Compile and Run):
```bash
javac -cp .:mysql-connector-java-8.0.33.jar *.java
java -cp .:mysql-connector-java-8.0.33.jar BankManagementSystem
```

(Use `;` instead of `:` on Windows)

---

## 🛠️ Database Setup

You can create and populate the database using the provided SQL script.

### 1. Open your MySQL CLI or GUI (like MySQL Workbench)

### 2. Run the following SQL:

```sql
CREATE DATABASE IF NOT EXISTS bank_management;
USE bank_management;

CREATE TABLE IF NOT EXISTS accounts (
    account_number VARCHAR(5) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(15) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    pin VARCHAR(64) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(5) NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    recipient_account VARCHAR(5),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES accounts(account_number)
);

INSERT INTO accounts (account_number, name, phone, email, pin, balance) VALUES
('12345', 'John Doe', '1234567890', 'john@example.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 1000.00),
('67890', 'Jane Smith', '0987654321', 'jane@example.com', '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 500.00);

INSERT INTO transactions (account_number, transaction_type, amount, recipient_account) VALUES
('12345', 'DEPOSIT', 1000.00, NULL),
('67890', 'DEPOSIT', 500.00, NULL),
('12345', 'WITHDRAW', 200.00, NULL),
('12345', 'TRANSFER', 300.00, '67890');
```

> 💡 Note: `03ac...f4` is the SHA-256 hash of the PIN `1234`.
