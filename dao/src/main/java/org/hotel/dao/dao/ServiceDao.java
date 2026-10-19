package org.hotel.dao.dao;

import org.hibernate.SessionFactory;
import org.hotel.model.entities.Service;
import org.hotel.dao.repository.ServiceRepository;
import org.springframework.stereotype.Repository;


import java.math.BigDecimal;
import java.util.List;

@Repository
public class ServiceDao implements ServiceRepository {
    /**
     * Здесь будет храниться открытая сессия с БД.
     */
    private final SessionFactory sessionFactory;

    public ServiceDao(final SessionFactory sessionFactoryP) {
        this.sessionFactory = sessionFactoryP;
    }

    @Override
    public void save(final Service serviceP) {
        sessionFactory.getCurrentSession().merge(serviceP);
    }

    @Override
    public List<Service> findAll() {
        return sessionFactory.getCurrentSession().createQuery("FROM Service", Service.class).list();
    }

    @Override
    public void setNewServicePrice(final Service serviceP, final BigDecimal priceP) {
        serviceP.setPrice(priceP);
        sessionFactory.getCurrentSession().merge(serviceP);
    }

    @Override
    public Service getService(final String idP) {
        return sessionFactory.getCurrentSession().get(Service.class, idP);
    }
}
