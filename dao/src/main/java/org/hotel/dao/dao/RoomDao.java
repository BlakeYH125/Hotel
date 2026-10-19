package org.hotel.dao.dao;

import org.hibernate.LockMode;
import org.hibernate.SessionFactory;
import org.hotel.model.entities.Room;
import org.hotel.model.enums.RoomStatus;
import org.hotel.dao.repository.RoomRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

@Repository
public class RoomDao implements RoomRepository {
    /**
     * Здесь будет храниться открытая сессия с БД.
     */
    private final SessionFactory sessionFactory;

    public RoomDao(final SessionFactory sessionFactoryP) {
        this.sessionFactory = sessionFactoryP;
    }

    @Override
    public void save(final Room roomP) {
        sessionFactory.getCurrentSession().merge(roomP);
    }

    @Override
    public List<Room> findAll() {
        return sessionFactory.getCurrentSession().createQuery("FROM Room", Room.class).list();
    }

    @Override
    public void setAvailable(final Room roomP) {
        roomP.setStatus(RoomStatus.AVAILABLE);
        roomP.setReleasedIn(null);
        sessionFactory.getCurrentSession().merge(roomP);
    }

    @Override
    public void setStatus(final Room roomP, final Date releasedInP, final RoomStatus roomStatusP) {
        roomP.setStatus(roomStatusP);
        roomP.setReleasedIn(releasedInP);
        sessionFactory.getCurrentSession().merge(roomP);
    }

    @Override
    public void setNewRoomPrice(final Room roomP, final BigDecimal priceP) {
        roomP.setPrice(priceP);
        sessionFactory.getCurrentSession().merge(roomP);
    }

    @Override
    public Room getRoom(final String idP) {
        return sessionFactory.getCurrentSession().get(Room.class, idP);
    }

    @Override
    public List<Room> findFreeRoomsByDate(final Date dateP) {
        String hql = "FROM Room WHERE releasedIn<:releasedInP OR roomStatus=:statusP";
        return sessionFactory.getCurrentSession().createQuery(hql, Room.class)
                .setParameter("releasedInP", dateP).setParameter("statusP", RoomStatus.AVAILABLE).list();
    }

    @Override
    public Room getRoomForUpdate(String roomId) {
        return sessionFactory.getCurrentSession().get(Room.class, roomId, LockMode.PESSIMISTIC_WRITE);
    }
}
