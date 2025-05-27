package com.example.atmsystem;

import java.io.*;
import java.util.*;

// --------- Model ---------
class Account {
    private String accountNumber;
    private String holderName;
    private double balance;

    public Account(String accountNumber, String holderName, double balance) {
        this.accountNumber = accountNumber;
        this.holderName = holderName;
        this.balance = balance;
    }

    public String getAccountNumber() { return accountNumber; }
    public String getHolderName() { return holderName; }
    public double getBalance() { return balance; }

    public void deposit(double amount) { balance += amount; }
    public boolean withdraw(double amount) {
        if (balance >= amount) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public String toFileString() {
        return accountNumber + "," + holderName + "," + balance;
    }

    public static Account fromFileString(String line) {
        String[] parts = line.split(",");
        return new Account(parts[0], parts[1], Double.parseDouble(parts[2]));
    }
}

// --------- DAO ---------
class AccountDAO {
    private static final String FILE_NAME = "accounts.txt";

    public AccountDAO() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) file.createNewFile();
    }

    public List<Account> getAllAccounts() throws IOException {
        List<Account> accounts = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME));
        String line;
        while ((line = reader.readLine()) != null) {
            accounts.add(Account.fromFileString(line));
        }
        reader.close();
        return accounts;
    }

    public void saveAccounts(List<Account> accounts) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
        for (Account acc : accounts) {
            writer.write(acc.toFileString());
            writer.newLine();
        }
        writer.close();
    }
}

// --------- Service ---------
class ATMService {
    private AccountDAO dao;
    private List<Account> accounts;

    public ATMService() throws IOException {
        dao = new AccountDAO();
        accounts = dao.getAllAccounts();
    }

    public void createAccount(String number, String name, double initialBalance) throws IOException {
        if (number == null || name == null || initialBalance < 0)
            throw new IllegalArgumentException("Invalid input");

        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(number))
                throw new IllegalArgumentException("Account already exists");
        }
        accounts.add(new Account(number, name, initialBalance));
        dao.saveAccounts(accounts);
    }

    public Account findAccount(String number) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(number)) return acc;
        }
        return null;
    }

    public void deposit(String number, double amount) throws IOException {
        Account acc = findAccount(number);
        if (acc == null) throw new IllegalArgumentException("Account not found");
        acc.deposit(amount);
        dao.saveAccounts(accounts);
    }

    public boolean withdraw(String number, double amount) throws IOException {
        Account acc = findAccount(number);
        if (acc == null) throw new IllegalArgumentException("Account not found");
        boolean success = acc.withdraw(amount);
        dao.saveAccounts(accounts);
        return success;
    }

    public void displayAccounts() {
        for (Account acc : accounts) {
            System.out.printf("%s | %s | %.2f\n", acc.getAccountNumber(), acc.getHolderName(), acc.getBalance());
        }
    }
}

// --------- Main (UI Layer) ---------
public class Main {
    public static void main(String[] args) {
        try {
            Scanner scanner = new Scanner(System.in);
            ATMService service = new ATMService();

            while (true) {
                System.out.println("\n--- ATM System ---");
                System.out.println("1. Create Account");
                System.out.println("2. Deposit");
                System.out.println("3. Withdraw");
                System.out.println("4. Show Accounts");
                System.out.println("5. Exit");
                System.out.print("Enter choice: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        System.out.print("Enter account number: ");
                        String number = scanner.nextLine();
                        System.out.print("Enter holder name: ");
                        String name = scanner.nextLine();
                        System.out.print("Enter initial balance: ");
                        double balance = scanner.nextDouble();
                        service.createAccount(number, name, balance);
                        break;
                    case 2:
                        System.out.print("Enter account number: ");
                        number = scanner.nextLine();
                        System.out.print("Enter deposit amount: ");
                        double dep = scanner.nextDouble();
                        service.deposit(number, dep);
                        break;
                    case 3:
                        System.out.print("Enter account number: ");
                        number = scanner.nextLine();
                        System.out.print("Enter withdrawal amount: ");
                        double with = scanner.nextDouble();
                        if (!service.withdraw(number, with)) {
                            System.out.println("Insufficient balance.");
                        }
                        break;
                    case 4:
                        service.displayAccounts();
                        break;
                    case 5:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid choice.");
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
