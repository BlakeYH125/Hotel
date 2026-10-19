package org.hotel.dao.dao;

import org.hibernate.SessionFactory;
import org.hotel.model.entities.User;
import org.hotel.dao.repository.UserRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class UserDao implements UserRepository {

    /**
     * Здесь будет храниться открытая сессия с БД.
     */
    private final SessionFactory sessionFactory;

    public UserDao(final SessionFactory sessionFactoryP) {
        this.sessionFactory = sessionFactoryP;
    }

    @Override
    public void save(final User userP) {
        sessionFactory.getCurrentSession().merge(userP);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(final String usernameP) {
        String hql = "FROM User WHERE username = :usernameP";
        return sessionFactory.getCurrentSession()
                .createQuery(hql, User.class)
                .setParameter("usernameP", usernameP)
                .uniqueResultOptional();
    }

    @Override
    public boolean isOwner(String username, String guestId) {
        String hql = "SELECT count(g) FROM Guest g WHERE g.id = :gId AND g.user.username = :uname";
        Long count = sessionFactory.getCurrentSession().createQuery(hql, Long.class).setParameter("gId", guestId).setParameter("uname", username).uniqueResult();
        return count != null && count > 0;
    }

    @Override
    public boolean existsAnyUser() {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT 1 FROM User", String.class)
                .setMaxResults(1)
                .uniqueResultOptional()
                .isPresent();
    }
}
