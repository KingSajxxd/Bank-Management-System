class Account {
    private String accountNumber;
    private int userId;
    private double balance;
    private String hashedPin;

    public Account(String accountNumber, int userId, double balance, String hashedPin) {
        this.accountNumber = accountNumber;
        this.userId = userId;
        this.balance = balance;
        this.hashedPin = hashedPin;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public int getUserId() {
        return userId;
    }

    public double getBalance() {
        return balance;
    }

    public String getHashedPin() {
        return hashedPin;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Account [accountNumber=" + accountNumber + ", userId=" + userId + ", balance=" + balance + "]";
    }
}