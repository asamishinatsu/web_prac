package ru.msu.cmc.webapp.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assumptions;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CartAndOrderTest extends SeleniumTestBase {

    private static void loginTestUser(WebDriverWait wait) {
        Assumptions.assumeTrue(AuthTest.uniqueUsername != null && !AuthTest.uniqueUsername.contains("System.currentTimeMillis"),
                "Пользователь из AuthTest не был корректно создан.");

        driver.get("http://localhost:8080/");
        try {
            WebElement welcomeMessage = wait.withTimeout(Duration.ofSeconds(3)).until(ExpectedConditions.visibilityOfElementLocated(
                    By.xpath("//span[contains(text(),'Привет, " + AuthTest.uniqueUsername + "')]")
            ));
            if (welcomeMessage.isDisplayed()) return;
        } catch (Exception e) {
            try {
                driver.findElement(By.cssSelector(".main-header form button[type='submit']")).click();
                wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Войти")));
            } catch (Exception logoutException) {}
        }

        driver.get("http://localhost:8080/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(AuthTest.uniqueUsername);
        driver.findElement(By.id("password")).sendKeys(AuthTest.password);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();
        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[contains(text(),'Привет, " + AuthTest.uniqueUsername + "')]")
        ));
    }

    private static void ensureCartIsEmpty(WebDriverWait wait) {
        driver.get("http://localhost:8080/cart");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));

        List<WebElement> removeButtons = driver.findElements(By.cssSelector("table form button.button-danger"));
        while (!removeButtons.isEmpty()) {
            removeButtons.get(0).click();
            try {
                wait.until(ExpectedConditions.stalenessOf(removeButtons.get(0)));
            } catch (Exception e) {
                //stalenessOf может не сработать, если элемент удаляется другим способом
            }
            driver.get("http://localhost:8080/cart"); // Перезагружаем, чтобы точно обновить состояние
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
            removeButtons = driver.findElements(By.cssSelector("table form button.button-danger"));
        }
        assertTrue(driver.getPageSource().contains("Ваша корзина пуста"), "Не удалось очистить корзину");
    }

    @BeforeAll
    public static void setupCartTestsGlobal() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        loginTestUser(wait);
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
            Assumptions.assumeTrue(false, "Нет книг на главной для теста");
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
            addressInput.sendKeys("Тестовый адрес Selenium для заказа");
        }

        driver.findElement(By.xpath("//button[contains(text(),'Подтвердить и заказать')]")).click();
        wait.until(ExpectedConditions.urlContains("/order/"));

        driver.get("http://localhost:8080/cart");
        wait.until(ExpectedConditions.textToBePresentInElementLocated(By.tagName("body"), "Ваша корзина пуста"));
    }
}