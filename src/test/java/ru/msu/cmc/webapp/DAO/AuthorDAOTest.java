package ru.msu.cmc.webapp.DAO;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webapp.entities.Author;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class AuthorDAOTest {

    @Autowired
    private AuthorDAO authorDAO;

    @Test
    void getAllAuthorByNameTest() {
        List<Author> authorList;

        authorList = authorDAO.getAllAuthorsByName("Михаил Булгаков");
        assertEquals(1, authorList.size());

        authorList = authorDAO.getAllAuthorsByName("testtesttest");
        assertEquals(0, authorList.size());
    }
}