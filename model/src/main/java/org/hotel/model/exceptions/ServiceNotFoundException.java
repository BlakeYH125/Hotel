package org.hotel.model.exceptions;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException() {
        super("Услуги с таким id нет");
    }
}
