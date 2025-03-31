package com.beginsecure.application_good.repo;
import com.beginsecure.application_good.domain.User;
import com.beginsecure.application_good.domain.validators.ValidationException;
import com.beginsecure.application_good.domain.validators.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDatabaseRepository implements Repository<Long, User> {
    private final Validator<User> validator;

    public UserDatabaseRepository(Validator<User> validator) {
        this.validator = validator;
    }

    @Override
    public Optional<User> findOne(Long id) {
        String query = "SELECT * FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                User user = new User(resultSet.getLong("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"));
                return Optional.of(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<User> findAll() {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User(resultSet.getLong("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"));
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    @Override
    public Optional<User> save(User entity) throws ValidationException {
        String query = "INSERT INTO users (first_name, last_name) VALUES (?, ?) RETURNING id";
        validator.validate(entity);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Long generatedId = resultSet.getLong("id");
                entity.setId(generatedId);
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<User> delete(Long id) {
        Optional<User> userToDelete = findOne(id);
        if (userToDelete.isEmpty()) {
            return Optional.empty();
        }

        String query = "DELETE FROM users WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                return userToDelete;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> update(User entity) throws ValidationException {
        String query = "UPDATE users SET first_name = ?, last_name = ? WHERE id = ?";
        validator.validate(entity);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, entity.getFirstName());
            statement.setString(2, entity.getLastName());
            statement.setLong(3, entity.getId());
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    public void sendFriendRequest(Long senderId, Long receiverId) {
        String query = "INSERT INTO friendship_requests (sender_id, receiver_id, request_date) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, senderId);
            statement.setLong(2, receiverId);
            statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public List<User> getPendingRequests(Long userId) {
        List<User> pendingRequests = new ArrayList<>();
        String query = "SELECT u.id, u.first_name, u.last_name FROM users u " +
                "JOIN friendship_requests fr ON u.id = fr.sender_id WHERE fr.receiver_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User user = new User(resultSet.getLong("id"),
                        resultSet.getString("first_name"),
                        resultSet.getString("last_name"));
                pendingRequests.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pendingRequests;
    }

    public Optional<String> getRequestDate(Long senderId, Long receiverId) {
        String query = "SELECT request_date FROM friendship_requests WHERE sender_id = ? AND receiver_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, senderId);
            statement.setLong(2, receiverId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                LocalDateTime requestDate = resultSet.getTimestamp("request_date").toLocalDateTime();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm");
                return Optional.of(requestDate.format(formatter));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean friendRequestExists(Long senderId, Long receiverId) {
        String query = "SELECT 1 FROM friendship_requests WHERE sender_id = ? AND receiver_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, senderId);
            statement.setLong(2, receiverId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


}

