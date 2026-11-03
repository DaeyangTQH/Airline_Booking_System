package com.group3.domain.aggregate;

import com.group3.domain.exception.DomainValidationException;
import com.group3.domain.vo.Email;
import com.group3.domain.vo.UserId;

public class User {
    private final UserId id;
    private Email email;
    private String passwordHash;
    private String phone;
    private final UserRole role;
    private boolean active;

    private User(UserId id, Email email, String hash, String phone, UserRole role, boolean active) {
        if (id == null || email == null || role == null || hash == null || hash.isBlank()) {
            throw new DomainValidationException("Invalid user data");
        }
        this.id = id;
        this.email = email;
        this.passwordHash = hash;
        this.phone = normalizePhone(phone);
        this.role = role;
        this.active = active;
    }

    public static User register(Email email, String passwordHash, String phone) {
        return new User(UserId.newId(), email, passwordHash, phone, UserRole.CUSTOMER, true);
    }

    public static User restore(UserId id, Email email, String hash, String phone,
                               UserRole role, boolean active) {
        return new User(id, email, hash, phone, role, active);
    }

    public void updateProfile(Email email, String phone) {
        if (email == null) throw new DomainValidationException("Email is required");
        this.email = email;
        this.phone = normalizePhone(phone);
    }

    public void changePasswordHash(String newHash) {
        if (newHash == null || newHash.isBlank()) {
            throw new DomainValidationException("Password hash is required");
        }
        this.passwordHash = newHash;
    }

    public void changeActive(boolean enabled) {
        if (role == UserRole.ADMIN) {
            throw new DomainValidationException("Admin account status cannot be changed here");
        }
        this.active = enabled;
    }

    private static String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String clean = phone.trim();
        if (clean.length() > 20) throw new DomainValidationException("Phone is too long");
        return clean;
    }

    public UserId getId() { return id; }
    public Email getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public String getPhone() { return phone; }
    public UserRole getRole() { return role; }
    public boolean isActive() { return active; }
}
