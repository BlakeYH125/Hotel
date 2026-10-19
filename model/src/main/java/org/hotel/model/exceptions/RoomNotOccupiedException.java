package org.hotel.model.exceptions;

public class RoomNotOccupiedException extends RuntimeException {
    public RoomNotOccupiedException() {
        super("Комната никем не занята");
    }
}
