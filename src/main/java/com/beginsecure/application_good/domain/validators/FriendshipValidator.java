package com.beginsecure.application_good.domain.validators;
import com.beginsecure.application_good.domain.Friendship;
import com.beginsecure.application_good.domain.Tuple;



public class FriendshipValidator implements Validator<Friendship> {
    @Override
    public void validate(Friendship friendship) throws ValidationException {
        if (friendship == null) {
            throw new ValidationException("Friendship cannot be null");
        }

        if (friendship.getId() == null) {
            throw new ValidationException("Friendship ID cannot be null");
        }

        Tuple<Long, Long> ids = friendship.getId();
        if (ids.getLeft() == null || ids.getRight() == null) {
            throw new ValidationException("User IDs in friendship cannot be null");
        }

        if (ids.getLeft().equals(ids.getRight())) {
            throw new ValidationException("A user cannot befriend themselves");
        }
    }
}

