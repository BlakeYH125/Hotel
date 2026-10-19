package org.hotel.model.exceptions;

public class GuestNotFoundException extends RuntimeException {
    public GuestNotFoundException() {
        super("Гостя с таким id нет");
    }
}
