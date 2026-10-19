package org.hotel.model.exceptions;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super("Аккаунт с таким именем пользователя уже существует");
    }
}
