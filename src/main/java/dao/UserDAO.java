package dao;

import util.jpa.GenericJpaDao;
import model.User;

import java.util.List;

public class UserDAO extends GenericJpaDao<User> {

    public List<String> findUsers() {
        return entityManager.createQuery("select u.id || ':' || u.name from User u", String.class).getResultList();
    }
}
