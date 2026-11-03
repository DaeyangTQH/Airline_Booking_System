package com.group3.application.port.in;
import com.group3.application.model.PageResult;
import com.group3.domain.aggregate.User;
import java.util.UUID;
public interface AdminUserUseCase {
    PageResult<User> list(UUID adminId, String keyword, int page, int size);
    User get(UUID adminId, UUID userId);
    User updateStatus(UUID adminId, UUID userId, boolean active);
}
