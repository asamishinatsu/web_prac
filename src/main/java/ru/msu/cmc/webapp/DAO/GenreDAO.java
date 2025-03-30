package ru.msu.cmc.webapp.DAO;

import ru.msu.cmc.webapp.entities.Author;
import ru.msu.cmc.webapp.entities.Genre;

import java.util.List;

public interface GenreDAO extends CommonDAO<Genre, Long> {
    List<Genre> getAllGenresByName(String name);
}
