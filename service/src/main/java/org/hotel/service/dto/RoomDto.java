package org.hotel.service.dto;

import java.math.BigDecimal;
import java.util.Date;

public class RoomDto {
    /**
     * Уникальный ID комнаты.
     */
    private String id;

    /**
     * Номер комнаты.
     */
    private int number;

    /**
     * Цена за комнату.
     */
    private BigDecimal price;

    /**
     * Вместимость комнаты.
     */
    private int capacity;

    /**
     * Количество звезд у комнаты.
     */
    private int stars;

    /**
     * Статус комнаты.
     */
    private String status;

    /**
     * Дата освобождения комнаты.
     */
    private Date releasedIn;

    public RoomDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String idP) {
        this.id = idP;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int numberP) {
        this.number = numberP;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal priceP) {
        this.price = priceP;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacityP) {
        this.capacity = capacityP;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int starsP) {
        this.stars = starsP;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String statusP) {
        this.status = statusP;
    }

    public Date getReleasedIn() {
        return releasedIn;
    }

    public void setReleasedIn(Date releasedInP) {
        this.releasedIn = releasedInP;
    }
}
