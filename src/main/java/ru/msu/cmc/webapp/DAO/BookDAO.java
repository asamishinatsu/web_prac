package ru.msu.cmc.webapp.DAO;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.msu.cmc.webapp.entities.Author;
import ru.msu.cmc.webapp.entities.CoverType;
import ru.msu.cmc.webapp.entities.Genre;
import ru.msu.cmc.webapp.entities.Book;

import java.util.List;

public interface BookDAO extends CommonDAO<Book, Long> {
    List<Book> getAllBooksByTitle(String title);
    List<Author> getAuthorsByBookId(Long bookId);
    List<Genre> getGenresByBookId(Long bookId);
    List<Book> getAllBookByFilter(Filter filter);

    @Builder
    @Getter
    @Setter
    class Filter {
        private String titleQuery;
        private String authorQuery;
        private String genreQuery;
        private String publisher;
        private Long year;
        private Long pagesMin, pagesMax;
        private CoverType coverType;
    }
}