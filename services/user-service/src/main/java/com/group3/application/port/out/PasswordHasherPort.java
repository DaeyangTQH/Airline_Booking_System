package com.group3.application.port.out;
public interface PasswordHasherPort {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
