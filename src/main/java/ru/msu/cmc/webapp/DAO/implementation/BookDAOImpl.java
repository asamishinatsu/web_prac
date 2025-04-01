package ru.msu.cmc.webapp.DAO.implementation;


import org.hibernate.Session;
import org.hibernate.query.Query;

import org.springframework.stereotype.Repository;

import ru.msu.cmc.webapp.DAO.BookDAO;
import ru.msu.cmc.webapp.entities.*;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
public class BookDAOImpl extends CommonDAOImpl<Book, Long> implements BookDAO {

    public BookDAOImpl() {
        super(Book.class);
    }

    @Override
    public List<Book> getAllBooksByTitle(String title) {
        try (Session session = sessionFactory.openSession()) {
            Query<Book> query = session
                    .createQuery("FROM Book WHERE title LIKE :gotTitle", Book.class)
                    .setParameter("gotTitle", likeExpr(title));
            return !query.getResultList().isEmpty() ? query.getResultList() : Collections.emptyList();
        }
    }

    @Override
    public List<Author> getAuthorsByBookId(Long bookId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Author> query = session
                    .createQuery("SELECT ba.author FROM BookAuthor ba " +
                            "WHERE ba.book.id = :gotBookId", Author.class)
                    .setParameter("gotBookId", bookId);
            return !query.getResultList().isEmpty() ? query.getResultList() : Collections.emptyList();
        }
    }

    @Override
    public List<Genre> getGenresByBookId(Long bookId) {
        try (Session session = sessionFactory.openSession()) {
            Query<Genre> query = session
                    .createQuery("SELECT bg.genre FROM BookGenre bg " +
                            "WHERE bg.book.id = :gotBookId", Genre.class)
                    .setParameter("gotBookId", bookId);
            return !query.getResultList().isEmpty() ? query.getResultList() : Collections.emptyList();
        }
    }

    @Override
    public List<Book> getAllBookByFilter(Filter filter) {
        try (Session session = sessionFactory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Book> query = cb.createQuery(Book.class);
            Root<Book> root = query.from(Book.class);

            List<Predicate> predicates = new ArrayList<>();

            if (filter.getAuthorId() != null) {
                Join<Book, BookAuthor> bookAuthorJoin = root.join("bookAuthorList");
                Join<BookAuthor, Author> authorJoin = bookAuthorJoin.join("author");
                predicates.add(cb.equal(authorJoin.get("id"), filter.getAuthorId()));
            }

            if (filter.getGenreId() != null) {
                Join<Book, BookGenre> bookGenreJoin = root.join("bookGenreList");
                Join<BookGenre, Genre> genreJoin = bookGenreJoin.join("genre");
                predicates.add(cb.equal(genreJoin.get("id"), filter.getGenreId()));
            }

            if (filter.getPublisher() != null) {
                predicates.add(cb.equal(root.get("publisher"), filter.getPublisher()));
            }

            if (filter.getYear() != null) {
                predicates.add(cb.equal(root.get("year"), filter.getYear()));
            }

            if (filter.getPagesMin() != null) {
                Long minPages = filter.getPagesMin() >= 0 ? filter.getPagesMin() : 0;
                predicates.add(cb.ge(root.get("pages"), minPages));
            }

            if (filter.getPagesMax() != null) {
                Long maxPages = filter.getPagesMax() >= 0 ? filter.getPagesMax() : 0;
                if (filter.getPagesMin() != null) {
                    maxPages = maxPages > filter.getPagesMin() ? maxPages : filter.getPagesMin();
                }
                predicates.add(cb.le(root.get("pages"), maxPages));
            }

            if (filter.getCoverType() != null) {
                predicates.add(cb.equal(root.get("cover"), filter.getCoverType()));
            }

            query.select(root).where(predicates.toArray(new Predicate[0])).distinct(true);

            return session.createQuery(query).getResultList();
        }
    }

}