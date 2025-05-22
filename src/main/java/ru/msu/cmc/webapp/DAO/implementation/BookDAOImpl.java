package ru.msu.cmc.webapp.DAO.implementation;


import org.hibernate.Session;
import org.hibernate.query.Query;

import org.springframework.stereotype.Repository;

import ru.msu.cmc.webapp.DAO.BookDAO;
import ru.msu.cmc.webapp.entities.*;

import javax.persistence.criteria.*;
import java.util.*;

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
            StringBuilder hql = new StringBuilder("SELECT DISTINCT b FROM Book b LEFT JOIN b.bookAuthorList ba LEFT JOIN ba.author a LEFT JOIN b.bookGenreList bg LEFT JOIN bg.genre g");
            List<String> conditions = new ArrayList<>();
            Map<String, Object> parameters = new HashMap<>();

            List<String> orConditions = new ArrayList<>();
            if (filter.getTitleQuery() != null && !filter.getTitleQuery().trim().isEmpty()) {
                orConditions.add("lower(b.title) LIKE lower(:titleQueryParam)");
                parameters.put("titleQueryParam", likeExpr(filter.getTitleQuery()));
            }
            if (filter.getAuthorQuery() != null && !filter.getAuthorQuery().trim().isEmpty()) {
                orConditions.add("lower(a.name) LIKE lower(:authorQueryParam)");
                parameters.put("authorQueryParam", likeExpr(filter.getAuthorQuery()));
            }
            if (filter.getGenreQuery() != null && !filter.getGenreQuery().trim().isEmpty()) {
                orConditions.add("lower(g.name) LIKE lower(:genreQueryParam)");
                parameters.put("genreQueryParam", likeExpr(filter.getGenreQuery()));
            }

            if (!orConditions.isEmpty()) {
                conditions.add("(" + String.join(" OR ", orConditions) + ")");
            }

            if (filter.getPublisher() != null && !filter.getPublisher().trim().isEmpty()) {
                conditions.add("lower(b.publisher) LIKE lower(:publisherParam)");
                parameters.put("publisherParam", likeExpr(filter.getPublisher()));
            }
            if (filter.getYear() != null) {
                conditions.add("b.year = :yearParam"); // b.year - это поле в Entity Book, а не year_of_publication
                parameters.put("yearParam", filter.getYear());
            }
            if (filter.getPagesMin() != null) {
                conditions.add("b.pages >= :pagesMinParam");
                parameters.put("pagesMinParam", filter.getPagesMin());
            }
            if (filter.getPagesMax() != null) {
                conditions.add("b.pages <= :pagesMaxParam");
                parameters.put("pagesMaxParam", filter.getPagesMax());
            }
            if (filter.getCoverType() != null) {
                conditions.add("b.cover = :coverTypeParam"); // b.cover - поле в Entity Book
                parameters.put("coverTypeParam", filter.getCoverType());
            }
            // Если бы использовали authorId / genreId:
            // if (filter.getAuthorId() != null) {
            //     conditions.add("a.id = :authorIdParam");
            //     parameters.put("authorIdParam", filter.getAuthorId());
            // }
            // if (filter.getGenreId() != null) {
            //     conditions.add("g.id = :genreIdParam");
            //     parameters.put("genreIdParam", filter.getGenreId());
            // }


            if (!conditions.isEmpty()) {
                hql.append(" WHERE ").append(String.join(" AND ", conditions));
            }

            // Добавим ORDER BY по названию по умолчанию, как в задании
            hql.append(" ORDER BY b.title ASC");


            Query<Book> query = session.createQuery(hql.toString(), Book.class);
            for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
            return query.list();
        }
    }

}