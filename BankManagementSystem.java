// BankManagementSystem.java - Main application class
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Pattern;

public class BankManagementSystem {
    private static Scanner scanner = new Scanner(System.in);
    private static Connection connection;

    // Regular expressions for input validation
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)\\.[A-Za-z]{2,}$");
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_CYAN = "\u001B[36m";

    public static void main(String[] args) {
        try {
            printWelcomeBanner();

            // Initialize database connection
            connection = initializeDatabaseConnection();


            // Run the application
            runApplication();

        } catch (SQLException e) {
            printError("Database connection failed.");
            e.printStackTrace();
        } finally {
            scanner.close();
            try {
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private static void printWelcomeBanner() {
        System.out.println(ANSI_CYAN + "╔══════════════════════════════════════════════════╗");
        System.out.println("║                                                  ║");
        System.out.println("║             BANK MANAGEMENT SYSTEM               ║");
        System.out.println("║                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println(ANSI_BLUE + "            Welcome to Our Banking Services" + ANSI_RESET);
        System.out.println();
    }

    private static Connection initializeDatabaseConnection() throws SQLException {
        System.out.println("Connecting to database...");
        String url = "jdbc:mysql://localhost:3306/bank_management";
        String user = "root";  // Change to your MySQL username
        String password = "";  // Change to your MySQL password

        try {
            // Load MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            printError("MySQL JDBC Driver not found!");
            throw new SQLException("JDBC Driver not found", e);
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static void setupDatabase(Connection connection) throws SQLException {
        // Create tables if they don't exist
        String createAccountsTable = "CREATE TABLE IF NOT EXISTS accounts (" +
                "account_number VARCHAR(5) PRIMARY KEY," +
                "name VARCHAR(100) NOT NULL," +
                "phone VARCHAR(15) UNIQUE NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "pin VARCHAR(64) NOT NULL," +  // Storing hashed pin
                "balance DECIMAL(15,2) DEFAULT 0.00" +
                ")";

        String createTransactionsTable = "CREATE TABLE IF NOT EXISTS transactions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "account_number VARCHAR(5) NOT NULL," +
                "transaction_type ENUM('DEPOSIT', 'WITHDRAW', 'TRANSFER') NOT NULL," +
                "amount DECIMAL(15,2) NOT NULL," +
                "recipient_account VARCHAR(5)," +
                "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "FOREIGN KEY (account_number) REFERENCES accounts(account_number)" +
                ")";

        try (var statement = connection.createStatement()) {
            statement.execute(createAccountsTable);
            statement.execute(createTransactionsTable);
        }
    }

    private static void runApplication() {
        boolean exit = false;

        while (!exit) {
            displayMainMenu();
            int choice = getInput(0, 6);

            switch (choice) {
                case 0:
                    System.out.println(ANSI_GREEN + "Thank you for using our Bank Management System. Goodbye!" + ANSI_RESET);
                    exit = true;
                    break;
                case 1:
                    createAccount();
                    break;
                case 2:
                    deposit();
                    break;
                case 3:
                    withdraw();
                    break;
                case 4:
                    transfer();
                    break;
                case 5:
                    checkBalance();
                    break;
                case 6:
                    viewTransactionHistory();
                    break;
            }
        }
    }

    private static void displayMainMenu() {
        System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
        System.out.println("║             BANK MANAGEMENT SYSTEM               ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);
        System.out.println("1. Create New Account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Check Balance");
        System.out.println("6. View Transaction History");
        System.out.println("0. Exit");
        System.out.print("Enter your choice (0-6): ");
    }

    private static int getInput(int min, int max) {
        int choice = -1;
        boolean validInput = false;

        while (!validInput) {
            try {
                choice = scanner.nextInt();
                scanner.nextLine(); // Clear the buffer

                if (choice >= min && choice <= max) {
                    validInput = true;
                } else {
                    printError("Invalid option. Please enter a number between " + min + " and " + max + ": ");
                }
            } catch (InputMismatchException e) {
                scanner.nextLine(); // Clear the buffer
                printError("Invalid input. Please enter a number: ");
            }
        }

        return choice;
    }

    private static void createAccount() {
        System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
        System.out.println("║               CREATE NEW ACCOUNT                ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.print("Enter name: ");
        String name = scanner.nextLine();

        // Phone validation
        String phone = "";
        boolean validPhone = false;
        while (!validPhone) {
            System.out.print("Enter phone number (9-10 digits): ");
            phone = scanner.nextLine();
            if (PHONE_PATTERN.matcher(phone).matches()) {
                validPhone = true;
            } else {
                printError("Invalid phone number. Please enter 9-10 digits only.");
            }
        }

        // Email validation
        String email = "";
        boolean validEmail = false;
        while (!validEmail) {
            System.out.print("Enter email address: ");
            email = scanner.nextLine();
            if (EMAIL_PATTERN.matcher(email).matches()) {
                validEmail = true;
            } else {
                printError("Invalid email format. Email must contain '@' and a domain (e.g., example@domain.com)");
            }
        }

        // PIN validation
        String pin = "";
        boolean validPin = false;
        while (!validPin) {
            System.out.print("Create 4-digit PIN: ");
            pin = scanner.nextLine();
            if (pin.matches("\\d{4}")) {
                validPin = true;
            } else {
                printError("PIN must be exactly 4 digits.");
            }
        }

        try {
            // Check if phone or email already exists
            String checkQuery = "SELECT COUNT(*) FROM accounts WHERE phone = ? OR email = ?";
            try (PreparedStatement checkStmt = connection.prepareStatement(checkQuery)) {
                checkStmt.setString(1, phone);
                checkStmt.setString(2, email);

                ResultSet resultSet = checkStmt.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    printError("An account with this phone number or email already exists.");
                    handleProcessEnd();
                    return;
                }
            }

            // Generate a unique 5-digit account number
            String accountNumber = generateUniqueAccountNumber();

            // Hash the PIN for security
            String hashedPin = hashPin(pin);

            // Insert new account
            String insertQuery = "INSERT INTO accounts (account_number, name, phone, email, pin, balance) VALUES (?, ?, ?, ?, ?, 0.00)";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
                insertStmt.setString(1, accountNumber);
                insertStmt.setString(2, name);
                insertStmt.setString(3, phone);
                insertStmt.setString(4, email);
                insertStmt.setString(5, hashedPin);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println(ANSI_GREEN + "Account created successfully!" + ANSI_RESET);
                    System.out.println(ANSI_GREEN + "Your account number is: " + accountNumber + ANSI_RESET);
                    System.out.println("Please keep your account number safe - you'll need it for all transactions.");
                } else {
                    printError("Failed to create account. Please try again.");
                }
            }
        } catch (SQLException e) {
            printError(e.getMessage());
        }

        handleProcessEnd();
    }

    private static void deposit() {
        System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
        System.out.println("║                    DEPOSIT                      ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        try {
            // Check if account exists
            if (!accountExists(accountNumber)) {
                printError("Account not found.");
                handleProcessEnd();
                return;
            }

            System.out.print("Enter amount to deposit: ");
            double amount = getDoubleInput();

            if (amount <= 0) {
                printError("Amount must be greater than zero.");
                handleProcessEnd();
                return;
            }

            // Begin transaction
            connection.setAutoCommit(false);

            try {
                // Get current balance
                double currentBalance = getBalance(accountNumber);

                // Update balance
                double newBalance = currentBalance + amount;
                boolean balanceUpdated = updateBalance(accountNumber, newBalance);

                if (!balanceUpdated) {
                    connection.rollback();
                    printError("Deposit failed. Please try again.");
                    handleProcessEnd();
                    return;
                }

                // Record transaction
                boolean transactionRecorded = recordTransaction(accountNumber, "DEPOSIT", amount, null);

                if (!transactionRecorded) {
                    connection.rollback();
                    printError("Deposit failed. Please try again.");
                    handleProcessEnd();
                    return;
                }

                // Commit transaction
                connection.commit();
                System.out.println(ANSI_GREEN + "Deposit successful!" + ANSI_RESET);
                System.out.println(ANSI_GREEN + "New balance: $" + String.format("%.2f", newBalance) + ANSI_RESET);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            printError(e.getMessage());
        }

        handleProcessEnd();
    }

    private static void withdraw() {
        System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
        System.out.println("║                   WITHDRAW                      ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        try {
            // Check if account exists
            if (!accountExists(accountNumber)) {
                printError("Account not found.");
                handleProcessEnd();
                return;
            }

            System.out.print("Enter amount to withdraw: ");
            double amount = getDoubleInput();

            if (amount <= 0) {
                printError("Amount must be greater than zero.");
                handleProcessEnd();
                return;
            }

            // Verify PIN
            boolean pinValid = verifyPin(accountNumber);
            if (!pinValid) {
                handleProcessEnd();
                return;
            }

            // Check if there's enough balance
            double currentBalance = getBalance(accountNumber);
            if (currentBalance < amount) {
                printError("Insufficient balance. Your current balance is $" + String.format("%.2f", currentBalance));
                handleProcessEnd();
                return;
            }

            // Begin transaction
            connection.setAutoCommit(false);

            try {
                // Update balance
                double newBalance = currentBalance - amount;
                boolean balanceUpdated = updateBalance(accountNumber, newBalance);

                if (!balanceUpdated) {
                    connection.rollback();
                    printError("Withdrawal failed. Please try again.");
                    handleProcessEnd();
                    return;
                }

                // Record transaction
                boolean transactionRecorded = recordTransaction(accountNumber, "WITHDRAW", amount, null);

                if (!transactionRecorded) {
                    connection.rollback();
                    printError("Withdrawal failed. Please try again.");
                    handleProcessEnd();
                    return;
                }

                // Commit transaction
                connection.commit();
                System.out.println(ANSI_GREEN + "Withdrawal successful!" + ANSI_RESET);
                System.out.println(ANSI_GREEN + "New balance: $" + String.format("%.2f", newBalance) + ANSI_RESET);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            printError(e.getMessage());
        }

        handleProcessEnd();
    }

    private static void transfer() {
        System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
        System.out.println("║                   TRANSFER                      ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.print("Enter your account number: ");
        String fromAccount = scanner.nextLine();

        try {
            // Check if account exists
            if (!accountExists(fromAccount)) {
                printError("Account not found.");
                handleProcessEnd();
                return;
            }

            System.out.print("Enter recipient account number: ");
            String toAccount = scanner.nextLine();

            // Check if recipient account exists
            if (!accountExists(toAccount)) {
                printError("Recipient account not found.");
                handleProcessEnd();
                return;
            }

            // Prevent self-transfers
            if (fromAccount.equals(toAccount)) {
                printError("Cannot transfer to your own account.");
                handleProcessEnd();
                return;
            }

            System.out.print("Enter amount to transfer: ");
            double amount = getDoubleInput();

            if (amount <= 0) {
                printError("Amount must be greater than zero.");
                handleProcessEnd();
                return;
            }

            // Verify PIN
            boolean pinValid = verifyPin(fromAccount);
            if (!pinValid) {
                handleProcessEnd();
                return;
            }

            // Check if there's enough balance
            double currentBalance = getBalance(fromAccount);
            if (currentBalance < amount) {
                printError("Insufficient balance. Your current balance is $" + String.format("%.2f", currentBalance));
                handleProcessEnd();
                return;
            }

            // Begin transaction
            connection.setAutoCommit(false);

            try {
                // Update sender's balance
                double newSenderBalance = currentBalance - amount;
                boolean senderBalanceUpdated = updateBalance(fromAccount, newSenderBalance);

                if (!senderBalanceUpdated) {
                    connection.rollback();
                    printError("Transfer failed. Please try again.");
                    handleProcessEnd();
                    return;
                }

                // Update recipient's balance
                double recipientBalance = getBalance(toAccount);
                double newRecipientBalance = recipientBalance + amount;
                boolean recipientBalanceUpdated = updateBalance(toAccount, newRecipientBalance);

                if (!recipientBalanceUpdated) {
                    connection.rollback();
                    printError("Transfer failed. Please try again.");
                    handleProcessEnd();
                    return;
                }

                // Record transaction
                boolean transactionRecorded = recordTransaction(fromAccount, "TRANSFER", amount, toAccount);

                if (!transactionRecorded) {
                    connection.rollback();
                    printError("Transfer failed. Please try again.");
                    handleProcessEnd();
                    return;
                }

                // Commit transaction
                connection.commit();
                System.out.println(ANSI_GREEN + "Transfer successful!" + ANSI_RESET);
                System.out.println(ANSI_GREEN + "New balance: $" + String.format("%.2f", newSenderBalance) + ANSI_RESET);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            printError(e.getMessage());
        }

        handleProcessEnd();
    }

    private static void checkBalance() {
        System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
        System.out.println("║                 CHECK BALANCE                   ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        try {
            // Check if account exists
            if (!accountExists(accountNumber)) {
                printError("Account not found.");
                handleProcessEnd();
                return;
            }

            // Verify PIN
            boolean pinValid = verifyPin(accountNumber);
            if (!pinValid) {
                handleProcessEnd();
                return;
            }

            // Get account information
            String query = "SELECT name, balance FROM accounts WHERE account_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, accountNumber);

                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next()) {
                    String name = resultSet.getString("name");
                    double balance = resultSet.getDouble("balance");

                    System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
                    System.out.println("║                ACCOUNT DETAILS                   ║");
                    System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);
                    System.out.println("Account Holder: " + name);
                    System.out.println("Account Number: " + accountNumber);
                    System.out.println(ANSI_GREEN + "Current Balance: $" + String.format("%.2f", balance) + ANSI_RESET);
                }
            }

        } catch (SQLException e) {
            printError(e.getMessage());
        }

        handleProcessEnd();
    }

    private static void viewTransactionHistory() {
        System.out.println(ANSI_CYAN + "\n╔══════════════════════════════════════════════════╗");
        System.out.println("║             TRANSACTION HISTORY                ║");
        System.out.println("╚══════════════════════════════════════════════════╝" + ANSI_RESET);

        System.out.print("Enter account number: ");
        String accountNumber = scanner.nextLine();

        try {
            // Check if account exists
            if (!accountExists(accountNumber)) {
                printError("Account not found.");
                handleProcessEnd();
                return;
            }

            // Verify PIN
            boolean pinValid = verifyPin(accountNumber);
            if (!pinValid) {
                handleProcessEnd();
                return;
            }

            // Retrieve transactions
            String query = "SELECT transaction_type, amount, recipient_account, timestamp FROM transactions " +
                    "WHERE account_number = ? ORDER BY timestamp DESC LIMIT 10";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, accountNumber);

                ResultSet resultSet = stmt.executeQuery();
                boolean hasTransactions = false;

                System.out.println("\nRecent Transactions:");
                System.out.println("----------------------------------------------------------");
                System.out.printf("%-10s %-12s %-15s %-20s\n", "Type", "Amount", "Recipient", "Date/Time");
                System.out.println("----------------------------------------------------------");

                while (resultSet.next()) {
                    hasTransactions = true;
                    String type = resultSet.getString("transaction_type");
                    double amount = resultSet.getDouble("amount");
                    String recipient = resultSet.getString("recipient_account");
                    Timestamp timestamp = resultSet.getTimestamp("timestamp");

                    System.out.printf("%-10s $%-11.2f %-15s %-20s\n",
                            type, amount, (recipient != null ? recipient : "N/A"), timestamp);
                }

                System.out.println("----------------------------------------------------------");

                if (!hasTransactions) {
                    System.out.println("No transaction history found for this account.");
                }
            }

        } catch (SQLException e) {
            printError(e.getMessage());
        }

        handleProcessEnd();
    }

    private static boolean verifyPin(String accountNumber) throws SQLException {
        int attempts = 0;
        boolean verified = false;

        while (attempts < 2 && !verified) {
            System.out.print("Enter your 4-digit PIN: ");
            String pin = scanner.nextLine();

            String hashedPin = hashPin(pin);
            String query = "SELECT COUNT(*) FROM accounts WHERE account_number = ? AND pin = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, accountNumber);
                stmt.setString(2, hashedPin);

                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) > 0) {
                    verified = true;
                } else {
                    attempts++;
                    if (attempts < 2) {
                        printError("Incorrect PIN. Please try again.");
                    } else {
                        printError("Multiple incorrect PIN attempts. Access denied.");
                    }
                }
            }
        }

        return verified;
    }

    private static String generateUniqueAccountNumber() throws SQLException {
        Random random = new Random();
        boolean isUnique = false;
        String accountNumber = "";

        while (!isUnique) {
            // Generate a random 5-digit number
            int randomNum = 10000 + random.nextInt(90000);
            accountNumber = String.valueOf(randomNum);

            // Check if it's unique
            String query = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, accountNumber);

                ResultSet resultSet = stmt.executeQuery();
                if (resultSet.next() && resultSet.getInt(1) == 0) {
                    isUnique = true;
                }
            }
        }

        return accountNumber;
    }

    private static String hashPin(String pin) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(pin.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashedBytes) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Fall back to simple method if SHA-256 is not available
            return "HASH_" + pin;
        }
    }

    private static boolean accountExists(String accountNumber) throws SQLException {
        String query = "SELECT COUNT(*) FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);

            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        }

        return false;
    }

    private static double getBalance(String accountNumber) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);

            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getDouble("balance");
            }
        }

        throw new SQLException("Account not found.");
    }

    private static boolean updateBalance(String accountNumber, double newBalance) throws SQLException {
        String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private static boolean recordTransaction(String accountNumber, String type, double amount, String recipientAccount) throws SQLException {
        String query = "INSERT INTO transactions (account_number, transaction_type, amount, recipient_account) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);
            stmt.setString(2, type);
            stmt.setDouble(3, amount);
            stmt.setString(4, recipientAccount);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    private static double getDoubleInput() {
        double value = 0;
        boolean validInput = false;

        while (!validInput) {
            try {
                value = scanner.nextDouble();
                scanner.nextLine(); // Clear the buffer
                validInput = true;
            } catch (InputMismatchException e) {
                scanner.nextLine(); // Clear the buffer
                printError("Invalid input. Please enter a valid amount: ");
            }
        }

        return value;
    }

    private static void handleProcessEnd() {
        System.out.println("\nWhat would you like to do next?");
        System.out.println("1. Return to main menu");
        System.out.println("0. Exit system");
        System.out.print("Enter your choice (0-1): ");

        int choice = getInput(0, 1);
        if (choice == 0) {
            System.out.println(ANSI_GREEN + "Thank you for using our Bank Management System. Goodbye!" + ANSI_RESET);
            System.exit(0);
        }
    }

    private static void printError(String message) {
        System.out.println(ANSI_RED + "ERROR: " + message + ANSI_RESET);
    }
}