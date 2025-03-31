package com.beginsecure.application_good.repo;
import com.beginsecure.application_good.domain.Friendship;
import com.beginsecure.application_good.domain.Page;
import com.beginsecure.application_good.domain.Pageable;
import com.beginsecure.application_good.domain.Tuple;
import com.beginsecure.application_good.domain.validators.ValidationException;
import com.beginsecure.application_good.domain.validators.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FriendshipDatabaseRepository implements Repository<Tuple<Long, Long>, Friendship> {
    private final Validator<Friendship> validator;

    public FriendshipDatabaseRepository(Validator<Friendship> validator) {
        this.validator = validator;
    }

    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        String query = "SELECT * FROM friendships WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id.getLeft());
            statement.setLong(2, id.getRight());
            statement.setLong(3, id.getRight());
            statement.setLong(4, id.getLeft());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Friendship friendship = new Friendship(
                        new Tuple<>(resultSet.getLong("user_id1"), resultSet.getLong("user_id2")),
                        resultSet.getTimestamp("friendship_date").toLocalDateTime()
                );
                return Optional.of(friendship);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }


    @Override
    public Iterable<Friendship> findAll() {
        List<Friendship> friendships = new ArrayList<>();
        String query = "SELECT * FROM friendships";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Tuple<Long, Long> id = new Tuple<>(resultSet.getLong("user_id1"), resultSet.getLong("user_id2"));
                Friendship friendship = new Friendship(id, resultSet.getTimestamp("friendship_date").toLocalDateTime());
                friendships.add(friendship);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendships;
    }

    @Override
    public Optional<Friendship> save(Friendship entity) throws ValidationException {
        String query = "INSERT INTO friendships (user_id1, user_id2, friendship_date) VALUES (?, ?, ?)";
        validator.validate(entity);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, entity.getId().getLeft());
            statement.setLong(2, entity.getId().getRight());
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        Optional<Friendship> friendshipToDelete = findOne(id);
        if (friendshipToDelete.isEmpty()) {
            return Optional.of(new Friendship(id, null));
        }

        String query = "DELETE FROM friendships WHERE (user_id1 = ? AND user_id2 = ?) OR (user_id1 = ? AND user_id2 = ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, id.getLeft());
            statement.setLong(2, id.getRight());
            statement.setLong(3, id.getRight());
            statement.setLong(4, id.getLeft());
            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                return Optional.empty();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(new Friendship(id, null));
    }

    @Override
    public Optional<Friendship> update(Friendship entity) throws ValidationException {
        String query = "UPDATE friendships SET friendship_date = ? WHERE user_id1 = ? AND user_id2 = ?";
        validator.validate(entity);
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setTimestamp(1, Timestamp.valueOf(entity.getDate()));
            statement.setLong(2, entity.getId().getLeft());
            statement.setLong(3, entity.getId().getRight());
            int affectedRows = statement.executeUpdate();
            return affectedRows > 0 ? Optional.empty() : Optional.of(entity);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.of(entity);
    }

    public List<Friendship> getFriends(Long userId) {
        List<Friendship> friendsList = new ArrayList<>();
        String query = "SELECT * FROM friendships WHERE user_id1 = ? OR user_id2 = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Tuple<Long, Long> id = new Tuple<>(resultSet.getLong("user_id1"), resultSet.getLong("user_id2"));
                LocalDateTime friendshipDate = resultSet.getTimestamp("friendship_date").toLocalDateTime();
                Friendship friendship = new Friendship(id, friendshipDate);
                friendsList.add(friendship);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friendsList;
    }

    public void acceptFriendRequest(Long senderId, Long receiverId) {
        String deleteRequestQuery = "DELETE FROM friendship_requests WHERE sender_id = ? AND receiver_id = ?";
        String insertFriendshipQuery = "INSERT INTO friendships (user_id1, user_id2, friendship_date) VALUES (?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            try (PreparedStatement deleteStatement = connection.prepareStatement(deleteRequestQuery)) {
                deleteStatement.setLong(1, senderId);
                deleteStatement.setLong(2, receiverId);
                deleteStatement.executeUpdate();
            }
            try (PreparedStatement insertStatement = connection.prepareStatement(insertFriendshipQuery)) {
                insertStatement.setLong(1, senderId);
                insertStatement.setLong(2, receiverId);
                insertStatement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                insertStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void declineFriendRequest(Long senderId, Long receiverId) {
        String query = "DELETE FROM friendship_requests WHERE sender_id = ? AND receiver_id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, senderId);
            statement.setLong(2, receiverId);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Page<Friendship> findFriendsOnPage(Long userId, Pageable pageable) {
        List<Friendship> friendsOnPage = new ArrayList<>();
        String query = "SELECT * FROM friendships WHERE user_id1 = ? OR user_id2 = ? LIMIT ? OFFSET ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            statement.setInt(3, pageable.getPageSize());
            statement.setInt(4, pageable.getPageNumber() * pageable.getPageSize());
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Tuple<Long, Long> id = new Tuple<>(resultSet.getLong("user_id1"), resultSet.getLong("user_id2"));
                LocalDateTime friendshipDate = resultSet.getTimestamp("friendship_date").toLocalDateTime();
                friendsOnPage.add(new Friendship(id, friendshipDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        int totalFriends = countFriends(userId);
        return new Page<>(friendsOnPage, totalFriends);
    }

    private int countFriends(Long userId) {
        String query = "SELECT COUNT(*) AS total FROM friendships WHERE user_id1 = ? OR user_id2 = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setLong(2, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}


