package com.group3.infrastructure.persistence.mapper;

import com.group3.domain.aggregate.User;
import com.group3.domain.aggregate.UserRole;
import com.group3.domain.vo.Email;
import com.group3.domain.vo.UserId;
import com.group3.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserPersistenceMapper {
    public User toDomain(UserJpaEntity e) {
        return User.restore(new UserId(e.getId()), new Email(e.getEmail()),
                e.getPasswordHash(), e.getPhone(), UserRole.valueOf(e.getRole()), e.isActive());
    }

    public void copyToEntity(User user, UserJpaEntity e) {
        e.setId(user.getId().value());
        e.setEmail(user.getEmail().value());
        e.setPasswordHash(user.getPasswordHash());
        e.setPhone(user.getPhone());
        e.setRole(user.getRole().name());
        e.setActive(user.isActive());
    }
}
