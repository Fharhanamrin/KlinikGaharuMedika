package com.release.klinikgaharumedika.repository;

import com.release.klinikgaharumedika.config.DatabaseConnection;
import com.release.klinikgaharumedika.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserRepository {

    private static final String FIND_BY_USERNAME_SQL = """
            SELECT id, username, password_hash, nama_lengkap, role, is_active
            FROM users
            WHERE username = ?
            LIMIT 1
            """;

    public Optional<User> findByUsername(String username) throws SQLException {
        try (
                Connection connection = DatabaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME_SQL)
        ) {
            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                return Optional.of(mapUser(resultSet));
            }
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("nama_lengkap"),
                resultSet.getString("role"),
                resultSet.getBoolean("is_active")
        );
    }
}
