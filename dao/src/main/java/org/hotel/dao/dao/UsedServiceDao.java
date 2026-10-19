package org.hotel.dao.dao;

import org.hibernate.SessionFactory;
import org.hotel.model.entities.Guest;
import org.hotel.model.entities.UsedService;
import org.hotel.dao.repository.UsedServiceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UsedServiceDao implements UsedServiceRepository {
    /**
     * Здесь будет храниться открытая сессия с БД.
     */
    private final SessionFactory sessionFactory;

    public UsedServiceDao(final SessionFactory sessionFactoryP) {
        this.sessionFactory = sessionFactoryP;
    }

    @Override
    public void save(final UsedService usedServiceP) {
        sessionFactory.getCurrentSession().merge(usedServiceP);
    }

    @Override
    public List<UsedService> findServicesUsedByGuest(final Guest guestP) {
        String hql = "FROM UsedService WHERE guestId = :guestIdP";
        return sessionFactory.getCurrentSession().createQuery(hql, UsedService.class)
                .setParameter("guestIdP", guestP.getId()).list();
    }
}
