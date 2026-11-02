package com.group3.application.port.in;
import com.group3.application.command.ChangePasswordCommand;
import com.group3.application.command.UpdateProfileCommand;
import com.group3.domain.aggregate.User;
import java.util.UUID;
public interface UserProfileUseCase {
    User getMyProfile(UUID userId);
    User updateMyProfile(UUID userId, UpdateProfileCommand command);
    void changePassword(UUID userId, ChangePasswordCommand command);
}
