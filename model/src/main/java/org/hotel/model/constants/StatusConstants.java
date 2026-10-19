package org.hotel.model.constants;

public final class StatusConstants {
    /**
     * Код статуса "успешно".
     */
    public static final int SUCCESS = 0;

    /**
     * Код статуса "на обслуживании".
     */
    public static final int IN_SERVICE = -1;

    /**
     * Код статуса "занята".
     */
    public static final int OCCUPIED = -2;

    /**
     * Код статуса "ошибка транзакции".
     */
    public static final int TRANSACTION_ERROR = -3;


    private StatusConstants() { }
}
