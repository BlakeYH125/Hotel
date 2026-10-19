package org.hotel.model.exceptions;

public class WrongSortTypeException extends RuntimeException {
    public WrongSortTypeException() {
        super("Неверный тип сортировки");
    }
}
