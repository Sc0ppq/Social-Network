package com.beginsecure.application_good.domain.validators;

import com.beginsecure.application_good.domain.Message;

import java.util.List;

public class MessageValidator implements Validator<Message> {

    @Override
    public void validate(Message message) throws ValidationException {
        StringBuilder errors = new StringBuilder();
        if (message.getFrom() == null) {
            errors.append("Message must have a sender (from)!\n");
        }
        List<?> toList = message.getTo();
        if (toList == null || toList.isEmpty()) {
            errors.append("Message must have at least one recipient (to)!\n");
        }

        if (message.getMessage() == null || message.getMessage().trim().isEmpty()) {
            errors.append("Message content cannot be empty!\n");
        }
        if (message.getDate() == null) {
            errors.append("Message must have a valid date!\n");
        }

        if (message.getReply() != null && message.getReply().getId() == null) {
            errors.append("Reply must refer to a valid existing message!\n");
        }
        
        if (errors.length() > 0) {
            throw new ValidationException(errors.toString());
        }
    }
}

