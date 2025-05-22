package ru.msu.cmc.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.webapp.DAO.BookDAO;
import ru.msu.cmc.webapp.DAO.CustomerDAO;
import ru.msu.cmc.webapp.DAO.OrderDAO;
// import ru.msu.cmc.webapp.DAO.OrderBookDAO; // OrderBookDAO больше не нужен для сохранения
import ru.msu.cmc.webapp.entities.*;

import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderDAO orderDAO;
    private final BookDAO bookDAO;
    private final CustomerDAO customerDAO;
    // private final OrderBookDAO orderBookDAO; // Убираем зависимость от OrderBookDAO

    @Autowired
    public OrderController(OrderDAO orderDAO, BookDAO bookDAO, CustomerDAO customerDAO /* OrderBookDAO убран */) {
        this.orderDAO = orderDAO;
        this.bookDAO = bookDAO;
        this.customerDAO = customerDAO;
        // this.orderBookDAO = orderBookDAO; // Убираем
    }

    // ... (методы getCurrentAuthenticatedUser, getCartFromSession остаются без изменений) ...
    private Customer getCurrentAuthenticatedUser(HttpSession session) {
        return (Customer) session.getAttribute("currentUser");
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getCartFromSession(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        return (cart != null) ? cart : new HashMap<>();
    }


    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // ... (код этого метода остается без изменений, он не использовал OrderBookDAO) ...
        Customer currentUser = getCurrentAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Map<Long, Integer> cartSessionItems = getCartFromSession(session);
        if (cartSessionItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("checkoutError", "Ваша корзина пуста. Невозможно оформить заказ.");
            return "redirect:/cart";
        }

        List<CartController.CartDisplayItem> cartDisplayItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (Map.Entry<Long, Integer> entry : cartSessionItems.entrySet()) {
            Book book = bookDAO.getById(entry.getKey());
            if (book != null) {
                cartDisplayItems.add(new CartController.CartDisplayItem(book, entry.getValue()));
                totalPrice = totalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }

        if (cartDisplayItems.isEmpty() && !cartSessionItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("checkoutError", "Товары в вашей корзине больше не доступны. Пожалуйста, обновите корзину.");
            session.removeAttribute("cart");
            return "redirect:/cart";
        }

        Customer customerDataFromDB = customerDAO.getById(currentUser.getId());
        model.addAttribute("customerData", customerDataFromDB != null ? customerDataFromDB : currentUser);
        model.addAttribute("cartItems", cartDisplayItems);
        model.addAttribute("totalPrice", totalPrice);

        return "user/order_checkout";
    }

    @Transactional
    @PostMapping("/checkout")
    public String processOrder(@RequestParam String fullName,
                               @RequestParam String email,
                               @RequestParam(required = false) String phone,
                               @RequestParam String deliveryAddress,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Customer currentUser = getCurrentAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Map<Long, Integer> cartSessionItems = getCartFromSession(session);
        if (cartSessionItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("checkoutError", "Ваша корзина пуста. Невозможно оформить заказ.");
            redirectAttributes.addFlashAttribute("submittedFullName", fullName);
            redirectAttributes.addFlashAttribute("submittedEmail", email);
            redirectAttributes.addFlashAttribute("submittedPhone", phone);
            redirectAttributes.addFlashAttribute("submittedDeliveryAddress", deliveryAddress);
            return "redirect:/order/checkout";
        }

        if (deliveryAddress.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("checkoutError", "Адрес доставки не может быть пустым.");
            redirectAttributes.addFlashAttribute("submittedFullName", fullName);
            redirectAttributes.addFlashAttribute("submittedEmail", email);
            redirectAttributes.addFlashAttribute("submittedPhone", phone);
            return "redirect:/order/checkout";
        }

        for (Map.Entry<Long, Integer> entry : cartSessionItems.entrySet()) {
            Book book = bookDAO.getById(entry.getKey());
            if (book == null || book.getStock() == null || book.getStock() < entry.getValue()) {
                redirectAttributes.addFlashAttribute("checkoutError",
                        "Книги '" + (book != null ? book.getTitle() : "ID:"+entry.getKey()) + "' нет в наличии в нужном количестве. Пожалуйста, обновите корзину.");
                return "redirect:/cart";
            }
        }

        Order newOrder = new Order();
        newOrder.setCustomer(currentUser);
        newOrder.setOrderDate(LocalDateTime.now());
        newOrder.setDeliveryAddress(deliveryAddress);
        newOrder.setStatus(StatusType.Processing);

        BigDecimal calculatedTotalPrice = BigDecimal.ZERO;
        // List<OrderBook> orderBooksForThisOrder = new ArrayList<>(); // Список уже инициализирован в newOrder

        for (Map.Entry<Long, Integer> entry : cartSessionItems.entrySet()) {
            Book book = bookDAO.getById(entry.getKey());
            int quantity = entry.getValue();

            OrderBook orderBook = new OrderBook();
            orderBook.setBook(book);
            orderBook.setQuantity((long)quantity);
            orderBook.setPrice(book.getPrice());
            // orderBook.setOrder(newOrder); // Устанавливаем связь - можно здесь или через newOrder.addOrderBook()

            orderBook.setOrder(newOrder); // 1. Устанавливаем ссылку от OrderBook к Order
            if (newOrder.getOrderBookList() == null) { // 2. Убеждаемся, что список инициализирован
                newOrder.setOrderBookList(new ArrayList<>());
            }
            newOrder.getOrderBookList().add(orderBook); // 3. Добавляем OrderBook в список в Order

            calculatedTotalPrice = calculatedTotalPrice.add(book.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }
        newOrder.setTotalPrice(calculatedTotalPrice);

        try {
            orderDAO.save(newOrder); // Сохраняем Order, JPA/Hibernate каскадно сохранит связанные OrderBook'и

            // Обновляем стоки книг ПОСЛЕ успешного сохранения заказа и его элементов
            for (OrderBook ob : newOrder.getOrderBookList()) { // Итерируемся по элементам сохраненного заказа
                Book bookToUpdateStock = ob.getBook();
                bookToUpdateStock.setStock(bookToUpdateStock.getStock() - ob.getQuantity());
                bookDAO.update(bookToUpdateStock);
            }

            session.removeAttribute("cart");
            redirectAttributes.addFlashAttribute("orderSuccessMessage", "Ваш заказ №" + newOrder.getId() + " успешно оформлен!");
            return "redirect:/order/" + newOrder.getId();

        } catch (Exception e) {
            // Логирование ошибки
            // e.printStackTrace(); // Для отладки
            // logger.error("Error processing order", e); // Если есть логгер

            redirectAttributes.addFlashAttribute("checkoutError", "Произошла ошибка при оформлении заказа. Пожалуйста, попробуйте еще раз.");
            redirectAttributes.addFlashAttribute("submittedFullName", fullName);
            redirectAttributes.addFlashAttribute("submittedEmail", email);
            redirectAttributes.addFlashAttribute("submittedPhone", phone);
            redirectAttributes.addFlashAttribute("submittedDeliveryAddress", deliveryAddress);
            return "redirect:/order/checkout";
        }
    }

    @GetMapping("/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        // ... (код этого метода остается без изменений) ...
        Customer currentUser = getCurrentAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderDAO.getById(orderId);

        if (order == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Заказ не найден.");
            return "redirect:/profile";
        }

        if (!Objects.equals(order.getCustomer().getId(), currentUser.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "У вас нет прав на просмотр этого заказа.");
            return "redirect:/profile";
        }

        model.addAttribute("order", order);
        return "user/order_detail";
    }
}