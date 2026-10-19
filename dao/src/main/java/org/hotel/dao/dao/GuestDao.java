package org.hotel.dao.dao;

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import org.hotel.model.entities.Guest;
import org.hotel.dao.repository.GuestRepository;
import org.hotel.model.enums.GuestStatus;
import org.hotel.model.entities.Room;

import java.util.List;

@Repository
public class GuestDao implements GuestRepository {
    /**
     * Здесь будет храниться открытая сессия с БД.
     */
    private final SessionFactory sessionFactory;

    public GuestDao(final SessionFactory sessionFactoryP) {
        this.sessionFactory = sessionFactoryP;
    }

    @Override
    public void save(final Guest guestP) {
        sessionFactory.getCurrentSession().merge(guestP);
    }

    @Override
    public List<Guest> findAll() {
        return sessionFactory.getCurrentSession().createQuery("FROM Guest", Guest.class).list();
    }

    @Override
    public List<Guest> findCurrentGuestsInRoom(final Room roomP) {
        String hql = "FROM Guest WHERE status=:statusP AND rentRoomId=:rentRoomIdP";
        return sessionFactory.getCurrentSession().createQuery(hql, Guest.class)
                .setParameter("statusP", GuestStatus.SETTLED).setParameter("rentRoomIdP", roomP.getId()).list();
    }

    @Override
    public List<Guest> findCurrentGuestsInHotel() {
        String hql = "FROM Guest where status=:statusP";
        return sessionFactory.getCurrentSession().createQuery(hql, Guest.class).setParameter("statusP", GuestStatus.SETTLED).list();
    }

    @Override
    public Guest getGuest(final String idP) {
        return sessionFactory.getCurrentSession().get(Guest.class, idP);
    }

    @Override
    public List<Guest> findPreviousGuests(final Room roomP, final int limitP) {
        String hql = "FROM Guest WHERE rentRoomId=:rentRoomIdP AND status=:statusP";
        return sessionFactory.getCurrentSession().createQuery(hql, Guest.class)
                .setParameter("rentRoomIdP", roomP.getId()).setParameter("statusP", GuestStatus.EVICTED)
                .setMaxResults(limitP).list();
    }

    @Override
    public void setEvicted(final Guest guestP) {
        guestP.setStatus(GuestStatus.EVICTED);
        sessionFactory.getCurrentSession().merge(guestP);
    }
}
