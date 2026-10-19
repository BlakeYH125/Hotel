package org.hotel.service.dto;

import java.math.BigDecimal;

public class ServiceDto {
    /**
     * Уникальный ID услуги.
     */
    private String id;

    /**
     * Название услуги.
     */
    private String name;

    /**
     * Цена услуги.
     */
    private BigDecimal price;

    /**
     * Раздел услуги.
     */
    private String serviceSection;

    public ServiceDto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String idP) {
        this.id = idP;
    }

    public String getName() {
        return name;
    }

    public void setName(String nameP) {
        this.name = nameP;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal priceP) {
        this.price = priceP;
    }

    public String getServiceSection() {
        return serviceSection;
    }

    public void setServiceSection(String serviceSectionP) {
        this.serviceSection = serviceSectionP;
    }
}
