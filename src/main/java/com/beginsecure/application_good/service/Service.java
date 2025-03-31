package com.beginsecure.application_good.service;

import com.beginsecure.application_good.domain.Friendship;
import com.beginsecure.application_good.domain.Message;
import com.beginsecure.application_good.domain.User;
import com.beginsecure.application_good.domain.Tuple;
import com.beginsecure.application_good.domain.validators.ValidationException;
import com.beginsecure.application_good.repo.Repository;
import com.beginsecure.application_good.repo.UserDatabaseRepository;
import com.beginsecure.application_good.repo.FriendshipDatabaseRepository;
import com.beginsecure.application_good.repo.MessageDatabaseRepository;
import com.beginsecure.application_good.domain.Pageable;
import com.beginsecure.application_good.domain.Page;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


public class Service {
    private final Repository<Long, User> userRepository;
    private final Repository<Tuple<Long, Long>, Friendship> friendshipRepository;
    private final Repository<Long, Message> messageRepository;
    private long nextUserId = 1;
    private long nextMessageId = 1;

    public Service(Repository<Long, User> userRepository, Repository<Tuple<Long, Long>, Friendship> friendshipRepository, Repository<Long, Message> messageRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.messageRepository = messageRepository;
    }

    public void addUser(User user) {
        try {
            user.setId(nextUserId++);
            userRepository.save(user);
        } catch (ValidationException e) {
            throw new ServiceException("Failed to add user: " + e.getMessage(), e);
        }
    }

    public void removeUser(Long userId) {
        userRepository.delete(userId).orElseThrow(() -> new ServiceException("User with ID " + userId + " not found"));
    }

    public void sendFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new ServiceException("A user cannot send a friend request to themselves");
        }

        Optional<User> sender = userRepository.findOne(senderId);
        Optional<User> receiver = userRepository.findOne(receiverId);

        if (sender.isEmpty() || receiver.isEmpty()) {
            throw new ServiceException("One or both users not found");
        }

        if (isFriendRequestAlreadySent(senderId, receiverId)) {
            throw new ServiceException("A friend request has already been sent to this user");
        }

        System.out.println("Sending friend request from " + senderId + " to " + receiverId);

        ((UserDatabaseRepository) userRepository).sendFriendRequest(senderId, receiverId);
    }


    public List<User> getPendingRequests(Long userId) {
        return ((UserDatabaseRepository) userRepository).getPendingRequests(userId);
    }

    public List<String> getFormattedPendingRequests(Long userId) {
        List<User> pendingRequests = getPendingRequests(userId);
        List<String> formattedRequests = new ArrayList<>();

        for (User user : pendingRequests) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            String requestDate = ((UserDatabaseRepository) userRepository).getRequestDate(user.getId(), userId)
                    .orElse("Unknown Date");
            formattedRequests.add(fullName + ", " + requestDate);
        }
        return formattedRequests;
    }



    public void acceptFriendRequest(Long senderId, Long receiverId) {
        if (senderId.equals(receiverId)) {
            throw new ServiceException("A user cannot accept a friend request from themselves");
        }

        Optional<User> sender = userRepository.findOne(senderId);
        Optional<User> receiver = userRepository.findOne(receiverId);
        if (sender.isEmpty() || receiver.isEmpty()) {
            throw new ServiceException("One or both users not found");
        }

        ((FriendshipDatabaseRepository) friendshipRepository).acceptFriendRequest(senderId, receiverId);
    }

    public void declineFriendRequest(Long senderId, Long receiverId) {
        ((FriendshipDatabaseRepository) friendshipRepository).declineFriendRequest(senderId, receiverId);
    }

    public void addFriendship(Long userId1, Long userId2) {
        try {
            if (userId1.equals(userId2)) {
                throw new ServiceException("A user cannot befriend themselves");
            }

            Optional<User> user1 = userRepository.findOne(userId1);
            Optional<User> user2 = userRepository.findOne(userId2);
            if (user1.isEmpty() || user2.isEmpty()) {
                throw new ServiceException("One or both users not found");
            }

            Friendship friendship = new Friendship(new Tuple<>(userId1, userId2), LocalDateTime.now());
            friendshipRepository.save(friendship);
        } catch (ValidationException e) {
            throw new ServiceException("Failed to add friendship: " + e.getMessage(), e);
        }
    }

    public void removeFriendship(Long userId1, Long userId2) {
        Tuple<Long, Long> friendshipId = createSortedTuple(userId1, userId2);
        Optional<Friendship> deletedFriendship = friendshipRepository.delete(friendshipId);

        if (deletedFriendship.isPresent()) {
            throw new ServiceException("Friendship between users " + userId1 + " and " + userId2 + " not found");
        }
    }

    public List<User> findAllUsers() {
        List<User> users = new ArrayList<>();
        userRepository.findAll().forEach(users::add);
        return users;
    }

    public Optional<User> findUserByName(String firstName, String lastName) {
        Iterable<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getFirstName().equals(firstName) && user.getLastName().equals(lastName)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }



    public List<User> getFriends(Long userId) {
        List<User> friends = new ArrayList<>();
        Iterable<Friendship> friendships = friendshipRepository.findAll();

        for (Friendship friendship : friendships) {
            Long user1 = friendship.getId().getLeft();
            Long user2 = friendship.getId().getRight();

            if (user1.equals(userId)) {
                userRepository.findOne(user2).ifPresent(friends::add);
            } else if (user2.equals(userId)) {
                userRepository.findOne(user1).ifPresent(friends::add);
            }
        }

        return friends;
    }

    private Tuple<Long, Long> createSortedTuple(Long id1, Long id2) {
        return id1 < id2 ? new Tuple<>(id1, id2) : new Tuple<>(id2, id1);
    }

    private boolean isFriendRequestAlreadySent(Long senderId, Long receiverId) {
        return ((UserDatabaseRepository) userRepository).friendRequestExists(senderId, receiverId);
    }

    public void sendMessage(Long senderId, List<Long> recipientIds, String messageText) {
        Optional<User> sender = userRepository.findOne(senderId);
        if (sender.isEmpty()) {
            throw new ServiceException("Sender not found with ID " + senderId);
        }

        List<User> recipients = new ArrayList<>();
        for (Long recipientId : recipientIds) {
            Optional<User> recipient = userRepository.findOne(recipientId);
            if (recipient.isEmpty()) {
                throw new ServiceException("Recipient not found with ID " + recipientId);
            }
            recipients.add(recipient.get());
        }

        Message lastMessage = findLastMessageBetween(senderId, recipientIds);

        Message message = new Message(nextMessageId++, sender.get(), recipients, messageText, LocalDateTime.now());

        if (lastMessage != null) {
            message.setReply(lastMessage);
        }

        try {
            messageRepository.save(message);
            notifyObservers(message);
        } catch (ValidationException e) {
            throw new ServiceException("Failed to send message: " + e.getMessage(), e);
        }
    }

    private Message findLastMessageBetween(Long senderId, List<Long> recipientIds) {
        List<Message> conversation = new ArrayList<>();
        for (Message message : messageRepository.findAll()) {
            boolean isRelevant = (message.getFrom().getId().equals(senderId) &&
                    message.getTo().stream().anyMatch(user -> recipientIds.contains(user.getId()))) ||
                    (recipientIds.contains(message.getFrom().getId()) &&
                            message.getTo().stream().anyMatch(user -> user.getId().equals(senderId)));

            if (isRelevant) {
                conversation.add(message);
            }
        }

        conversation.sort((m1, m2) -> m2.getDate().compareTo(m1.getDate()));

        return conversation.isEmpty() ? null : conversation.get(0);
    }

    public List<Message> getConversationBetweenUsers(Long userId1, Long userId2) {
        List<Message> conversation = new ArrayList<>();
        for (Message message : messageRepository.findAll()) {
            boolean sentByUser1 = message.getFrom().getId().equals(userId1) &&
                    message.getTo().stream().anyMatch(user -> user.getId().equals(userId2));
            boolean sentByUser2 = message.getFrom().getId().equals(userId2) &&
                    message.getTo().stream().anyMatch(user -> user.getId().equals(userId1));
            if (sentByUser1 || sentByUser2) {
                conversation.add(message);
            }
        }
        conversation.sort(Comparator.comparing(Message::getDate));
        return conversation;
    }

    private final List<Observer<Message>> observers = new ArrayList<>();

    public void addObserver(Observer<Message> observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer<Message> observer) {
        observers.remove(observer);
    }

    private void notifyObservers(Message message) {
        for (Observer<Message> observer : observers) {
            observer.update(message);
        }
    }

    public Page<User> getFriendsOnPage(Long userId, Pageable pageable) {
        Page<Friendship> friendshipPage = ((FriendshipDatabaseRepository) friendshipRepository).findFriendsOnPage(userId, pageable);

        List<User> friends = new ArrayList<>();
        for (Friendship friendship : friendshipPage.getElementsOnPage()) {
            Long friendId = (friendship.getId().getLeft().equals(userId))
                    ? friendship.getId().getRight()
                    : friendship.getId().getLeft();

            userRepository.findOne(friendId).ifPresent(friends::add);
        }
        return new Page<>(friends, friendshipPage.getTotalNumberOfElements());
    }


}


