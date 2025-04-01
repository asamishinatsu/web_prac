package ru.msu.cmc.webapp.DAO;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import ru.msu.cmc.webapp.entities.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(locations="classpath:application.properties")
public class CommonDAOTest {

    @Autowired
    private BookDAO bookDAO;

    @Test
    void getByIdTest() {
        Book book;

        book = bookDAO.getById(1L);
        assertNotNull(book);
    }

    @Test
    void getAllTest() {
        List<Book> bookList;

        bookList = (List<Book>) bookDAO.getAll();
        assertEquals(5, bookList.size());
    }

    @Test
    void saveDeleteTest() {
        Book book = new Book(
                10L,
                "test",
                "test_publisher",
                2025L,
                100L,
                CoverType.Hard,
                BigDecimal.valueOf(1000.0),
                1L,
                null,
                null);

        bookDAO.save(book);

        List<Book> listBook = bookDAO.getAllBooksByTitle("test");
        assertEquals(1, listBook.size());

        bookDAO.delete(book);
        listBook = bookDAO.getAllBooksByTitle("test");
        assertEquals(0, listBook.size());

        book = new Book(
                null,
                "test",
                "test_publisher",
                2025L,
                100L,
                CoverType.Hard,
                BigDecimal.valueOf(1000.0),
                1L,
                null,
                null);

        bookDAO.save(book);

        listBook = bookDAO.getAllBooksByTitle("test");
        assertEquals(1, listBook.size());

        bookDAO.delete(book);
        listBook = bookDAO.getAllBooksByTitle("test");
        assertEquals(0, listBook.size());
    }

    @Test
    void saveCollectionTest() {
        List<Book> bookList = new ArrayList<>();

        bookList.add(new Book(
                10L,
                "test1",
                "test_publisher",
                2025L,
                100L,
                CoverType.Hard,
                BigDecimal.valueOf(1000.0),
                1L,
                null,
                null));

        bookList.add(new Book(
                11L,
                "test2",
                "test_publisher",
                2025L,
                100L,
                CoverType.Hard,
                BigDecimal.valueOf(1000.0),
                1L,
                null,
                null));

        int prevSize = bookDAO.getAll().size();

        bookDAO.saveCollection(bookList);

        assertEquals(2, bookDAO.getAll().size() - prevSize);

        bookDAO.delete(bookDAO.getById(10L));
        bookDAO.delete(bookDAO.getById(11L));
    }

    @Test
    void updateTest() {
        Book book = new Book(
                null,
                "test",
                "test_publisher",
                2025L,
                100L,
                CoverType.Hard,
                BigDecimal.valueOf(1000.0),
                1L,
                null,
                null);

        bookDAO.save(book);

        assertEquals(1, bookDAO.getAllBooksByTitle("test").size());

        book = bookDAO.getAllBooksByTitle("test").get(0);
        book.setTitle("update");

        bookDAO.update(book);
        assertEquals(0, bookDAO.getAllBooksByTitle("test").size());
        assertEquals(1, bookDAO.getAllBooksByTitle("update").size());

        bookDAO.delete(bookDAO.getAllBooksByTitle("update").get(0));
    }

    @Test
    public void deleteByIdTest() {
        Book book = new Book(
                null,
                "test",
                "test_publisher",
                2025L,
                100L,
                CoverType.Hard,
                BigDecimal.valueOf(1000.0),
                1L,
                null,
                null);

        bookDAO.save(book);

        List<Book> listBook = bookDAO.getAllBooksByTitle("test");
        assertEquals(1, listBook.size());

        bookDAO.deleteById(listBook.get(0).getId());

        listBook = bookDAO.getAllBooksByTitle("test");
        assertEquals(0, listBook.size());
    }
}