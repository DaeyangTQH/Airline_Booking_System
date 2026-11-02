package com.group3.infrastructure.persistence.adapter;

import com.group3.application.model.PageResult;
import com.group3.application.port.out.UserRepositoryPort;
import com.group3.domain.aggregate.User;
import com.group3.infrastructure.persistence.entity.UserJpaEntity;
import com.group3.infrastructure.persistence.mapper.UserPersistenceMapper;
import com.group3.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserPersistenceAdapter implements UserRepositoryPort {
    private final UserJpaRepository repo;
    private final UserPersistenceMapper mapper;

    public UserPersistenceAdapter(UserJpaRepository repo, UserPersistenceMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public Optional<User> findById(UUID id) { return repo.findById(id).map(mapper::toDomain); }
    @Override
    public Optional<User> findByEmail(String email) { return repo.findByEmailIgnoreCase(email).map(mapper::toDomain); }
    @Override
    public boolean existsByEmail(String email) { return repo.existsByEmailIgnoreCase(email); }

    @Override
    public User save(User user) {
        UserJpaEntity entity = repo.findById(user.getId().value()).orElseGet(UserJpaEntity::new);
        mapper.copyToEntity(user, entity);
        return mapper.toDomain(repo.saveAndFlush(entity));
    }

    @Override
    public PageResult<User> search(String keyword, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "email"));
        Page<UserJpaEntity> result = keyword == null || keyword.isBlank()
                ? repo.findAll(pageable)
                : repo.findByEmailContainingIgnoreCase(keyword, pageable);
        return new PageResult<>(result.getContent().stream().map(mapper::toDomain).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }
}
