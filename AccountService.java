import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

class AccountService {
    private Connection connection;

    public AccountService(Connection connection) {
        this.connection = connection;
    }

    public int getAccountCountByUserId(int userId) throws SQLException {
        String query = "SELECT COUNT(*) FROM accounts WHERE user_id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, userId);

            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        }

        return 0;
    }

    public String createAccount(int userId, String pin) throws SQLException {
        // Generate a unique 5-digit account number
        String accountNumber = generateUniqueAccountNumber();

        // Hash the PIN for security
        String hashedPin = hashPin(pin);

        // Insert new account
        String insertQuery = "INSERT INTO accounts (account_number, user_id, balance, pin) VALUES (?, ?, 0.00, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertQuery)) {
            insertStmt.setString(1, accountNumber);
            insertStmt.setInt(2, userId);
            insertStmt.setString(3, hashedPin);

            int rowsAffected = insertStmt.executeUpdate();
            if (rowsAffected > 0) {
                return accountNumber;
            }
        }

        return null;
    }

    public boolean verifyPin(String accountNumber, String pin) throws SQLException {
        String query = "SELECT pin FROM accounts WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, accountNumber);

            ResultSet resultSet = stmt.executeQuery();
            if (resultSet.next()) {
                String storedHashedPin = resultSet.getString("pin");
                String inputHashedPin = hashPin(pin);

                return storedHashedPin.equals(inputHashedPin);
            }
        }

        return false;
    }

    public double getBalance(String accountNumber) throws SQLException {
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

    public boolean updateBalance(String accountNumber, double newBalance) throws SQLException {
        String query = "UPDATE accounts SET balance = ? WHERE account_number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDouble(1, newBalance);
            stmt.setString(2, accountNumber);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    public boolean accountExists(String accountNumber) throws SQLException {
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

    private String generateUniqueAccountNumber() throws SQLException {
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

    private String hashPin(String pin) {
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
}