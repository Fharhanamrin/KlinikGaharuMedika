package com.release.klinikgaharumedika.controller;

import com.release.klinikgaharumedika.model.User;
import com.release.klinikgaharumedika.repository.UserRepository;
import com.release.klinikgaharumedika.state.SessionManager;
import java.sql.SQLException;
import java.util.Optional;

public class AuthController {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    public AuthController() {
        this.userRepository = new UserRepository();
        this.sessionManager = SessionManager.getInstance();
    }

    public AuthResult login(String username, String password) {
        String normalizedUsername = username == null ? "" : username.trim();
        String normalizedPassword = password == null ? "" : password;

        if (normalizedUsername.isBlank()) {
            return AuthResult.failure("Username wajib diisi.");
        }

        if (normalizedPassword.isBlank()) {
            return AuthResult.failure("Password wajib diisi.");
        }

        try {
            Optional<User> userOptional = userRepository.findByUsername(normalizedUsername);
            if (userOptional.isEmpty()) {
                return AuthResult.failure("Username tidak ditemukan.");
            }

            User user = userOptional.get();

            if (!user.isActive()) {
                return AuthResult.failure("Akun nonaktif. Hubungi admin.");
            }

            if (!matchesPassword(normalizedPassword, user.getPasswordHash())) {
                return AuthResult.failure("Password salah.");
            }

            sessionManager.setCurrentUser(user);
            return AuthResult.success(user);
        } catch (SQLException e) {
            return AuthResult.failure("Gagal konek database: " + e.getMessage());
        }
    }

    private boolean matchesPassword(String rawPassword, String storedPasswordHash) {
        if (storedPasswordHash == null || storedPasswordHash.isBlank()) {
            return false;
        }

        // Sementara fallback ke plain text sampai format hash final ditetapkan di docs/seeding.
        return rawPassword.equals(storedPasswordHash);
    }
}
