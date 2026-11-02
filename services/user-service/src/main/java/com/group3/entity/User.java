package com.group3.entity;

import com.group3.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.UUID;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                )
        }
)
public class User extends BaseEntity {

    // ========================================
    // 1. PRIMARY KEY
    // ========================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(
            name = "id",
            nullable = false,
            updatable = false
    )
    private UUID id;

    // ========================================
    // 2. ACCOUNT INFORMATION
    // ========================================

    @Column(
            name = "email",
            nullable = false,
            length = 255
    )
    private String email;

    @Column(
            name = "password_hash",
            nullable = false,
            length = 255
    )
    private String passwordHash;

    @Column(
            name = "phone",
            length = 20
    )
    private String phone;

    // CUSTOMER / ADMIN / STAFF
    @Column(
            name = "role",
            nullable = false,
            length = 20
    )
    private String role = "CUSTOMER";

    @Column(
            name = "is_active",
            nullable = false
    )
    private boolean active = true;

    // created_at va updated_at
    // duoc ke thua tu BaseEntity

    // ========================================
    // 3. CONSTRUCTORS
    // ========================================

    public User() {
    }

    public User(
            String email,
            String passwordHash,
            String phone
    ) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.phone = phone;
        this.role = "CUSTOMER";
        this.active = true;
    }

    // ========================================
    // 4. GETTERS AND SETTERS
    // ========================================

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
