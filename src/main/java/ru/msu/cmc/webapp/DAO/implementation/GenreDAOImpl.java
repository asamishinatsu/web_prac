package ru.msu.cmc.webapp.DAO.implementation;

import org.hibernate.Session;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import ru.msu.cmc.webapp.DAO.GenreDAO;
import ru.msu.cmc.webapp.entities.Genre;

import java.util.Collections;
import java.util.List;

@Repository
public class GenreDAOImpl extends CommonDAOImpl<Genre, Long> implements GenreDAO {

    public GenreDAOImpl() {
        super(Genre.class);
    }

    @Override
    public List<Genre> getAllGenresByName(String name) {
        try (Session session = sessionFactory.openSession()) {
            Query<Genre> query = session
                    .createQuery("FROM Genre WHERE name LIKE :gotName", Genre.class)
                    .setParameter("gotName", likeExpr(name));
            return !query.getResultList().isEmpty() ? query.getResultList() : Collections.emptyList();
        }
    }
}