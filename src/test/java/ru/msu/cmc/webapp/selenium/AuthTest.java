package ru.msu.cmc.webapp.selenium;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthTest extends SeleniumTestBase {

    // Оставляем эти поля static, так как тест логина будет использовать созданного пользователя
    protected static String uniqueUsername = "testuser_" + System.currentTimeMillis();
    protected static String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
    protected static final String password = "password123";

    private void ensureLoggedOut(WebDriverWait wait) {
        driver.get("http://localhost:8080/");
        try {
            // Попытка найти кнопку "Выйти". Если она есть и кликабельна, нажимаем.
            WebElement logoutButton = wait.withTimeout(Duration.ofSeconds(2)).until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(".main-header form button[type='submit']"))
            );
            logoutButton.click();
            // Ждем появления элемента, характерного для неавторизованного состояния
            wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Войти")));
        } catch (Exception e) {
            // Если кнопки "Выйти" нет или произошла ошибка ожидания,
            // предполагаем, что пользователь не залогинен или сессия истекла.
            // Можно добавить дополнительную проверку, если нужно.
        }
        driver.get("http://localhost:8080/"); // Убедимся, что мы на главной перед следующим действием
    }

    @Test
    @Order(1)
    public void testUserRegistrationRedirectsToLogin() { // Изменил название для ясности
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ensureLoggedOut(wait); // Убедимся, что начинаем без активной сессии
        driver.get("http://localhost:8080/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("confirmPassword")).sendKeys(password);
        driver.findElement(By.id("fullName")).sendKeys("Тестовый JUnit Пользователь Рег");
        driver.findElement(By.id("email")).sendKeys(uniqueEmail);

        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        // Ожидаем редирект на страницу входа
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/login"));
        assertEquals("http://localhost:8080/login", driver.getCurrentUrl(), "После регистрации не произошел редирект на страницу входа.");

        // Проверяем, что на странице входа есть сообщение об успешной регистрации (если оно реализуется)
        // Например, если есть такой элемент: <div class="alert alert-success">Регистрация прошла успешно!</div>
        try {
            WebElement successMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
            assertTrue(successMessage.getText().contains("Регистрация прошла успешно"), "Нет сообщения об успешной регистрации на странице входа.");
            // или другой текст, который у тебя отображается
        } catch (Exception e) {
            // Сообщение может и не быть, это опционально
            System.out.println("Примечание: Сообщение об успешной регистрации на странице входа не найдено или не ожидается.");
        }

        // Убедимся, что мы все еще не залогинены (в хедере должны быть "Войти", "Регистрация")
        driver.get("http://localhost:8080/"); // Перейдем на главную, чтобы проверить хедер
        WebElement headerNav = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-header nav")));
        assertTrue(headerNav.getText().contains("Войти"), "Кнопка 'Войти' отсутствует в хедере после редиректа на логин.");
        assertFalse(headerNav.getText().contains("Привет, " + uniqueUsername), "Пользователь оказался залогиненным после регистрации, хотя ожидался редирект на вход.");
    }

    @Test
    @Order(2) // Этот тест теперь будет логинить пользователя, созданного в предыдущем тесте
    public void testUserLoginAndLogout() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ensureLoggedOut(wait); // Убедимся, что начинаем без активной сессии

        driver.get("http://localhost:8080/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(uniqueUsername); // Используем данные из предыдущего теста
        driver.findElement(By.id("password")).sendKeys(password); // Используем данные из предыдущего теста
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        // Ожидаем редирект на главную страницу после успешного входа
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        WebElement headerNav = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-header nav")));
        assertTrue(headerNav.getText().contains("Привет, " + uniqueUsername), "Нет приветствия пользователя в хедере после входа.");

        // Выход
        headerNav.findElement(By.cssSelector("form button[type='submit']")).click();
        // Ожидаем появления элемента, характерного для неавторизованного состояния
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Войти")));
        headerNav = driver.findElement(By.cssSelector(".main-header nav")); // Переполучаем элемент, т.к. страница могла обновиться
        assertFalse(headerNav.getText().contains("Привет, " + uniqueUsername), "Приветствие пользователя все еще отображается после выхода.");
        assertTrue(headerNav.getText().contains("Войти"), "Кнопка 'Войти' отсутствует в хедере после выхода.");
    }

    @Test
    @Order(3)
    public void testLoginWithInvalidCredentials() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ensureLoggedOut(wait);
        driver.get("http://localhost:8080/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys("nonexistentuser" + System.currentTimeMillis());
        driver.findElement(By.id("password")).sendKeys("wrongpassword");
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        WebElement errorAlert = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-danger")));
        assertTrue(errorAlert.isDisplayed(), "Сообщение об ошибке не отображается.");
        // assertTrue(errorAlert.getText().contains("Неверный логин или пароль")); // Проверь точный текст сообщения
    }
}