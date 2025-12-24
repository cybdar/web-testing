package ru.netology.webtesting;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import io.github.bonigarcia.wdm.WebDriverManager;

import static org.junit.jupiter.api.Assertions.*;

class CardOrderTest {
    private WebDriver driver;
    private final String baseUrl = "http://localhost:9999";

    @BeforeAll
    static void setupAll() {
        WebDriverManager.chromedriver().setup();
    }

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--no-sandbox");
        options.addArguments("--headless");
        driver = new ChromeDriver(options);
        driver.get(baseUrl);
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void fillForm(String city, String date, String name, String phone, boolean agree) {
        driver.findElement(By.cssSelector("[data-test-id=city] input")).sendKeys(city);

        WebElement dateInput = driver.findElement(By.cssSelector("[data-test-id=date] input"));
        dateInput.clear();
        dateInput.sendKeys(date);

        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys(name);
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys(phone);

        if (agree) {
            driver.findElement(By.cssSelector("[data-test-id=agreement]")).click();
        }

        driver.findElement(By.cssSelector("button")).click();
    }

    private String getSuccessMessage() {
        return driver.findElement(By.cssSelector("[data-test-id=order-success]"))
                .getText().trim();
    }

    private String getErrorForField(String fieldId) {
        return driver.findElement(By.cssSelector(String.format("[data-test-id=%s].input_invalid .input__sub", fieldId)))
                .getText().trim();
    }

    @Test
    @DisplayName("Успешная отправка формы с валидными данными")
    void shouldSubmitFormWithValidData() {
        fillForm("Москва", DataGenerator.generateDate(3),
                "Иванов-Петров Иван", "+79270000000", true);

        String actualText = getSuccessMessage();
        assertEquals("Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.",
                actualText);
    }

    @Test
    @DisplayName("Ошибка при вводе имени латинскими буквами")
    void shouldShowErrorForInvalidName() {
        fillForm("Санкт-Петербург", DataGenerator.generateDate(5),
                "John Smith", "+79270000000", true);

        String errorText = getErrorForField("name");
        assertEquals("Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.",
                errorText);
    }

    @Test
    @DisplayName("Ошибка при вводе некорректного телефона")
    void shouldShowErrorForInvalidPhone() {
        fillForm("Казань", DataGenerator.generateDate(7),
                "Сидоров Петр", DataGenerator.generateInvalidPhone(), true);

        String errorText = getErrorForField("phone");
        assertEquals("Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.",
                errorText);
    }

    @Test
    @DisplayName("Ошибка при незаполненном чекбоксе согласия")
    void shouldShowErrorWithoutAgreement() {
        fillForm("Новосибирск", DataGenerator.generateDate(10),
                "Кузнецова Мария", "+79270000000", false);

        WebElement agreementCheckbox = driver.findElement(By.cssSelector("[data-test-id=agreement]"));
        assertTrue(agreementCheckbox.getAttribute("class").contains("input_invalid"));
    }

    @Test
    @DisplayName("Ошибка при пустом поле города")
    void shouldShowErrorForEmptyCity() {
        WebElement dateInput = driver.findElement(By.cssSelector("[data-test-id=date] input"));
        dateInput.clear();
        dateInput.sendKeys(DataGenerator.generateDate(3));

        driver.findElement(By.cssSelector("[data-test-id=name] input")).sendKeys("Федоров Алексей");
        driver.findElement(By.cssSelector("[data-test-id=phone] input")).sendKeys("+79270000000");
        driver.findElement(By.cssSelector("[data-test-id=agreement]")).click();
        driver.findElement(By.cssSelector("button")).click();

        String errorText = getErrorForField("city");
        assertEquals("Поле обязательно для заполнения", errorText);
    }
}