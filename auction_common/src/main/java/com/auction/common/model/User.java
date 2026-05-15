package com.auction.common.model;

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
            System.out.println("Logout successful.");
        }
        else {
            System.out.println("You are not logged in.");
        }
    }
	public abstract void displayDashboard();
}
