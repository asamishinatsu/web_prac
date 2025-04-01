package ru.msu.cmc.webapp.DAO;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webapp.entities.*;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class BookDAOTest {

    @Autowired
    private BookDAO bookDAO;
    @Autowired
    private SessionFactory sessionFactory;

    @Test
    void getAllBooksByTitleTest() {
        List<Book> listBook = bookDAO.getAllBooksByTitle("1984");
        assertEquals(1, listBook.size());

        listBook = bookDAO.getAllBooksByTitle("hihihihahaha");
        assertEquals(0, listBook.size());
    }

    @Test
    void getAuthorsByBookIdTest() {
        List<Author> authorList = bookDAO.getAuthorsByBookId(1L);
        assertEquals(1, authorList.size());

        authorList = bookDAO.getAuthorsByBookId(0L);
        assertEquals(0, authorList.size());
    }

    @Test
    void getGenresByBookIdTest() {
        List<Genre> genreList = bookDAO.getGenresByBookId(1L);
        assertEquals(1, genreList.size());

        genreList = bookDAO.getGenresByBookId(0L);
        assertEquals(0, genreList.size());
    }

    @Test
    void getAllBookByFilterTest() {

        BookDAO.Filter filter = BookDAO.Filter.builder()
                .authorId(1L)
                .genreId(1L)
                .publisher("Союз")
                .year(1949L)
                .pagesMin(0L)
                .pagesMax(100000L)
                .coverType(CoverType.Hard)
                .build();
        List<Book> bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(1, bookList.size());

        filter = BookDAO.Filter.builder()
                .authorId(1L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(1, bookList.size());

        filter = BookDAO.Filter.builder()
                .genreId(1L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(1, bookList.size());

        filter = BookDAO.Filter.builder()
                .publisher("Союз")
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(1, bookList.size());

        filter = BookDAO.Filter.builder()
                .year(1949L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(1, bookList.size());

        filter = BookDAO.Filter.builder()
                .pagesMin(0L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(5, bookList.size());

        filter = BookDAO.Filter.builder()
                .pagesMax(10000L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(5, bookList.size());

        filter = BookDAO.Filter.builder()
                .pagesMin(0L)
                .pagesMax(100000L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(5, bookList.size());

        filter = BookDAO.Filter.builder()
                .pagesMin(328L)
                .pagesMax(0L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(1, bookList.size());

        filter = BookDAO.Filter.builder()
                .pagesMin(-1L)
                .pagesMax(-1L)
                .build();
        bookList = bookDAO.getAllBookByFilter(filter);
        assertEquals(0, bookList.size());
    }
}