package ru.msu.cmc.webapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartAndOrderTest extends SeleniumTestBase {

    private static String cartTestUsername = "cart_user_" + System.currentTimeMillis();
    private static String cartTestEmail = "cart_" + System.currentTimeMillis() + "@example.com";
    private static final String cartTestPassword = "cartPassword123";

    // Вспомогательный метод для регистрации и логина пользователя для этого класса тестов
    private static void registerAndLoginUserForCartTests(WebDriverWait wait) {
        // 1. Регистрация
        driver.get("http://localhost:8080/register");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(cartTestUsername);
        driver.findElement(By.id("password")).sendKeys(cartTestPassword);
        driver.findElement(By.id("confirmPassword")).sendKeys(cartTestPassword);
        driver.findElement(By.id("fullName")).sendKeys("Cart Test User");
        driver.findElement(By.id("email")).sendKeys(cartTestEmail);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        // Ожидаем редирект на страницу входа после регистрации
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/login"));

        // 2. Логин
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(cartTestUsername);
        driver.findElement(By.id("password")).sendKeys(cartTestPassword);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        // Ожидаем редирект на главную и приветствие
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        WebElement headerNav = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-header nav")));
        assertTrue(headerNav.getText().contains("Привет, " + cartTestUsername), "Нет приветствия пользователя в хедере после входа.");
        System.out.println("Пользователь " + cartTestUsername + " успешно зарегистрирован и вошел в систему для CartAndOrderTest.");
    }

    private static void ensureCartIsEmpty(WebDriverWait wait) {
        driver.get("http://localhost:8080/cart");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));

        List<WebElement> removeButtons = driver.findElements(By.cssSelector("table form button.button-danger"));
        while (!removeButtons.isEmpty()) {
            removeButtons.get(0).click();
            try {
                wait.until(ExpectedConditions.stalenessOf(removeButtons.get(0)));
            } catch (Exception e) { /* ignore */ }
            driver.get("http://localhost:8080/cart");
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            removeButtons = driver.findElements(By.cssSelector("table form button.button-danger"));
        }
        assertTrue(driver.getPageSource().contains("Ваша корзина пуста"), "Не удалось очистить корзину");
    }

    @BeforeAll
    public static void setupCartTestsGlobal() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        registerAndLoginUserForCartTests(wait); // Регистрируем и логиним своего пользователя
    }

    @BeforeEach
    public void clearCartBeforeEachTest() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ensureCartIsEmpty(wait);
    }

    @Test
    @Order(1)
    public void testAddBookToCartAndCheckout() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:8080/");

        WebElement firstBookCard;
        try {
            firstBookCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".book-list .book-card:first-child")));
        } catch (Exception e) {
            Assumptions.abort("Нет книг на главной для теста testAddBookToCartAndCheckout");
            return;
        }
        String bookTitle = firstBookCard.findElement(By.tagName("h3")).getText();
        WebElement addToCartButton = firstBookCard.findElement(By.cssSelector("form button.button"));
        addToCartButton.click();

        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/cart"));

        WebElement cartTable = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("table")));
        assertTrue(cartTable.getText().contains(bookTitle));
        WebElement quantityInput = driver.findElement(By.cssSelector("table input[name='quantity']"));
        assertEquals("1", quantityInput.getAttribute("value"));

        driver.findElement(By.xpath("//a[contains(text(),'Оформить заказ')]")).click();
        wait.until(ExpectedConditions.titleIs("Книжный магазин - Оформление заказа"));

        WebElement addressInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deliveryAddress")));
        if(addressInput.getAttribute("value").trim().isEmpty()){
            addressInput.sendKeys("Тестовый адрес Selenium для заказа (cart test)");
        }

        driver.findElement(By.xpath("//button[contains(text(),'Подтвердить и заказать')]")).click();
        wait.until(ExpectedConditions.urlContains("/order/"));

        driver.get("http://localhost:8080/cart");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Ваша корзина пуста"));
    }
}