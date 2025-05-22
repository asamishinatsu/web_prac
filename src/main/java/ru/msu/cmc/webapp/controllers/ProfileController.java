package ru.msu.cmc.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.webapp.DAO.CustomerDAO;
import ru.msu.cmc.webapp.DAO.OrderDAO;
import ru.msu.cmc.webapp.entities.Customer;
import ru.msu.cmc.webapp.entities.Order;

import jakarta.servlet.http.HttpSession; // Используем jakarta.servlet
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final CustomerDAO customerDAO;
    private final OrderDAO orderDAO;

    @Autowired
    public ProfileController(CustomerDAO customerDAO, OrderDAO orderDAO) {
        this.customerDAO = customerDAO;
        this.orderDAO = orderDAO;
    }

    // Вспомогательный метод для проверки авторизации и получения текущего пользователя
    private Customer getCurrentAuthenticatedUser(HttpSession session) {
        return (Customer) session.getAttribute("currentUser");
    }

    @GetMapping
    public String viewProfile(HttpSession session, Model model) {
        Customer currentUser = getCurrentAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login"; // Не авторизован - на страницу входа
        }

        // Обновляем данные пользователя из БД, на случай если они изменились в другой сессии/месте
        // (хотя в нашем простом случае это маловероятно, но хорошая практика)
        Customer freshCurrentUser = customerDAO.getById(currentUser.getId());
        if (freshCurrentUser == null) { // Если пользователь был удален из БД пока был в сессии
            session.removeAttribute("currentUser");
            session.invalidate();
            return "redirect:/login?error=user_deleted";
        }
        session.setAttribute("currentUser", freshCurrentUser); // Обновляем в сессии


        List<Order> orders = orderDAO.getOrdersByCustomer(freshCurrentUser, 5); // Последние 5 заказов

        model.addAttribute("currentUser", freshCurrentUser);
        model.addAttribute("orders", orders);

        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam String fullName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                @RequestParam(required = false) String address,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Customer currentUser = getCurrentAuthenticatedUser(session);
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Получаем актуальную версию пользователя из БД для обновления
        Customer customerToUpdate = customerDAO.getById(currentUser.getId());
        if (customerToUpdate == null) {
            session.invalidate();
            return "redirect:/login?error=user_not_found_for_update";
        }

        // Проверка, не пытается ли пользователь сменить email на уже существующий у другого
        if (!customerToUpdate.getEmail().equalsIgnoreCase(email)) {
            Customer customerWithNewEmail = customerDAO.getCustomerByEmail(email);
            if (customerWithNewEmail != null && !Objects.equals(customerWithNewEmail.getId(), customerToUpdate.getId())) {
                redirectAttributes.addFlashAttribute("profileUpdateError", "Этот Email уже используется другим пользователем.");
                return "redirect:/profile";
            }
        }

        // Валидация обязательных полей
        if (fullName.trim().isEmpty() || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("profileUpdateError", "ФИО и Email не могут быть пустыми.");
            return "redirect:/profile";
        }

        customerToUpdate.setFullName(fullName);
        customerToUpdate.setEmail(email);
        customerToUpdate.setPhone(phone);
        customerToUpdate.setAddress(address);

        try {
            customerDAO.update(customerToUpdate);
            // Обновляем данные пользователя в сессии
            session.setAttribute("currentUser", customerToUpdate);
            redirectAttributes.addFlashAttribute("profileUpdateSuccess", "Данные профиля успешно обновлены.");
        } catch (Exception e) {
            // Логирование ошибки e.printStackTrace(); или logger.error("...", e);
            redirectAttributes.addFlashAttribute("profileUpdateError", "Не удалось обновить данные профиля. Попробуйте позже.");
        }

        return "redirect:/profile";
    }
}