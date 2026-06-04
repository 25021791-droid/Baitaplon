package com.auction.client.utils;

import com.auction.common.model.User;

public class UserSession {

    private static volatile User currentUser;

    public static void setUser(User user) {
        currentUser = user;
    }

    public static User getUser() {
        return currentUser;
    }

    public static void cleanUserSession() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}