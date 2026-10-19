package org.hotel.model.exceptions;

public class RoomAlreadyInServiceException extends RuntimeException {
    public RoomAlreadyInServiceException() {
        super("Комната уже обслуживается");
    }
}
