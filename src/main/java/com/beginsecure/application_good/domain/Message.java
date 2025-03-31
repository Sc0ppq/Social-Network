package com.beginsecure.application_good.domain;

import java.time.LocalDateTime;
import java.util.List;

public class Message extends Entity<Long> {
    private Long id;
    private User from;
    private List<User> to;
    private String message;
    private LocalDateTime date;
    private Message reply;

    public Message(Long id, User from, List<User> to, String message, LocalDateTime date) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.message = message;
        this.date = date;
        this.reply = null;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public User getFrom() {
        return this.from;
    }

    public List<User> getTo() {
        return this.to;
    }

    public String getMessage() {
        return this.message;
    }

    public LocalDateTime getDate() {
        return this.date;
    }
    public Message getReply() {
        return this.reply;
    }
    public void setReply(Message reply) {
        this.reply = reply;
    }


}

