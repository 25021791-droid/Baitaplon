package com.auction.server.service;

import com.auction.common.model.User;

public class TestMain {
    public static void main(String[] args) {
        UserService userService = new UserService();

        boolean testRegister = false;
        boolean testLogin = false;

        
        if (testRegister) {
            boolean isRegisterOk = userService.register("user", "aaa", "test@gmail.com", "USER");
            if (isRegisterOk) {
                System.out.println("=> Đăng ký thành công!");
            } else {
                System.out.println("=> Đăng ký thất bại");
            }
        }

        
        if (testLogin) {
            UserService userService1 = new UserService();

            User user = userService1.login("user", "aaa");

            if (user != null) {
                System.out.println("=> Login OK! Chào mừng: " + user.getName());
                System.out.println("=> Role của bạn là: " + user.getRole());
            } else {
                System.out.println("=> Login thất bại! Sai user hoặc pass.");
            }
        }
    }
}