package org.hotel.model.enums;

public enum RoomStatus {
    /**
     * Статус свободный.
     */
    AVAILABLE("свободный"),

    /**
     * Статус занят.
     */
    OCCUPIED("занят"),

    /**
     * Статус обслуживается.
     */
    IN_SERVICE("обслуживается");

    /**
     * Название.
     */
    private final String label;

    RoomStatus(final String labelP) {
        this.label = labelP;
    }

    @Override
    public String toString() {
        return label;
    }
}
