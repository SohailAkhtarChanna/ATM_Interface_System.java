package ui;

import model.User;
import service.ATMService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;

public class DepositFrame extends JFrame {
    private User user;
    private ATMService atmService;
    private JTextField amountField;
    private static final DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private JLabel balanceLabel;

    public DepositFrame(User user) {
        this.user = user;

        try {
            this.atmService = new ATMService();
            initializeUI();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Error initializing ATM service: " + e.getMessage(),
                    "Service Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void initializeUI() {
        setTitle("Deposit Money - " + user.getName());
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
        add(new JLabel("Deposit Amount:"), gbc);

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

        JButton depositButton = createStyledButton("Deposit", new Color(0, 150, 0));
        JButton cancelButton = createStyledButton("Cancel", new Color(200, 0, 0));

        buttonPanel.add(depositButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);

        // Add action listeners
        depositButton.addActionListener(this::handleDeposit);
        cancelButton.addActionListener(e -> dispose());

        // Add Enter key support
        amountField.addActionListener(this::handleDeposit);

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

    private void handleDeposit(ActionEvent e) {
        try {
            double amount = Double.parseDouble(amountField.getText().trim());

            if (amount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter a positive amount",
                        "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (amount > 10000) {
                JOptionPane.showMessageDialog(this,
                        "Maximum deposit amount is $10,000 per transaction",
                        "Amount Limit", JOptionPane.WARNING_MESSAGE);
                return;
            }

            boolean success = atmService.deposit(user, amount);

            if (success) {
                // Fetch updated balance from DB
                double updatedBalance = atmService.getBalance(user.getAccountNo());
                user.setBalance(updatedBalance);
                balanceLabel.setText("Current Balance: " + currencyFormat.format(updatedBalance));

                JOptionPane.showMessageDialog(this,
                        "Deposit successful!\nNew Balance: " + currencyFormat.format(updatedBalance),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Deposit failed. Please try again.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid numeric amount",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error processing deposit: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            User testUser = new User(1234, "Test User", 1234, 1000.0, "active");
            new DepositFrame(testUser);
        });
    }
}