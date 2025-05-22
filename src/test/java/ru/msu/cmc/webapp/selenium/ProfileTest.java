package ru.msu.cmc.webapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assumptions;


import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProfileTest extends SeleniumTestBase {

    private static String profileTestUsername = "profile_user_" + System.currentTimeMillis();
    private static String profileTestEmail = "profile_" + System.currentTimeMillis() + "@example.com";
    private static final String profileTestPassword = "profilePassword123";

    // Вспомогательный метод для регистрации и логина пользователя для этого класса тестов
    private static void registerAndLoginUserForProfileTests(WebDriverWait wait) {
        // 1. Регистрация
        driver.get("http://localhost:8080/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(profileTestUsername);
        driver.findElement(By.id("password")).sendKeys(profileTestPassword);
        driver.findElement(By.id("confirmPassword")).sendKeys(profileTestPassword);
        driver.findElement(By.id("fullName")).sendKeys("Profile Test User");
        driver.findElement(By.id("email")).sendKeys(profileTestEmail);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        // Ожидаем редирект на страницу входа после регистрации
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/login"));
        System.out.println("ProfileTest: Пользователь " + profileTestUsername + " зарегистрирован, переход на страницу логина.");


        // 2. Логин
        // Убедимся, что поля ввода логина и пароля доступны
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        usernameInput.clear(); // Очищаем поле, если там что-то осталось от предыдущих действий
        usernameInput.sendKeys(profileTestUsername);

        WebElement passwordInput = driver.findElement(By.id("password"));
        passwordInput.clear();
        passwordInput.sendKeys(profileTestPassword);

        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        // Ожидаем редирект на главную и приветствие
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        WebElement headerNav = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-header nav")));
        assertTrue(headerNav.getText().contains("Привет, " + profileTestUsername), "Нет приветствия пользователя в хедере после входа.");
        System.out.println("Пользователь " + profileTestUsername + " успешно вошел в систему для ProfileTest.");
    }


    @BeforeAll
    public static void setupProfileTestsGlobal() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        registerAndLoginUserForProfileTests(wait); // Регистрируем и логиним своего пользователя
    }

    @Test
    @Order(1)
    public void testOpenProfilePage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:8080/profile");
        wait.until(ExpectedConditions.titleIs("Книжный магазин - Личный кабинет"));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(h1.getText().contains("Личный кабинет"));
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        assertEquals(profileTestUsername, usernameField.getAttribute("value"));
    }

    @Test
    @Order(2)
    public void testUpdateUserProfile() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:8080/profile");
        String newFullName = "Обновленное JUnit Имя (Профиль) " + System.currentTimeMillis();

        WebElement fullNameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("fullName")));
        fullNameInput.clear();
        fullNameInput.sendKeys(newFullName);

        driver.findElement(By.xpath("//button[contains(text(),'Сохранить изменения')]")).click();

        WebElement successAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertTrue(successAlert.isDisplayed());
        assertEquals(newFullName, driver.findElement(By.id("fullName")).getAttribute("value"));
    }

    @Test
    @Order(3)
    public void testOrderHistoryIsPresent() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:8080/profile");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//h2[contains(text(),'История заказов')]")));

        // Этот тест теперь не может полагаться на заказ из CartAndOrderTest,
        // так как пользователь другой. Он просто проверит, что секция истории есть.
        // Если нужно проверить наличие заказов, их нужно будет создать для profileTestUser.
        WebElement orderHistorySection = driver.findElement(By.xpath("//div[./h2[contains(text(),'История заказов')]]"));
        assertTrue(orderHistorySection.isDisplayed());

        // Проверяем, есть ли сообщение "У вас пока нет заказов" или таблица
        boolean noOrdersMessageVisible = false;
        try {
            WebElement noOrdersMessage = orderHistorySection.findElement(By.xpath(".//p[contains(text(),'У вас пока нет заказов')]"));
            noOrdersMessageVisible = noOrdersMessage.isDisplayed();
        } catch (NoSuchElementException e) {
            // Сообщения нет, значит, должна быть таблица (или ошибка, если нет ни того, ни другого)
        }

        boolean tableVisible = false;
        try {
            WebElement ordersTable = orderHistorySection.findElement(By.tagName("table"));
            tableVisible = ordersTable.isDisplayed();
        } catch (NoSuchElementException e) {
            // Таблицы нет
        }
        assertTrue(noOrdersMessageVisible || tableVisible, "В секции истории заказов нет ни сообщения об отсутствии заказов, ни таблицы заказов.");
    }
}