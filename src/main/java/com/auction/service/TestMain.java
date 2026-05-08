package com.auction.service;

import com.auction.model.User;

public class TestMain {
    public static void main(String[] args) {
        UserService userService = new UserService();

        boolean testRegister = false;
        boolean testLogin = false;

        // -- Test Register
        if (testRegister) {
            boolean isRegisterOk = userService.register("user", "aaa", "test@gmail.com");
            if (isRegisterOk) {
                System.out.println("=> Đăng ký thành công!");
            } else {
                System.out.println("=> Đăng ký thất bại");
            }
        }

        // -- Test Login
        if (testLogin) {
            User user = userService.login("user", "aaa");

            if (user != null) {
                System.out.println("=> Login OK! Chào mừng: " + user.getName());
                System.out.println("=> Role của bạn là: " + user.getRole());
            } else {
                System.out.println("=> Login thất bại! Sai user hoặc pass.");
            }
        }
    }
}