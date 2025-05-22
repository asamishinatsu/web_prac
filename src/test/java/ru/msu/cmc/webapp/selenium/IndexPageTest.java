package ru.msu.cmc.webapp.selenium;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.junit.jupiter.api.Assumptions;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IndexPageTest extends SeleniumTestBase {

    @Test
    public void testOpenIndexPageAndVerifyTitle() {
        driver.get("http://localhost:8080/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.titleIs("Книжный магазин - Каталог книг"));
        assertEquals("Книжный магазин - Каталог книг", driver.getTitle());
        WebElement h1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h1")));
        assertTrue(h1.getText().contains("Каталог книг"));
    }

    @Test
    public void testSearchBookByTitle() {
        driver.get("http://localhost:8080/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement titleInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("titleQuery")));
        titleInput.sendKeys("1984");

        WebElement searchButton = driver.findElement(By.cssSelector(".filter-buttons button[type='submit']"));
        searchButton.click();

        WebElement bookList = wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("book-list")));
        assertTrue(bookList.getText().contains("1984"), "Книга '1984' не найдена после поиска по названию");
    }

    @Test
    public void testBookDetailsLink() {
        driver.get("http://localhost:8080/");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        WebElement firstBookCard;
        try {
            firstBookCard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".book-list .book-card:first-child")));
        } catch (Exception e) {
            Assumptions.assumeTrue(false, "Нет книг на главной для теста testBookDetailsLink");
            return;
        }

        WebElement detailsButton = firstBookCard.findElement(By.cssSelector(".actions a.button"));
        String bookTitleInCard = firstBookCard.findElement(By.tagName("h3")).getText();

        detailsButton.click();

        wait.until(ExpectedConditions.urlContains("/books/"));
        assertTrue(driver.getCurrentUrl().contains("/books/"), "URL не содержит /books/");

        WebElement bookTitleOnPage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".book-info h1")));
        assertEquals(bookTitleInCard, bookTitleOnPage.getText(), "Названия книг не совпадают");
    }
}