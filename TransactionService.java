import java.sql.*;

class TransactionService {
    private Connection connection;
    private AccountService accountService;

    public TransactionService(Connection connection) {
        this.connection = connection;
        this.accountService = new AccountService(connection);
    }

    public boolean deposit(String accountNumber, double amount) throws SQLException {
        // Validate account
        if (!accountService.accountExists(accountNumber)) {
            return false;
        }

        // Begin transaction
        connection.setAutoCommit(false);

        try {
            // Get current balance
            double currentBalance = accountService.getBalance(accountNumber);

            // Update balance
            double newBalance = currentBalance + amount;
            boolean balanceUpdated = accountService.updateBalance(accountNumber, newBalance);

            if (!balanceUpdated) {
                connection.rollback();
                return false;
            }

            // Record transaction
            boolean transactionRecorded = recordTransaction(accountNumber, "DEPOSIT", amount, null);

            if (!transactionRecorded) {
                connection.rollback();
                return false;
            }

            // Commit transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean withdraw(String accountNumber, double amount) throws SQLException {
        // Validate account
        if (!accountService.accountExists(accountNumber)) {
            return false;
        }

        // Check if there's enough balance
        double currentBalance = accountService.getBalance(accountNumber);
        if (currentBalance < amount) {
            System.out.println("Error: Insufficient balance. Your current balance is $" + currentBalance);
            return false;
        }

        // Begin transaction
        connection.setAutoCommit(false);

        try {
            // Update balance
            double newBalance = currentBalance - amount;
            boolean balanceUpdated = accountService.updateBalance(accountNumber, newBalance);

            if (!balanceUpdated) {
                connection.rollback();
                return false;
            }

            // Record transaction
            boolean transactionRecorded = recordTransaction(accountNumber, "WITHDRAW", amount, null);

            if (!transactionRecorded) {
                connection.rollback();
                return false;
            }

            // Commit transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public boolean transfer(String fromAccount, String toAccount, double amount) throws SQLException {
        // Validate accounts
        if (!accountService.accountExists(fromAccount) || !accountService.accountExists(toAccount)) {
            return false;
        }

        // Check if there's enough balance
        double currentBalance = accountService.getBalance(fromAccount);
        if (currentBalance < amount) {
            System.out.println("Error: Insufficient balance. Your current balance is $" + currentBalance);
            return false;
        }

        // Begin transaction
        connection.setAutoCommit(false);

        try {
            // Update sender's balance
            double newSenderBalance = currentBalance - amount;
            boolean senderBalanceUpdated = accountService.updateBalance(fromAccount, newSenderBalance);

            if (!senderBalanceUpdated) {
                connection.rollback();
                return false;
            }

            // Update recipient's balance
            double recipientBalance = accountService.getBalance(toAccount);
            double newRecipientBalance = recipientBalance + amount;
            boolean recipientBalanceUpdated = accountService.updateBalance(toAccount, newRecipientBalance);

            if (!recipientBalanceUpdated) {
                connection.rollback();
                return false;
            }

            // Record transaction
            boolean transactionRecorded = recordTransaction(fromAccount, "TRANSFER", amount, toAccount);

            if (!transactionRecorded) {
                connection.rollback();
                return false;
            }

            // Commit transaction
            connection.commit();
            return true;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private boolean recordTransaction(String accountNumber, String type, double amount, String recipientAccount) throws SQLException {
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
}