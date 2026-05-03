package com.release.klinikgaharumedika.model;

public class User {

    private final int id;
    private final String username;
    private final String passwordHash;
    private final String fullName;
    private final String role;
    private final boolean active;

    public User(int id, String username, String passwordHash, String fullName, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public String getDisplayName() {
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }

        return username;
    }
}
