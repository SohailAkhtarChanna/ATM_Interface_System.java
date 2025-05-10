package ui;

import model.User;
import service.ATMService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class WithdrawFrame extends JFrame {
    private User user;
    private ATMService atmService;
    private JTextField amountField;
    private static final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private JLabel balanceLabel;

    public WithdrawFrame(User user) {
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
        setTitle("Withdraw Money - " + user.getName());
        setSize(400, 250);
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

        // Amount Field
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        add(new JLabel("Withdraw Amount:"), gbc);

        gbc.gridx = 1;
        amountField = new JTextField(15);
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));
        add(amountField, gbc);

        // Buttons Panel
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton withdrawButton = createStyledButton("Withdraw", new Color(0, 150, 0));
        JButton cancelButton = createStyledButton("Cancel", new Color(200, 0, 0));

        buttonPanel.add(withdrawButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);

        // Add action listeners
        withdrawButton.addActionListener(this::handleWithdraw);
        cancelButton.addActionListener(e -> dispose());

        // Add Enter key support
        amountField.addActionListener(this::handleWithdraw);

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

    private void handleWithdraw(ActionEvent e) {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a positive amount",
                        "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = atmService.withdraw(user, amount);

            if (success) {
                // Fetch updated balance from DB
                double updatedBalance = atmService.getBalance(user.getAccountNo());
                user.setBalance(updatedBalance);

                JOptionPane.showMessageDialog(this,
                        "Withdrawal successful!\nNew Balance: " + currencyFormat.format(updatedBalance),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Withdrawal failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric amount",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    ex.getMessage(),
                    "Withdrawal Error", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error processing withdrawal: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User testUser = new User(1234, "Test User", 1234, 1000.0, "active");
            new WithdrawFrame(testUser);
        });
    }
}