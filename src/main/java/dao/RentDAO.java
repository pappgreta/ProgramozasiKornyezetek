package dao;

import model.Rent;
import util.jpa.GenericJpaDao;

import java.util.List;

public class RentDAO extends GenericJpaDao<Rent> {

    public List<String> findRents() {
        return entityManager.createQuery("select r.id || ' : ' || b.title || ' : ' || u.name " +
                        " from Rent r, User u, Book b where r.bookId = b.id and r.userId = u.id",
                String.class).getResultList();
    }

    public Rent findRentById(Integer rentId) {
        return entityManager.createQuery("select r from Rent r where r.id = :rentId", Rent.class)
                .setParameter("rentId", rentId)
                .getSingleResult();
    }
}
