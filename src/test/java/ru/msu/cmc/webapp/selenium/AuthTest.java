package ru.msu.cmc.webapp.selenium;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthTest extends SeleniumTestBase {

    protected static String uniqueUsername = "testuser_" + System.currentTimeMillis();
    protected static String uniqueEmail = "test_" + System.currentTimeMillis() + "@example.com";
    protected static final String password = "password123";

    private void ensureLoggedOut(WebDriverWait wait) {
        driver.get("http://localhost:8080/");
        try {
            WebElement logoutButton = wait.withTimeout(Duration.ofSeconds(2)).until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(".main-header form button[type='submit']"))
            );
            logoutButton.click();
            wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Войти")));
        } catch (Exception e) {
            // Not logged in
        }
        driver.get("http://localhost:8080/");
    }

    @Test
    @Order(1)
    public void testUserRegistrationSuccess() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ensureLoggedOut(wait);
        driver.get("http://localhost:8080/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.id("confirmPassword")).sendKeys(password);
        driver.findElement(By.id("fullName")).sendKeys("Тестовый JUnit Пользователь");
        driver.findElement(By.id("email")).sendKeys(uniqueEmail);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        WebElement headerNav = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-header nav")));
        assertTrue(headerNav.getText().contains("Привет, " + uniqueUsername));
    }

    @Test
    @Order(2)
    public void testUserLoginAndLogout() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        ensureLoggedOut(wait);

        driver.get("http://localhost:8080/login");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).sendKeys(uniqueUsername);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("form button[type='submit']")).click();

        wait.until(ExpectedConditions.urlToBe("http://localhost:8080/"));
        WebElement headerNav = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".main-header nav")));
        assertTrue(headerNav.getText().contains("Привет, " + uniqueUsername));

        headerNav.findElement(By.cssSelector("form button[type='submit']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.linkText("Войти")));
        headerNav = driver.findElement(By.cssSelector(".main-header nav"));
        assertFalse(headerNav.getText().contains("Привет, " + uniqueUsername));
        assertTrue(headerNav.getText().contains("Войти"));
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
        assertTrue(errorAlert.isDisplayed());
    }
}