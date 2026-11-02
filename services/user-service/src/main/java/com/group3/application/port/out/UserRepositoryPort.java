package com.group3.application.port.out;

import com.group3.application.model.PageResult;
import com.group3.domain.aggregate.User;
import java.util.Optional;
import java.util.UUID;

public interface UserRepositoryPort {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    User save(User user);
    PageResult<User> search(String keyword, int page, int size);
}
