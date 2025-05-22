package ru.msu.cmc.webapp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.msu.cmc.webapp.DAO.CustomerDAO;
import ru.msu.cmc.webapp.entities.Customer;

import jakarta.servlet.http.HttpSession; // Используем jakarta.servlet

@Controller
public class AuthController {

    private final CustomerDAO customerDAO;

    @Autowired
    public AuthController(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    // ----- Регистрация -----
    @GetMapping("/register")
    public String registerPage(HttpSession session, Model model) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/"; // Уже авторизован, на главную
        }
        // Передаем пустые значения, чтобы Thymeleaf не ругался на отсутствующие атрибуты th:value
        // в случае первой загрузки или если не было ошибок и редиректа
        model.addAttribute("username", "");
        model.addAttribute("fullName", "");
        model.addAttribute("email", "");
        model.addAttribute("phone", "");
        model.addAttribute("address", "");
        return "public/register";
    }

    @PostMapping("/register")
    public String handleRegistration(@RequestParam String username,
                                     @RequestParam String password,
                                     @RequestParam String confirmPassword,
                                     @RequestParam String fullName,
                                     @RequestParam String email,
                                     @RequestParam(required = false) String phone,
                                     @RequestParam(required = false) String address,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes,
                                     Model model) { // Model для случая, если не редирект, а forward с ошибкой

        if (session.getAttribute("currentUser") != null) {
            return "redirect:/"; // Уже авторизован
        }

        // 1. Проверка совпадения паролей
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("passwordMismatchError", "Пароли не совпадают.");
            // Сохраняем введенные данные (кроме паролей) для возврата в форму
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("phone", phone);
            redirectAttributes.addFlashAttribute("address", address);
            return "redirect:/register";
        }

        // 2. Проверка, не занят ли username
        if (customerDAO.getCustomerByUsername(username) != null) {
            redirectAttributes.addFlashAttribute("usernameExistsError", "Пользователь с таким логином уже существует.");
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("phone", phone);
            redirectAttributes.addFlashAttribute("address", address);
            return "redirect:/register";
        }

        // 3. Проверка, не занят ли email
        if (customerDAO.getCustomerByEmail(email) != null) {
            redirectAttributes.addFlashAttribute("emailExistsError", "Пользователь с таким email уже существует.");
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("phone", phone);
            redirectAttributes.addFlashAttribute("address", address);
            return "redirect:/register";
        }

        // 4. Валидация обязательных полей (хотя HTML required уже есть)
        if (username.trim().isEmpty() || password.trim().isEmpty() || fullName.trim().isEmpty() || email.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("registrationError", "Пожалуйста, заполните все обязательные поля.");
            redirectAttributes.addFlashAttribute("username", username);
            redirectAttributes.addFlashAttribute("fullName", fullName);
            redirectAttributes.addFlashAttribute("email", email);
            redirectAttributes.addFlashAttribute("phone", phone);
            redirectAttributes.addFlashAttribute("address", address);
            return "redirect:/register";
        }


        // Если все проверки пройдены
        // В реальном приложении пароль нужно хешировать!
        // String hashedPassword = passwordEncoder.encode(password);
        // Мы используем простое сравнение, поэтому сохраняем как есть (небезопасно для реальных систем)
        Customer newCustomer = new Customer();
        newCustomer.setUsername(username);
        newCustomer.setPasswordHash(password); // ВНИМАНИЕ: Это заглушка, пароль должен хешироваться
        newCustomer.setFullName(fullName);
        newCustomer.setEmail(email);
        newCustomer.setPhone(phone);
        newCustomer.setAddress(address);

        customerDAO.save(newCustomer);

        redirectAttributes.addFlashAttribute("registrationSuccess", "Регистрация прошла успешно! Теперь вы можете войти.");
        return "redirect:/login";
    }

    // ----- Вход -----
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/"; // Уже авторизован
        }
        return "public/login";
    }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username,
                              @RequestParam String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        if (session.getAttribute("currentUser") != null) {
            return "redirect:/"; // Уже авторизован
        }

        Customer customer = customerDAO.getCustomerByUsername(username);

        if (customer != null && customer.getPasswordHash().equals(password)) { // ВНИМАНИЕ: Прямое сравнение пароля - небезопасно!
            session.setAttribute("currentUser", customer); // Сохраняем весь объект Customer в сессию
            // Если в корзине были товары до логина (мы это не реализовывали, но если бы), здесь их можно было бы привязать к пользователю
            return "redirect:/"; // Редирект на главную
        } else {
            redirectAttributes.addFlashAttribute("loginError", "Неверный логин или пароль.");
            return "redirect:/login";
        }
    }

    // ----- Выход -----
    @PostMapping("/logout") // Лучше POST для выхода
    public String handleLogout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("currentUser"); // Удаляем пользователя из сессии
        session.removeAttribute("cart"); // Также очищаем корзину при выходе
        session.invalidate(); // Можно полностью инвалидировать сессию
        redirectAttributes.addFlashAttribute("logoutSuccess", "Вы успешно вышли из системы.");
        return "redirect:/login";
    }
}