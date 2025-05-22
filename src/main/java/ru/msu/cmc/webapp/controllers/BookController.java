package ru.msu.cmc.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.msu.cmc.webapp.DAO.BookDAO;
import ru.msu.cmc.webapp.entities.Book;
import ru.msu.cmc.webapp.entities.CoverType; // Убедитесь, что импорт правильный

import java.util.List;

@Controller
@RequestMapping("/") // Все запросы, начинающиеся с /, будут направлены сюда, если нет более специфичного маппинга
public class BookController {

    private final BookDAO bookDAO;

    @Autowired
    public BookController(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    @GetMapping
    public String homePage(@RequestParam(required = false) String titleQuery,
                           @RequestParam(required = false) String authorQuery,
                           @RequestParam(required = false) String genreQuery,
                           @RequestParam(required = false) String publisher,
                           @RequestParam(required = false) Long year,
                           @RequestParam(required = false) Long pagesMin,
                           @RequestParam(required = false) Long pagesMax,
                           @RequestParam(required = false) CoverType coverType, // Spring автоматически преобразует строку в Enum
                           Model model) {

        BookDAO.Filter filter = BookDAO.Filter.builder()
                .titleQuery(titleQuery)
                .authorQuery(authorQuery)
                .genreQuery(genreQuery)
                .publisher(publisher)
                .year(year)
                .pagesMin(pagesMin)
                .pagesMax(pagesMax)
                .coverType(coverType)
                .build();

        List<Book> books = bookDAO.getAllBookByFilter(filter);

        model.addAttribute("books", books);
        // Передаем обратно в модель значения фильтров, чтобы поля оставались заполненными
        model.addAttribute("titleQuery", titleQuery);
        model.addAttribute("authorQuery", authorQuery);
        model.addAttribute("genreQuery", genreQuery);
        model.addAttribute("publisher", publisher);
        model.addAttribute("year", year);
        model.addAttribute("pagesMin", pagesMin);
        model.addAttribute("pagesMax", pagesMax);
        model.addAttribute("coverType", coverType);

        // Передаем все возможные типы обложек для выпадающего списка
        model.addAttribute("allCoverTypes", CoverType.values());

        return "public/index"; // Имя шаблона главной страницы
    }

    @GetMapping("/books/{bookId}")
    public String bookDetailPage(@PathVariable Long bookId, Model model) {
        Book book = bookDAO.getById(bookId);

        if (book == null) {
            // Если книга не найдена, можно просто вернуть модель с book=null,
            // шаблон book_detail.html это обработает.
            // Либо редирект на главную с сообщением, но это усложнит.
            // Оставим пока так, шаблон покажет сообщение "Книга не найдена".
        }
        model.addAttribute("book", book);
        // Детальная информация (авторы, жанры) уже есть в объекте Book благодаря EAGER Fetch
        // или будет подгружена при обращении, если EAGER (но у нас EAGER для bookAuthorList, bookGenreList)

        return "public/book_detail"; // Имя шаблона детальной страницы книги
    }
}