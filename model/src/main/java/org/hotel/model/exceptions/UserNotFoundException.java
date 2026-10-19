package org.hotel.model.exceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("Аккаунта с таким именем пользователя не существует");
    }
}
