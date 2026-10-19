package org.hotel.model.exceptions;

public class GuestAlreadyExistsException extends RuntimeException {
    public GuestAlreadyExistsException() {
        super("Гость с таким ID уже существует");
    }
}
