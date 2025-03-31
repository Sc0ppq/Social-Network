package com.beginsecure.application_good.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User extends Entity<Long> {
    private Long id;
    private String firstName;
    private String lastName;
    private List<Friendship> pendingFriendships = new ArrayList<>(); // New list for pending friendships

    public User(Long id, String firstName, String lastName) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<Friendship> getPendingFriendships() {
        return pendingFriendships;
    }

    public void addPendingFriendship(Friendship friendship) {
        this.pendingFriendships.add(friendship);
    }

    public void removePendingFriendship(Friendship friendship) {
        this.pendingFriendships.remove(friendship);
    }

    @Override
    public String toString() {
        return "User{id=" + id + ", firstName='" + firstName + "', lastName='" + lastName + "', pendingFriendships=" + pendingFriendships + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) &&
                Objects.equals(firstName, user.firstName) &&
                Objects.equals(lastName, user.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName);
    }
}


