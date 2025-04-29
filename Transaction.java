class Transaction {
    private int id;
    private String accountNumber;
    private String type;
    private double amount;
    private String recipientAccount;
    private String timestamp;

    public Transaction(int id, String accountNumber, String type, double amount, String recipientAccount, String timestamp) {
        this.id = id;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.recipientAccount = recipientAccount;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Transaction [id=" + id + ", accountNumber=" + accountNumber + ", type=" + type + ", amount=" + amount
                + ", recipientAccount=" + recipientAccount + ", timestamp=" + timestamp + "]";
    }
}