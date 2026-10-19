package org.hotel.service.dto;

import java.math.BigDecimal;
import java.util.Date;

public class UsedServiceDto {
    /**
     * Уникальный ID использованной услуги.
     */
    private Long usedServiceId;

    /**
     * Уникальный ID гостя.
     */
    private String guestId;

    /**
     * Уникальный ID услуги.
     */
    private String serviceId;

    /**
     * Стоимость использованной услуги.
     */
    private BigDecimal price;

    /**
     * Дата использования услуги.
     */
    private Date date;

    public UsedServiceDto() {
    }

    public Long getUsedServiceId() {
        return usedServiceId;
    }

    public void setUsedServiceId(Long usedServiceIdP) {
        this.usedServiceId = usedServiceIdP;
    }

    public String getGuestId() {
        return guestId;
    }

    public void setGuestId(String guestIdP) {
        this.guestId = guestIdP;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceIdP) {
        this.serviceId = serviceIdP;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal priceP) {
        this.price = priceP;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date dateP) {
        this.date = dateP;
    }
}
