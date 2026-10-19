package org.hotel.model.exceptions;

public class RoomAlreadyOccupiedException extends RuntimeException {
    public RoomAlreadyOccupiedException() {
        super("Комната уже занята");
    }
}
