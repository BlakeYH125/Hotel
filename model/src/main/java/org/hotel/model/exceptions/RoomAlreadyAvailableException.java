package org.hotel.model.exceptions;

public class RoomAlreadyAvailableException extends RuntimeException {
    public RoomAlreadyAvailableException() {
        super("Комната уже свободна");
    }
}
