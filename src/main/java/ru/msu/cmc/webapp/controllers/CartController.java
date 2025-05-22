package ru.msu.cmc.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.webapp.DAO.BookDAO;
import ru.msu.cmc.webapp.entities.Book;

import jakarta.servlet.http.HttpSession; // Используем jakarta.servlet
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final BookDAO bookDAO;

    @Autowired
    public CartController(BookDAO bookDAO) {
        this.bookDAO = bookDAO;
    }

    // Вспомогательный класс для отображения товаров в корзине в шаблоне
    // Lombok аннотации @Getter, @Setter, @AllArgsConstructor можно использовать для сокращения кода
    public static class CartDisplayItem {
        private Book book;
        private int quantity;

        public CartDisplayItem(Book book, int quantity) {
            this.book = book;
            this.quantity = quantity;
        }

        public Book getBook() {
            return book;
        }

        public int getQuantity() {
            return quantity;
        }
        // сеттеры не нужны, если объект создается один раз
    }

    // Метод для получения корзины из сессии
    @SuppressWarnings("unchecked") // Подавление предупреждения для приведения типа
    private Map<Long, Integer> getCartFromSession(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        Map<Long, Integer> cartSessionItems = getCartFromSession(session);
        List<CartDisplayItem> cartDisplayItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        if (!cartSessionItems.isEmpty()) {
            for (Map.Entry<Long, Integer> entry : cartSessionItems.entrySet()) {
                Book book = bookDAO.getById(entry.getKey());
                if (book != null) { // Убедимся, что книга все еще существует
                    cartDisplayItems.add(new CartDisplayItem(book, entry.getValue()));
                    totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
                } else {
                    // Если книги нет в БД, стоит удалить ее из сессионной корзины
                    // Но это может произойти асинхронно, для простоты пока опустим авто-удаление здесь
                }
            }
        }

        model.addAttribute("cartItems", cartDisplayItems);
        model.addAttribute("totalPrice", totalPrice);
        return "public/cart";
    }

    @PostMapping("/add/{bookId}")
    public String addBookToCart(@PathVariable Long bookId,
                                @RequestParam(defaultValue = "1") int quantity, // Можно добавить параметр для количества
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Book book = bookDAO.getById(bookId);
        if (book == null || book.getStock() == null || book.getStock() < quantity) {
            redirectAttributes.addFlashAttribute("errorMessage", "Не удалось добавить товар: книга не найдена или нет в наличии.");
            return "redirect:/cart"; // или на страницу книги: "redirect:/books/" + bookId
        }

        Map<Long, Integer> cart = getCartFromSession(session);
        int currentQuantityInCart = cart.getOrDefault(bookId, 0);

        if (currentQuantityInCart + quantity > book.getStock()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Невозможно добавить такое количество, превышен остаток на складе.");
            // Можно редиректить обратно на страницу книги, если добавление было оттуда
            // return "redirect:" + request.getHeader("Referer"); // Осторожно с Referer
            return "redirect:/books/" + bookId;
        }

        cart.put(bookId, currentQuantityInCart + quantity);
        session.setAttribute("cart", cart); // Обновляем корзину в сессии

        redirectAttributes.addFlashAttribute("successMessage", "Книга '" + book.getTitle() + "' добавлена в корзину.");
        return "redirect:/cart";
    }

    @PostMapping("/update/{bookId}")
    public String updateCartItem(@PathVariable Long bookId,
                                 @RequestParam int quantity,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Map<Long, Integer> cart = getCartFromSession(session);
        Book book = bookDAO.getById(bookId);

        if (!cart.containsKey(bookId) || book == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: товар не найден в корзине или в каталоге.");
            return "redirect:/cart";
        }

        if (quantity <= 0) {
            cart.remove(bookId); // Удаляем товар, если количество 0 или меньше
            redirectAttributes.addFlashAttribute("successMessage", "Товар '" + book.getTitle() + "' удален из корзины.");
        } else if (book.getStock() != null && quantity > book.getStock()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Невозможно установить такое количество для '" + book.getTitle() + "', превышен остаток на складе. Максимум: " + book.getStock());
        }
        else {
            cart.put(bookId, quantity);
            redirectAttributes.addFlashAttribute("successMessage", "Количество товара '" + book.getTitle() + "' обновлено.");
        }
        session.setAttribute("cart", cart);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{bookId}")
    public String removeCartItem(@PathVariable Long bookId,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Map<Long, Integer> cart = getCartFromSession(session);
        Book book = bookDAO.getById(bookId); // Для получения названия в сообщение

        if (cart.containsKey(bookId)) {
            cart.remove(bookId);
            session.setAttribute("cart", cart);
            if (book != null) {
                redirectAttributes.addFlashAttribute("successMessage", "Товар '" + book.getTitle() + "' удален из корзины.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Товар удален из корзины.");
            }
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка: товар не найден в корзине.");
        }
        return "redirect:/cart";
    }
}