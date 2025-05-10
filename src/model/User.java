package model;

public class User {
    private int accountNo;
    private String name;
    private int pin;
    private double balance;
    private String status;


    public User(int accountNo, String name, int pin, double balance, String status){
        this.accountNo = accountNo;
        this.name = name;
        this.pin = pin;
        this.balance = balance;
        this.status = status;

    }

    public int getAccountNo() { return accountNo; }
    public String getName() { return name; }


    public int getPin() { return pin; }
    public double getBalance() { return balance; }
    public String getStatus() { return status; }
    public void setBalance(double balance) {
        this.balance = balance;
    }


}
