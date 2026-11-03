package com.group3.application.port.in;
import com.group3.application.command.LoginCommand;
import com.group3.application.command.RegisterCommand;
import com.group3.application.model.AuthResult;
public interface AuthUseCase {
    AuthResult register(RegisterCommand command);
    AuthResult login(LoginCommand command);
}
