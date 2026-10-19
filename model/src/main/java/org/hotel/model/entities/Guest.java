package org.hotel.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import org.hotel.model.enums.GuestStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "guests")
public final class Guest {
    /**
     * Уникальный id клиента.
     */
    @Id
    @Column(name = "guestId")
    private String id;

    /**
     * Полное имя.
     */
    @Column(name = "fullName")
    private String fullName;

    /**
     * Возраст.
     */
    @Column(name = "age")
    private int age;

    /**
     * ID арендуемой комнаты.
     */
    @Column(name = "rentRoomId")
    private String rentRoomId;

    /**
     * Дата заезда.
     */
    @Column(name = "arriveDate")
    @Temporal(TemporalType.DATE)
    private Date arriveDate;

    /**
     * Дата выезда.
     */
    @Column(name = "departureDate")
    @Temporal(TemporalType.DATE)
    private Date departureDate;

    /**
     * Статус гостя.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private GuestStatus status;

    /**
     * Аккаунт гостя.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "pricePerDay", precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    public Guest(final String paramId, final String paramFullName, final int paramAge) {
        this.id = paramId;
        this.fullName = paramFullName;
        this.age = paramAge;
        this.status = GuestStatus.SETTLED;
    }

    public Guest() { }

    public void setFullName(final String paramFullName) {
        this.fullName = paramFullName;
    }

    public void setAge(final int paramAge) {
        this.age = paramAge;
    }

    public GuestStatus getStatus() {
        return status;
    }

    public void setStatus(final GuestStatus paramStatus) {
        this.status = paramStatus;
    }

    public String getFullName() {
        return fullName;
    }

    public int getAge() {
        return age;
    }

    public void setId(final String paramId) {
        this.id = paramId;
    }

    public Date getArriveDate() {
        return arriveDate;
    }

    public void setArriveDate(final Date paramArriveDate) {
        this.arriveDate = paramArriveDate;
    }

    public Date getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(final Date paramDepartureDate) {
        this.departureDate = paramDepartureDate;
    }

    public void setRentRoomId(final String paramRentRoomId) {
        this.rentRoomId = paramRentRoomId;
    }

    public String getId() {
        return id;
    }

    public String getRentRoomId() {
        return rentRoomId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User userP) {
        this.user = userP;
    }

    public BigDecimal getPricePerDay() {
        return pricePerDay;
    }

    public void setPricePerDay(BigDecimal pricePerDay) {
        this.pricePerDay = pricePerDay;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        if (rentRoomId != null && arriveDate != null) {
            return String.format("[" + id + "] " + fullName + " " + age + " лет, комната " + rentRoomId + ", " + sdf.format(arriveDate) + "-" + sdf.format(departureDate) + ", " + status);
        } else if (arriveDate == null) {
            return String.format("[" + id + "] " + fullName + " " + age + " лет, пока не заселен.");
        } else {
            return String.format("[" + id + "] " + fullName + " " + age + " лет, " + sdf.format(arriveDate) + "-" + sdf.format(departureDate) + ", " + status);
        }
    }
}
