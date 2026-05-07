package com.auction.service;
import com.auction.exception.*;
public class UserService {
    public boolean login(String username, String password)
            throws AuthenticationException {
        if (username.equals("dat") && password.equals("123")) {
            return true;
        }
        throw new AuthenticationException("Sai tài khoản hoặc mật khẩu!");
    }
}