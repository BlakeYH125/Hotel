package org.hotel.model.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hotel.model.interfaces.Priceable;
import org.hotel.model.enums.ServiceSection;

import java.math.BigDecimal;

@Entity
@Table(name = "services")
public final class Service implements Priceable {
    /**
     * Уникальный ID услуги.
     */
    @Id
    @Column(name = "serviceId")
    private String id;

    /**
     * Название услуги.
     */
    @Column(name = "name")
    private String name;

    /**
     * Цена услуги.
     */
    @Column(name = "price")
    private BigDecimal price;

    /**
     * Раздел услуги.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "serviceSection")
    private ServiceSection serviceSection;

    public Service(final String idP, final String nameP, final BigDecimal priceP, final ServiceSection serviceSectionP) {
        this.id = idP;
        this.name = nameP;
        this.price = priceP;
        this.serviceSection = serviceSectionP;
    }

    public Service() { }

    public String getName() {
        return name;
    }

    @Override
    public BigDecimal getPrice() {
        return price;
    }

    public ServiceSection getServiceSection() {
        return serviceSection;
    }

    public void setName(final String nameP) {
        this.name = nameP;
    }

    public void setPrice(final BigDecimal priceP) {
        this.price = priceP;
    }

    public String getId() {
        return id;
    }

    public void setId(final String idP) {
        this.id = idP;
    }

    public void setServiceSection(final ServiceSection serviceSectionP) {
        this.serviceSection = serviceSectionP;
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + ", стоимость: " + price;
    }
}
