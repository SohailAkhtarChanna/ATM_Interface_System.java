package ui;

import model.User;
import service.ATMService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class TransferFrame extends JFrame {
    private User user;
    private ATMService atmService;
    private JTextField amountField;
    private JTextField accountField;
    private static final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private JLabel balanceLabel;

    public TransferFrame(User user) {
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
        setTitle("Transfer Money - " + user.getName());
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(new Color(240, 240, 240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Current Balance Label
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        balanceLabel = new JLabel("Current Balance: " + currencyFormat.format(user.getBalance()));
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(balanceLabel, gbc);

        // Recipient Account Field
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Recipient Account:"), gbc);

        gbc.gridx = 1;
        accountField = new JTextField(15);
        accountField.setFont(new Font("Arial", Font.PLAIN, 14));
        add(accountField, gbc);

        // Amount Field
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Transfer Amount:"), gbc);

        gbc.gridx = 1;
        amountField = new JTextField(15);
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        add(amountField, gbc);

        // Buttons Panel
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton transferButton = createStyledButton("Transfer", new Color(0, 150, 0));
        JButton cancelButton = createStyledButton("Cancel", new Color(200, 0, 0));

        buttonPanel.add(transferButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);

        // Add action listeners
        transferButton.addActionListener(this::handleTransfer);
        cancelButton.addActionListener(e -> dispose());

        // Add Enter key support
        amountField.addActionListener(this::handleTransfer);

        pack();
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
        return button;
    }

    private void handleTransfer(ActionEvent e) {
        try {
            int toAccount = Integer.parseInt(accountField.getText().trim());
            double amount = Double.parseDouble(amountField.getText().trim());

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a positive amount",
                        "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (toAccount == user.getAccountNo()) {
                JOptionPane.showMessageDialog(this,
                        "Cannot transfer to your own account",
                        "Invalid Account", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = atmService.transfer(user, toAccount, amount);

            if (success) {
                // Fetch updated balance from DB
                double updatedBalance = atmService.getBalance(user.getAccountNo());
                user.setBalance(updatedBalance);
                balanceLabel.setText("Current Balance: " + currencyFormat.format(updatedBalance));

                JOptionPane.showMessageDialog(this,
                        "Transfer successful!\nNew Balance: " + currencyFormat.format(updatedBalance),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Transfer failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for account and amount",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Transfer Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error processing transfer: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}