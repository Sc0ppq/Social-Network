package com.beginsecure.application_good.repo;

import com.beginsecure.application_good.domain.Message;
import com.beginsecure.application_good.domain.User;
import com.beginsecure.application_good.repo.UserDatabaseRepository;
import com.beginsecure.application_good.domain.validators.ValidationException;
import com.beginsecure.application_good.domain.validators.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageDatabaseRepository implements Repository<Long, Message> {
    private final Validator<Message> validator;
    private final UserDatabaseRepository userRepository;

    public MessageDatabaseRepository(Validator<Message> validator, UserDatabaseRepository userRepository) {
        this.validator = validator;
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Message> findOne(Long id) {
        String query = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Message message = mapToMessage(resultSet);
                return Optional.of(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Message> findAll() {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Message message = mapToMessage(resultSet);
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    @Override
    public Optional<Message> save(Message entity) throws ValidationException {
        validator.validate(entity);

        String query = "INSERT INTO messages (from_user, to_users, message_text, date, reply_id) VALUES (?, ?, ?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, entity.getFrom().getId());
            statement.setString(2, toUserIdsString(entity.getTo()));
            statement.setString(3, entity.getMessage());
            statement.setTimestamp(4, Timestamp.valueOf(entity.getDate()));

            if (entity.getReply() != null) {
                statement.setLong(5, entity.getReply().getId());
            } else {
                statement.setNull(5, Types.BIGINT);
            }

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                entity.setId(resultSet.getLong("id"));
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }


    @Override
    public Optional<Message> delete(Long id) {
        Optional<Message> messageToDelete = findOne(id);
        if (messageToDelete.isEmpty()) {
            return Optional.empty();
        }

        String query = "DELETE FROM messages WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id);
            int affectedRows = statement.executeUpdate();
            if (affectedRows > 0) {
                return messageToDelete;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public Optional<Message> update(Message entity) throws ValidationException {
        validator.validate(entity);
        String query = "UPDATE messages SET reply_id = ? WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setObject(1, entity.getReply() != null ? entity.getReply().getId() : null, Types.BIGINT);
            statement.setLong(2, entity.getId());
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    private Message mapToMessage(ResultSet resultSet) throws SQLException {
        Long id = resultSet.getLong("id");

        Long fromUserId = resultSet.getLong("from_user");
        User from = userRepository.findOne(fromUserId)
                .orElseThrow(() -> new SQLException("User with ID " + fromUserId + " not found"));

        List<User> to = toUsersList(resultSet.getString("to_users"));

        String messageText = resultSet.getString("message_text");
        LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
        Long replyId = resultSet.getObject("reply_id", Long.class);

        Message message = new Message(id, from, to, messageText, date);

        if (replyId != null) {
            message.setReply(findOne(replyId).orElse(null));
        }
        return message;
    }

    private List<User> toUsersList(String toUsers) {
        List<User> users = new ArrayList<>();
        if (toUsers == null || toUsers.isBlank()) return users;

        String[] userIds = toUsers.split(",");
        for (String userId : userIds) {
            userRepository.findOne(Long.parseLong(userId))
                    .ifPresent(users::add);
        }
        return users;
    }

    private String toUserIdsString(List<User> users) {
        StringBuilder userIds = new StringBuilder();
        for (User user : users) {
            if (userIds.length() > 0) {
                userIds.append(",");
            }
            userIds.append(user.getId());
        }
        return userIds.toString();
    }
}

