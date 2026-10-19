package org.hotel.model.exceptions;

public class GuestAlreadyEvictedException extends RuntimeException {
    public GuestAlreadyEvictedException() {
        super("Гость с таким ID уже выселен");
    }
}
