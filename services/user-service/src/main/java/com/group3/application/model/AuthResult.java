package com.group3.application.model;
import com.group3.domain.aggregate.User;
public record AuthResult(User user, TokenResult token) { }
