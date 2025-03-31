package com.beginsecure.application_good.domain.validators;
import com.beginsecure.application_good.domain.User;

public class UserValidator implements Validator<User> {
    public UserValidator() {
    }

    public void validate(User entity) throws ValidationException {
        if (entity.getFirstName().equals("")) {
            throw new ValidationException("Utilizatorul nu este valid");
        }
    }
}
