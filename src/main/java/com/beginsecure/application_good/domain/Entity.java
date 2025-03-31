package com.beginsecure.application_good.domain;

public class Entity<ID> {
    private ID id;

    public Entity() {
    }

    public ID getId() {
        return this.id;
    }

    public void setId(ID id) {
        this.id = id;
    }
}


