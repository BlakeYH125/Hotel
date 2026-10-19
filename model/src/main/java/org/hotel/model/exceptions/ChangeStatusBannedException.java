package org.hotel.model.exceptions;

public class ChangeStatusBannedException extends RuntimeException {
    public ChangeStatusBannedException() {
        super("Включен запрет на изменение статуса комнаты");
    }
}
