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

    @BeforeAll
    public static void loginBeforeProfileTestsGlobal() {
        Assumptions.assumeTrue(AuthTest.uniqueUsername != null && !AuthTest.uniqueUsername.contains("System.currentTimeMillis"),
                "Пользователь из AuthTest не был корректно создан.");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

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

    @Test
    @Order(1)
    public void testOpenProfilePage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:8080/profile");
        wait.until(ExpectedConditions.titleIs("Книжный магазин - Личный кабинет"));
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(h1.getText().contains("Личный кабинет"));
        WebElement usernameField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        assertEquals(AuthTest.uniqueUsername, usernameField.getAttribute("value"));
    }

    @Test
    @Order(2)
    public void testUpdateUserProfile() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get("http://localhost:8080/profile");
        String newFullName = "Обновленное JUnit Имя " + System.currentTimeMillis();

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
        try {
            WebElement ordersTable = driver.findElement(By.cssSelector("div.profile-section table"));
            assertTrue(ordersTable.isDisplayed());
        } catch (NoSuchElementException e) {
            Assumptions.assumeTrue(false,"Таблица истории заказов не найдена. Возможно, тест CartAndOrderTest.testAddBookToCartAndCheckout не создал заказ.");
        }
    }
}