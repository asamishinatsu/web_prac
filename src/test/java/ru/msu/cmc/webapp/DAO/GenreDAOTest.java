package ru.msu.cmc.webapp.DAO;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webapp.entities.Genre;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class GenreDAOTest {

    @Autowired
    private GenreDAO genreDAO;

    @Test
    void getAllGenresByNameTest() {
        List<Genre> genreList = genreDAO.getAllGenresByName("Антиутопия");
        assertEquals(1, genreList.size());
        genreList = genreDAO.getAllGenresByName("hihihihahaha");
        assertEquals(0, genreList.size());
    }
}