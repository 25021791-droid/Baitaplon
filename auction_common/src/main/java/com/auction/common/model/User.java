package com.auction.common.model;

public abstract class User extends Entity<Integer> {

	private String name;
	private String email;
	private String role;
	private boolean isLoggedIn;

	public User(Integer userId, String name, String email, String role) {
		super(userId);
		this.name = name;
		this.email = email;
		this.role = role;
		this.isLoggedIn = false;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getRole() {
		return this.role;
	}

	public boolean isLoggedIn() {
		return this.isLoggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.isLoggedIn = loggedIn;
	}

	public void logout() {
		if (isLoggedIn) {
			isLoggedIn = false;
			System.out.println("Logout successful.");
		} else {
			System.out.println("You are not logged in.");
		}
	}

	public abstract void displayDashboard();
}