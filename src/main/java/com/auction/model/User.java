package com.auction.model;

public abstract class User {
    private int userId;
    private String userName;
    private String password;
    private String email; 
    private boolean isLoggedIn;
    public User(int userId, String userName, String password, String email) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.isLoggedIn = false;
    } 
    public int getId() {
        return this.userId;
    }
    public String getName() {
        return this.userName;
    }
    public boolean checkPass(String inputPass) {
        if (this.password.equals(inputPass)) {
            return true;
        }
        return false;
    }
    public String getEmail() {
        return this.email;
    }
    public boolean login(String inputName, String inputPass) {
        if ((this.userName.equals(inputName)||this.email.equals(inputName)) && this.checkPass(inputPass)) {
            this.isLoggedIn = true;
            return true;
        }
        return false;
    }
    public void logout() {
        if (isLoggedIn) {
            isLoggedIn = false;
            System.out.println("Đăng xuất thành công");
        }
        else {
            System.out.println("Bạn chưa đăng nhập");
        }
    }
    public abstract void Dashboard();
}
