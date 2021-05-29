package dao;

import com.google.inject.persist.Transactional;
import model.Book;
import util.jpa.GenericJpaDao;

import java.util.List;

public class BookDAO extends GenericJpaDao<Book> {

    public List<Book> findBooks(String keyword, String searchTerm) {
        String query = "select b from Book b where :keyword like :searchTerm".replace(":keyword", keyword);
        return entityManager.createQuery(query, Book.class)
                .setParameter("searchTerm", String.format("%%%s%%", searchTerm))
                .getResultList();
    }

    public Book findBookById(String bookId) {
        return entityManager.createQuery("select b from Book b where id = :bookId", Book.class)
                .setParameter("bookId", bookId)
                .getSingleResult();
    }

    @Transactional
    public void updateAvailability(String bookId, Boolean availability) {
        Book book = findBookById(bookId);
        book.setAvailable(availability);
    }
}
