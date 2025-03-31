package com.beginsecure.application_good.service;

public interface Observer<T> {
    void update(T data);
}
