package org.hotel.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Date;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "guestUsedServices")
public final class UsedService {
    /**
     * Уникальный ID использованной услуги.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "usedServiceId")
    private Long usedServiceId;

    /**
     * Уникальный ID гостя.
     */
    @Column(name = "guestId")
    private String guestId;

    /**
     * Уникальный ID услуги.
     */
    @Column(name = "serviceId")
    private String serviceId;

    /**
     * Стоимость использованной услуги.
     */
    @Column(name = "price")
    private BigDecimal price;

    /**
     * Дата использования услуги.
     */
    @Column(name = "date")
    @Temporal(TemporalType.DATE)
    private Date date;

    public UsedService(final String serviceIdP, final String guestIdP, final BigDecimal priceP, final Date dateP) {
        this.serviceId = serviceIdP;
        this.guestId = guestIdP;
        this.price = priceP;
        this.date = dateP;
    }

    public UsedService() { }

    public Long getUsedServiceId() {
        return usedServiceId;
    }

    public void setUsedServiceId(final Long usedServiceIdP) {
        this.usedServiceId = usedServiceIdP;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(final String guestIdP) {
        this.guestId = guestIdP;
    }

    public String getId() {
        return serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(final String serviceIdP) {
        this.serviceId = serviceIdP;
    }

    public void setPrice(final BigDecimal priceP) {
        this.price = priceP;
    }

    public void setDate(final Date dateP) {
        this.date = dateP;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return "Услуга [" + getId() + "], " + "стоимость " + getPrice() + ", использована " + sdf.format(date);
    }
}
