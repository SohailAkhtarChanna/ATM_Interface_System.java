package ui;

import model.User;
import service.ATMService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

public class MainMenuFrame extends JFrame {
    private User user;
    private ATMService atmService;

    public MainMenuFrame(User user) {
        this.user = user;

        try {
            this.atmService = new ATMService();
            initializeUI();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "Error initializing ATM service: " + e.getMessage(),
                    "Service Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void initializeUI() {
        setTitle("ATM Main Menu - " + user.getName());
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 1, 10, 10));

        // Create buttons
        JButton depositButton = new JButton("Deposit Money");
        JButton withdrawButton = new JButton("Withdraw Money");
        JButton transferButton = new JButton("Transfer Money");
        JButton miniStatementButton = new JButton("Mini Statement");
        JButton changePinButton = new JButton("Change PIN");
        JButton logoutButton = new JButton("Logout");

        // Style buttons
        Font buttonFont = new Font("Arial", Font.BOLD, 14);
        Color buttonColor = new Color(50, 120, 200);

        for (JButton button : new JButton[]{depositButton, withdrawButton, transferButton,
                miniStatementButton, changePinButton, logoutButton}) {
            button.setFont(buttonFont);
            button.setBackground(buttonColor);
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            add(button);
        }

        // Add action listeners
        depositButton.addActionListener(e -> {
            dispose();
            new DepositFrame(user);
        });

        withdrawButton.addActionListener(e -> {
            dispose();
            new WithdrawFrame(user);
        });

        transferButton.addActionListener(e -> {
            dispose();
            new TransferFrame(user);
        });

        miniStatementButton.addActionListener(e -> {
            String statement = String.format("Account: %s\nBalance: $%.2f",
                    user.getName(), user.getBalance());
            JOptionPane.showMessageDialog(this, statement, "Account Statement",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        changePinButton.addActionListener(e -> {
            String newPinStr = JOptionPane.showInputDialog(this, "Enter new 4-digit PIN:");
            if (newPinStr != null && !newPinStr.isEmpty()) {
                try {
                    int newPin = Integer.parseInt(newPinStr);
                    if (newPin < 1000 || newPin > 9999) {
                        JOptionPane.showMessageDialog(this,
                                "PIN must be 4 digits (1000-9999)",
                                "Invalid PIN", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    boolean success = atmService.changePin(user, newPin);
                    if (success) {
                        JOptionPane.showMessageDialog(this,
                                "PIN changed successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to change PIN. Please try again.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                            "Please enter a valid 4-digit number",
                            "Invalid Input", JOptionPane.WARNING_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error changing PIN: " + ex.getMessage(),
                            "System Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User testUser = new User(123456, "Test User", 1234, 1000.0, "active");
            new MainMenuFrame(testUser);
        });
    }
}