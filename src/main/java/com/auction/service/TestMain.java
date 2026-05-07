package com.auction.service;

import com.auction.model.User;

public class TestMain {
    public static void main(String[] args) {
        UserService userService = new UserService();

        // -- Test Register
        boolean isRegisterOk = userService.register("dat", "123", "a@gmail.com");
        if (isRegisterOk) {
            System.out.println("=> Đăng ký thành công!");
        } else {
            System.out.println("=> Đăng ký thất bại");
        }

        // -- Test Login
        User user = userService.login("dat", "124");

        if (user != null) {
            System.out.println("=> Login OK! Chào mừng: " + user.getName());
            System.out.println("=> Role của bạn là: " + user.getRole());
        } else {
            System.out.println("=> Login thất bại! Sai user hoặc pass.");
        }
    }
}