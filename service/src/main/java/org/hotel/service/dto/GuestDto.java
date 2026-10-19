package org.hotel.service.dto;

import java.util.Date;

public class GuestDto {
    /**
     * Уникальный id клиента.
     */
    private String id;

    /**
     * Полное имя.
     */
    private String fullName;

    /**
     * Возраст.
     */
    private int age;

    /**
     * ID арендуемой комнаты.
     */
    private String rentRoomId;

    /**
     * Дата заезда.
     */
    private Date arriveDate;

    /**
     * Дата выезда.
     */
    private Date departureDate;

    /**
     * Статус гостя.
     */
    private String status;

    public GuestDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String idP) {
        this.id = idP;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullNameP) {
        this.fullName = fullNameP;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int ageP) {
        this.age = ageP;
    }

    public String getRentRoomId() {
        return rentRoomId;
    }

    public void setRentRoomId(String rentRoomIdP) {
        this.rentRoomId = rentRoomIdP;
    }

    public Date getArriveDate() {
        return arriveDate;
    }

    public void setArriveDate(Date arriveDateP) {
        this.arriveDate = arriveDateP;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(Date departureDateP) {
        this.departureDate = departureDateP;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String statusP) {
        this.status = statusP;
    }
}
