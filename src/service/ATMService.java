package service;

import db.DatabaseConnection;
import model.User;
import java.sql.*;

public class ATMService {
    private Connection conn;

    public ATMService() throws SQLException {
        this.conn = DatabaseConnection.getConnection();
    }

    public User authenticate(int accountNo, int pin) throws SQLException {
        String sql = "SELECT account_no, name, pin, balance, status FROM users WHERE account_no = ? AND pin = ? AND status = 'active'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNo);
            pstmt.setInt(2, pin);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getInt("account_no"),
                            rs.getString("name"),
                            rs.getInt("pin"),
                            rs.getDouble("balance"),
                            rs.getString("status")
                    );
                }
            }
        }
        return null;
    }

    public boolean deposit(User user, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        conn.setAutoCommit(false);

        try {
            // Update balance
            String updateSql = "UPDATE users SET balance = balance + ? WHERE account_no = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, user.getAccountNo());
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated != 1) {
                    throw new SQLException("Failed to update account balance");
                }
            }

            // Get updated balance
            double newBalance = getBalance(user.getAccountNo());

            // Record transaction
            String txnSql = "INSERT INTO transactions (account_no, type, amount, balance) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(txnSql)) {
                pstmt.setInt(1, user.getAccountNo());
                pstmt.setString(2, "DEPOSIT");
                pstmt.setDouble(3, amount);
                pstmt.setDouble(4, newBalance);
                pstmt.executeUpdate();
            }

            conn.commit();
            user.setBalance(newBalance);
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public boolean withdraw(User user, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (amount > user.getBalance()) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        conn.setAutoCommit(false);

        try {
            // Update balance
            String updateSql = "UPDATE users SET balance = balance - ? WHERE account_no = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, user.getAccountNo());
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated != 1) {
                    throw new SQLException("Failed to update account balance");
                }
            }

            // Get updated balance
            double newBalance = getBalance(user.getAccountNo());

            // Record transaction
            String txnSql = "INSERT INTO transactions (account_no, type, amount, balance) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(txnSql)) {
                pstmt.setInt(1, user.getAccountNo());
                pstmt.setString(2, "WITHDRAWAL");
                pstmt.setDouble(3, amount);
                pstmt.setDouble(4, newBalance);
                pstmt.executeUpdate();
            }

            conn.commit();
            user.setBalance(newBalance);
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public double getBalance(int accountNo) throws SQLException {
        String sql = "SELECT balance FROM users WHERE account_no = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountNo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("balance");
                } else {
                    throw new SQLException("Account not found");
                }
            }
        }
    }

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Add these methods to your ATMService class

    public boolean transfer(User fromUser, int toAccountNo, double amount) throws SQLException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }

        if (amount > fromUser.getBalance()) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        conn.setAutoCommit(false);

        try {
            // Check if recipient account exists
            String checkSql = "SELECT account_no FROM users WHERE account_no = ? AND status = 'active'";
            try (PreparedStatement pstmt = conn.prepareStatement(checkSql)) {
                pstmt.setInt(1, toAccountNo);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new SQLException("Recipient account not found or inactive");
                    }
                }
            }

            // Withdraw from sender
            String withdrawSql = "UPDATE users SET balance = balance - ? WHERE account_no = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(withdrawSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, fromUser.getAccountNo());
                int rowsUpdated = pstmt.executeUpdate();

                if (rowsUpdated != 1) {
                    throw new SQLException("Failed to update sender balance");
                }
            }

            // Deposit to recipient
            String depositSql = "UPDATE users SET balance = balance + ? WHERE account_no = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(depositSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setInt(2, toAccountNo);
                pstmt.executeUpdate();
            }

            // Get updated balance
            double newBalance = getBalance(fromUser.getAccountNo());

            // Record transaction for sender
            String txnSql = "INSERT INTO transactions (account_no, type, amount, balance) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(txnSql)) {
                pstmt.setInt(1, fromUser.getAccountNo());
                pstmt.setString(2, "TRANSFER_OUT");
                pstmt.setDouble(3, amount);
                pstmt.setDouble(4, newBalance);
                pstmt.executeUpdate();
            }

            // Record transaction for recipient
            try (PreparedStatement pstmt = conn.prepareStatement(txnSql)) {
                pstmt.setInt(1, toAccountNo);
                pstmt.setString(2, "TRANSFER_IN");
                pstmt.setDouble(3, amount);
                pstmt.setDouble(4, getBalance(toAccountNo));
                pstmt.executeUpdate();
            }

            conn.commit();
            fromUser.setBalance(newBalance);
            return true;

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public boolean changePin(User user, int newPin) throws SQLException {
        if (newPin < 1000 || newPin > 9999) {
            throw new IllegalArgumentException("PIN must be 4 digits");
        }

        String sql = "UPDATE users SET pin = ? WHERE account_no = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newPin);
            pstmt.setInt(2, user.getAccountNo());
            int rowsUpdated = pstmt.executeUpdate();

            if (rowsUpdated == 1) {
                return true;
            }
        }
        return false;
    }
}