package com.auction.model;

public abstract class User {
	private int userId;
	private String userName;
	private String email;
	private String role;
	private boolean isLoggedIn;

	public User(int userId, String userName, String email, String role) {
		this.userId = userId;
		this.userName = userName;
		this.email = email;
		this.isLoggedIn = false;
		this.role = role;
	}
	
	public int getId() {
		return this.userId;
	}
	public String getName() {
		return this.userName;
	}
	public String getEmail() {
		return this.email;
	}
	public String getRole() {
		return this.role;
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
	public abstract void displayDashboard();
}
