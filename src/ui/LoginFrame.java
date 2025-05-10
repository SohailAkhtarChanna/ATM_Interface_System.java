 package ui;

import model.User;
import service.ATMService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField accountField;
    private JPasswordField pinField;
    private ATMService atmService;
    private int attemptCount = 0;
    private static final int MAX_ATTEMPTS = 3;

    public LoginFrame() {
        try {
            atmService = new ATMService();
            initializeUI();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "Failed to initialize ATM service: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initializeUI() {
        setTitle("ATM Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Account Number Field
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Account Number:"), gbc);

        gbc.gridx = 1;
        accountField = new JTextField(15);
        accountField.setFont(new Font("Arial", Font.PLAIN, 14));
        add(accountField, gbc);

        // PIN Field
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("PIN:"), gbc);

        gbc.gridx = 1;
        pinField = new JPasswordField(15);
        pinField.setFont(new Font("Arial", Font.PLAIN, 14));
        add(pinField, gbc);

        // Login Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton loginButton = new JButton("Login");
        styleButton(loginButton, new Color(0, 120, 215));
        loginButton.addActionListener(this::handleLogin);
        add(loginButton, gbc);

        // Exit Button
        gbc.gridy = 3;
        JButton exitButton = new JButton("Exit");
        styleButton(exitButton, new Color(200, 60, 60));
        exitButton.addActionListener(e -> System.exit(0));
        add(exitButton, gbc);

        // Add key listener for Enter key
        pinField.addActionListener(this::handleLogin);
    }

    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(120, 35));
    }

    private void handleLogin(ActionEvent e) {
        try {
            int accNo = Integer.parseInt(accountField.getText().trim());
            int pin = Integer.parseInt(new String(pinField.getPassword()).trim());

            User user = atmService.authenticate(accNo, pin);

            if (user != null) {
                JOptionPane.showMessageDialog(this,
                        "Welcome, " + user.getName() + "!",
                        "Login Successful", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                SwingUtilities.invokeLater(() -> new MainMenuFrame(user));
            } else {
                attemptCount++;
                if (attemptCount >= MAX_ATTEMPTS) {
                    JOptionPane.showMessageDialog(this,
                            "Maximum login attempts reached. System will exit.",
                            "Security Alert", JOptionPane.ERROR_MESSAGE);
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Invalid Credentials. Attempts left: " + (MAX_ATTEMPTS - attemptCount),
                            "Login Failed", JOptionPane.WARNING_MESSAGE);
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for Account and PIN",
                    "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error during login: " + ex.getMessage(),
                    "System Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame();
            frame.setVisible(true);
        });
    }
} 