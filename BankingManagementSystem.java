import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.io.Serializable;
import java.util.HashMap;

@SuppressWarnings("serial")
class BankAccount implements Serializable {
    private int accountNumber;
    private String accountHolder;
    private double balance;
    private ArrayList<String> transactionHistory;

    public BankAccount(int accountNumber, String accountHolder) {
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.balance = 0.0;
        this.transactionHistory = new ArrayList<>();
        addTransaction("Account created with initial balance: 0.00");
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getAccountHolder() {
        return accountHolder;
    }

    public double getBalance() {
        return balance;
    }

    public ArrayList<String> getTransactionHistory() {
        return new ArrayList<>(transactionHistory);
    }

    private void addTransaction(String message) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = sdf.format(new Date());
        transactionHistory.add(timestamp + " - " + message);
    }

    public void deposit(double amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive.");
        }
        balance += amount;
        addTransaction(String.format("Deposited: %.2f, New Balance: %.2f", amount, balance));
    }

    public void withdraw(double amount) throws IllegalArgumentException {
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive.");
        }
        if (amount > balance) {
            throw new IllegalArgumentException("Insufficient balance.");
        }
        balance -= amount;
        addTransaction(String.format("Withdrew: %.2f, New Balance: %.2f", amount, balance));
    }

    @Override
    public String toString() {
        return String.format("Account #%d: %s\nBalance: %.2f", accountNumber, accountHolder, balance);
    }
}

@SuppressWarnings("serial")
public class BankingManagementSystemGUI extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel loginPanel;
    private JPanel bankingPanel;
    private JTabbedPane tabbedPane;
    private JTextArea outputArea;
    private ArrayList<BankAccount> accounts = new ArrayList<>();
    private HashMap<String, String> userCredentials = new HashMap<>();
    private String currentUser;
    private static final String ACCOUNTS_FILE = "accounts.dat";
    private static final String USERS_FILE = "users.dat";
    private static final String TRANSACTIONS_DIR = "transactions";

    public BankingManagementSystemGUI() {
        loadUserCredentials();
        loadAccounts();

        // Create transactions directory if it doesn't exist
        new File(TRANSACTIONS_DIR).mkdirs();

        setTitle("Banking Management System - Login");
        setSize(600, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        createLoginPanel();
        createBankingPanel();

        mainPanel.add(loginPanel, "login");
        mainPanel.add(bankingPanel, "banking");

        add(mainPanel);
        cardLayout.show(mainPanel, "login");
    }

    @SuppressWarnings("unchecked")
	private void loadUserCredentials() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            userCredentials = (HashMap<String, String>) ois.readObject();
        } catch (FileNotFoundException e) {
            // First run - create default users
            userCredentials.put("user1", "pass1");
            userCredentials.put("admin", "admin123");
            saveUserCredentials();
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading user data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveUserCredentials() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(userCredentials);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving user data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    @SuppressWarnings("unchecked")
	private void loadAccounts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ACCOUNTS_FILE))) {
            accounts = (ArrayList<BankAccount>) ois.readObject();
        } catch (FileNotFoundException e) {
            // No accounts file yet, start with empty list
        } catch (IOException | ClassNotFoundException e) {
            JOptionPane.showMessageDialog(this, "Error loading accounts: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ACCOUNTS_FILE))) {
            oos.writeObject(accounts);
        } catch (IOException e) {
//            JOptionPane.showMessageDialog(this, "Error saving accounts: " + e.getMessage(),
//                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveTransactionToFile(BankAccount account) {
        String filename = TRANSACTIONS_DIR + "/account_" + account.getAccountNumber() + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("Transaction History for Account #" + account.getAccountNumber());
            writer.println("Account Holder: " + account.getAccountHolder());
            writer.println("Current Balance: " + account.getBalance());
            writer.println("\nTransactions:");
            for (String transaction : account.getTransactionHistory()) {
                writer.println(transaction);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error saving transaction history: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void createLoginPanel() {
        loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel userLabel = new JLabel("Username:");
        JLabel passLabel = new JLabel("Password:");

        JTextField userField = new JTextField(15);
        JPasswordField passField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");

        gbc.gridx = 0;
        gbc.gridy = 0;
        loginPanel.add(userLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        loginPanel.add(userField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        loginPanel.add(passField, gbc);

        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        loginPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        loginPanel.add(registerButton, gbc);

        loginButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
                currentUser = username;
                JOptionPane.showMessageDialog(this, "Login successful! Welcome " + username);
                setTitle("Banking Management System - User: " + username);
                userField.setText("");
                passField.setText("");
                outputArea.setText("");
                cardLayout.show(mainPanel, "banking");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid username or password.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        registerButton.addActionListener(e -> {
            String username = userField.getText().trim();
            String password = new String(passField.getPassword());

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Username and password cannot be empty.",
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (userCredentials.containsKey(username)) {
                JOptionPane.showMessageDialog(this, "Username already exists.",
                        "Registration Failed", JOptionPane.ERROR_MESSAGE);
            } else {
                userCredentials.put(username, password);
                saveUserCredentials();
                JOptionPane.showMessageDialog(this, "Registration successful! You can now login.");
                userField.setText("");
                passField.setText("");
            }
        });
    }

    private void createBankingPanel() {
        bankingPanel = new JPanel(new BorderLayout());

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Create Account", createAccountPanel());
        tabbedPane.addTab("Deposit", depositPanel());
        tabbedPane.addTab("Withdraw", withdrawPanel());
        tabbedPane.addTab("Check Balance", balancePanel());
        tabbedPane.addTab("Transaction History", transactionHistoryPanel());

        outputArea = new JTextArea(10, 50);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to logout?", "Confirm Logout",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                setTitle("Banking Management System - Login");
                currentUser = null;
                cardLayout.show(mainPanel, "login");
            }
        });

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(logoutButton, BorderLayout.WEST);
        topPanel.add(new JLabel("Logged in as: " + currentUser), BorderLayout.CENTER);

        bankingPanel.add(topPanel, BorderLayout.NORTH);
        bankingPanel.add(tabbedPane, BorderLayout.CENTER);
        bankingPanel.add(scrollPane, BorderLayout.SOUTH);
    }

    private JPanel createAccountPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel accNumLabel = new JLabel("Account Number:");
        JTextField accNumField = new JTextField();
        JLabel accHolderLabel = new JLabel("Account Holder Name:");
        JTextField accHolderField = new JTextField();
        JButton createButton = new JButton("Create Account");

        // Input filtering to allow only digits
        accNumField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c)) {
                    e.consume(); // Ignore non-digit input
                    JOptionPane.showMessageDialog(panel, 
                        "Only numbers (0-9) are allowed in Account Number.",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        createButton.addActionListener(e -> {
            String accNumText = accNumField.getText().trim();
            String accHolder = accHolderField.getText().trim();

            // Validate Account Number
            if (accNumText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Account number cannot be empty.", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Additional check for numeric (redundant but safe)
            if (!accNumText.matches("\\d+")) {
                JOptionPane.showMessageDialog(this,
                    "Account number must contain only numbers (0-9).", "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int accNum = Integer.parseInt(accNumText);

                // Validate positive number
                if (accNum <= 0) {
                    JOptionPane.showMessageDialog(this,
                        "Account number must be a positive number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Validate Account Holder Name
                if (accHolder.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Account Holder name cannot be empty.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Check for duplicate account
                if (findAccount(accNum) != null) {
                    JOptionPane.showMessageDialog(this,
                        "Account number already exists.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Create account if all validations pass
                BankAccount account = new BankAccount(accNum, accHolder);
                accounts.add(account);
                saveAccounts();
                saveTransactionToFile(account);
                outputArea.append("Account created successfully:\n" + account + "\n\n");

                // Clear fields
                accNumField.setText("");
                accHolderField.setText("");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Invalid Account Number format.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(accNumLabel);
        panel.add(accNumField);
        panel.add(accHolderLabel);
        panel.add(accHolderField);
        panel.add(new JLabel());
        panel.add(createButton);

        return panel;
    }
    private JPanel depositPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel accNumLabel = new JLabel("Account Number:");
        JTextField accNumField = new JTextField();
        JLabel amountLabel = new JLabel("Amount to Deposit:");
        JTextField amountField = new JTextField();
        JButton depositButton = new JButton("Deposit");

        depositButton.addActionListener(e -> {
            try {
                int accNum = Integer.parseInt(accNumField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());

                BankAccount acc = findAccount(accNum);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this,
                            "Account not found.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                acc.deposit(amount);
                saveAccounts();
                saveTransactionToFile(acc);
                outputArea.append(String.format("Deposited %.2f to Account #%d\nNew Balance: %.2f\n\n", 
                        amount, accNum, acc.getBalance()));
                accNumField.setText("");
                amountField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(accNumLabel);
        panel.add(accNumField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(new JLabel());
        panel.add(depositButton);

        return panel;
    }

    private JPanel withdrawPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 10, 10));

        JLabel accNumLabel = new JLabel("Account Number:");
        JTextField accNumField = new JTextField();
        JLabel amountLabel = new JLabel("Amount to Withdraw:");
        JTextField amountField = new JTextField();
        JButton withdrawButton = new JButton("Withdraw");

        withdrawButton.addActionListener(e -> {
            try {
                int accNum = Integer.parseInt(accNumField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());

                BankAccount acc = findAccount(accNum);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this,
                            "Account not found.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                acc.withdraw(amount);
                saveAccounts();
                saveTransactionToFile(acc);
                outputArea.append(String.format("Withdrew %.2f from Account #%d\nNew Balance: %.2f\n\n", 
                        amount, accNum, acc.getBalance()));
                accNumField.setText("");
                amountField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid input. Please enter numeric values.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(accNumLabel);
        panel.add(accNumField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(new JLabel());
        panel.add(withdrawButton);

        return panel;
    }

    private JPanel balancePanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel accNumLabel = new JLabel("Account Number:");
        JTextField accNumField = new JTextField();
        JButton checkBalanceButton = new JButton("Check Balance");

        checkBalanceButton.addActionListener(e -> {
            try {
                int accNum = Integer.parseInt(accNumField.getText().trim());
                BankAccount acc = findAccount(accNum);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this,
                            "Account not found.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                outputArea.append(acc + "\n\n");
                accNumField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Account Number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(accNumLabel);
        panel.add(accNumField);
        panel.add(new JLabel());
        panel.add(checkBalanceButton);

        return panel;
    }

    private JPanel transactionHistoryPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        JLabel accNumLabel = new JLabel("Account Number:");
        JTextField accNumField = new JTextField();
        JButton viewHistoryButton = new JButton("View Transaction History");
        JButton saveHistoryButton = new JButton("Save to File");

        viewHistoryButton.addActionListener(e -> {
            try {
                int accNum = Integer.parseInt(accNumField.getText().trim());
                BankAccount acc = findAccount(accNum);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this,
                            "Account not found.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                outputArea.append("Transaction History for Account #" + accNum + "\n");
                outputArea.append("Account Holder: " + acc.getAccountHolder() + "\n");
                outputArea.append("Current Balance: " + acc.getBalance() + "\n\n");
                
                for (String transaction : acc.getTransactionHistory()) {
                    outputArea.append(transaction + "\n");
                }
                outputArea.append("\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Account Number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        saveHistoryButton.addActionListener(e -> {
            try {
                int accNum = Integer.parseInt(accNumField.getText().trim());
                BankAccount acc = findAccount(accNum);
                if (acc == null) {
                    JOptionPane.showMessageDialog(this,
                            "Account not found.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                saveTransactionToFile(acc);
                JOptionPane.showMessageDialog(this,
                        "Transaction history saved to file: account_" + accNum + ".txt",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Account Number.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        panel.add(accNumLabel);
        panel.add(accNumField);
        panel.add(viewHistoryButton);
        panel.add(saveHistoryButton);

        return panel;
    }

    private BankAccount findAccount(int accountNumber) {
        for (BankAccount acc : accounts) {
            if (acc.getAccountNumber() == accountNumber) {
                return acc;
            }
        }
        return null;
    }

    public static void main(String args[]) {
        SwingUtilities.invokeLater(() -> {
            BankingManagementSystemGUI app = new BankingManagementSystemGUI();
            app.setVisible(true);
        });
    }
}