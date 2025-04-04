package com.beginsecure.application_good.utils.events;
import com.beginsecure.application_good.domain.User;

public class UserEntityChangeEvent implements Event {
    private ChangeEventType type;
    private User data, oldData;

    public UserEntityChangeEvent(ChangeEventType type, User data) {
        this.type = type;
        this.data = data;
    }
    public UserEntityChangeEvent(ChangeEventType type, User data, User oldData) {
        this.type = type;
        this.data = data;
        this.oldData=oldData;
    }

    public ChangeEventType getType() {
        return type;
    }

    public User getData() {
        return data;
    }

    public User getOldData() {
        return oldData;
    }
}