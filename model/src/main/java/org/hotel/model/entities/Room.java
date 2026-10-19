package org.hotel.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import org.hotel.model.interfaces.Priceable;
import org.hotel.model.enums.RoomStatus;

import java.math.BigDecimal;
import java.util.Date;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "rooms")
public final class Room implements Priceable {
    /**
     * Уникальный ID комнаты.
     */
    @Id
    @Column(name = "roomId")
    private String id;

    /**
     * Номер комнаты.
     */
    @Column(name = "number")
    private int number;

    /**
     * Цена за комнату.
     */
    @Column(name = "price")
    private BigDecimal price;

    /**
     * Вместимость комнаты.
     */
    @Column(name = "capacity")
    private int capacity;

    /**
     * Количество звезд у комнаты.
     */
    @Column(name = "stars")
    private int stars;

    /**
     * Статус комнаты.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private RoomStatus roomStatus;

    /**
     * Дата освобождения комнаты.
     */
    @Column(name = "releasedIn")
    @Temporal(TemporalType.DATE)
    private Date releasedIn;

    public Room(final String idP, final int numberP, final BigDecimal priceP, final RoomStatus roomStatusP, final int capacityP, final int starsP) {
        this.id = idP;
        this.number = numberP;
        this.price = priceP;
        this.capacity = capacityP;
        this.stars = starsP;
        this.roomStatus = roomStatusP;
    }

    public Room() { }

    public int getNumber() {
        return number;
    }

    public void setNumber(final int numberP) {
        this.number = numberP;
    }

    public void setCapacity(final int capacityP) {
        this.capacity = capacityP;
    }

    public void setStars(final int starsP) {
        this.stars = starsP;
    }

    public String getId() {
        return id;
    }

    public void setId(final String idP) {
        this.id = idP;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal priceP) {
        this.price = priceP;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getStars() {
        return stars;
    }

    public RoomStatus getStatus() {
        return roomStatus;
    }

    public void setStatus(final RoomStatus roomStatusP) {
        this.roomStatus = roomStatusP;
    }

    public Date getReleasedIn() {
        return releasedIn;
    }

    public void setReleasedIn(final Date releasedInP) {
        this.releasedIn = releasedInP;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        if (releasedIn == null) {
            return "[" + id  + "] номер " + number + ", стоимость: " + price + ", вместимость: " + capacity + ", звезды: "
                    + stars + ", " + roomStatus.toString();
        }
        return "[" + id  + "] номер " + number + ", стоимость: " + price + ", вместимость: " + capacity + ", звезды: "
                + stars + ", " + roomStatus.toString() + ", освободится " + sdf.format(releasedIn);
    }
}
