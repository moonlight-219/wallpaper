package com.wallpaper.server.util;

public class GeneratePassword {
    public static void main(String[] args) {
        String password = "123456";
        String encodedPassword = PasswordUtil.encode(password);
        System.out.println("明文密码: " + password);
        System.out.println("加密密码: " + encodedPassword);
    }
}
