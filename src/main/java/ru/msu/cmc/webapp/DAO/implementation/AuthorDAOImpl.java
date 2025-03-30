package ru.msu.cmc.webapp.DAO.implementation;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import ru.msu.cmc.webapp.DAO.AuthorDAO;
import ru.msu.cmc.webapp.entities.Author;

import java.util.Collections;
import java.util.List;

@Repository
public class AuthorDAOImpl extends CommonDAOImpl<Author, Long> implements AuthorDAO {

    public AuthorDAOImpl() {
        super(Author.class);
    }

    @Override
    public List<Author> getAllAuthorsByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Author> query = session
                    .createQuery("FROM Author WHERE name LIKE :gotName", Author.class)
                    .setParameter("gotName", likeExpr(name));
            return !query.getResultList().isEmpty() ? query.getResultList() : Collections.emptyList();
        }
    }
}