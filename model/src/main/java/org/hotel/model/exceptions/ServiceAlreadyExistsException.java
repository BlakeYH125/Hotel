package org.hotel.model.exceptions;

public class ServiceAlreadyExistsException extends RuntimeException {
    public ServiceAlreadyExistsException() {
        super("Услуга с таким id уже есть");
    }
}
