package com.group3.application.port.out;
import com.group3.application.model.TokenResult;
import com.group3.domain.aggregate.User;
public interface AccessTokenPort {
    TokenResult issue(User user);
}
