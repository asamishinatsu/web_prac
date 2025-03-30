package ru.msu.cmc.webapp.DAO;

import ru.msu.cmc.webapp.entities.Author;

import java.util.List;

public interface AuthorDAO extends CommonDAO<Author, Long> {
    List<Author> getAllAuthorsByName(String name);
}