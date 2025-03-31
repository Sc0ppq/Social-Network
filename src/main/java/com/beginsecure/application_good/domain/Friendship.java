package com.beginsecure.application_good.domain;

import java.time.LocalDateTime;

public class Friendship extends Entity<Tuple<Long, Long>> {
    private LocalDateTime date;

    public Friendship(Tuple<Long, Long> id, LocalDateTime date) {
        super.setId(id);
        this.date = date;
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    @Override
    public String toString() {
        Tuple<Long, Long> ids = this.getId();
        return "Friendship{" +
                "user1Id=" + ids.getLeft() +
                ", user2Id=" + ids.getRight() +
                ", date=" + date +
                '}';
    }
}
