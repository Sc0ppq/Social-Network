package com.beginsecure.application_good.domain.validators;

public interface Validator<T> {
    void validate(T var1) throws ValidationException;
}