package org.hotel.model.exceptions;

public class WrongPasswordException extends RuntimeException {
    public WrongPasswordException() {
        super("Неверный пароль");
    }
}
