package org.hotel.model.exceptions;

public class RoomAlreadyExistsException extends RuntimeException {
    public RoomAlreadyExistsException() {
        super("Комната с таким id уже есть");
    }
}
