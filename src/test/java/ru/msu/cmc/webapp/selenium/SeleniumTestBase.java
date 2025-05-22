package ru.msu.cmc.webapp.selenium;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.time.Duration;

public abstract class SeleniumTestBase {

    protected static WebDriver driver;

    @BeforeAll
    public static void setUpClass() {
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // GUI
        options.addArguments("--window-size=1280,1024");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
    }

    @AfterAll
    public static void tearDownClass() {
        if (driver != null) {
            driver.quit();
        }
    }
}