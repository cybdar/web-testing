package ru.netology.webtesting;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class CardOrderTest {
    private WebDriver driver;
    private WebDriverWait wait;
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
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));

        driver.get(baseUrl);

        // Ждем загрузки формы (ждем хотя бы одно поле)
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-test-id=name] input")));
    }

    @AfterEach
    void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }

    private void fillForm(String name, String phone, boolean agree) {
        WebElement nameInput = driver.findElement(By.cssSelector("[data-test-id=name] input"));
        nameInput.clear();
        if (name != null && !name.isEmpty()) {
            nameInput.sendKeys(name);
        }

        WebElement phoneInput = driver.findElement(By.cssSelector("[data-test-id=phone] input"));
        phoneInput.clear();
        if (phone != null && !phone.isEmpty()) {
            phoneInput.sendKeys(phone);
        }

        WebElement agreement = driver.findElement(By.cssSelector("[data-test-id=agreement]"));
        boolean isChecked = agreement.isSelected();

        if (agree && !isChecked) {
            agreement.click();
        } else if (!agree && isChecked) {
            agreement.click();
        }

        driver.findElement(By.cssSelector("button")).click();
    }

    private String getSuccessMessage() {
        // Ждем появления сообщения об успехе
        return wait.until(ExpectedConditions.presenceOfElementLocated(
                        By.cssSelector("[data-test-id=order-success]")))
                .getText().trim();
    }

    private String getErrorForField(String fieldId) {
        // Ждем пока поле станет невалидным
        wait.until(ExpectedConditions.attributeContains(
                By.cssSelector(String.format("[data-test-id=%s]", fieldId)),
                "class", "input_invalid"));

        return driver.findElement(By.cssSelector(
                        String.format("[data-test-id=%s].input_invalid .input__sub", fieldId)))
                .getText().trim();
    }

    @Test
    @DisplayName("Успешная отправка формы с валидными данными")
    void shouldSubmitFormWithValidData() {
        fillForm("Иванов Иван", "+79270000000", true);

        String actualText = getSuccessMessage();
        String expectedText = "Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.";

        assertEquals(expectedText, actualText);
    }

    @Test
    @DisplayName("Ошибка при вводе имени латинскими буквами")
    void shouldShowErrorForInvalidName() {
        fillForm("John Smith", "+79270000000", true);

        String errorText = getErrorForField("name");
        String expectedError = "Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы.";

        assertEquals(expectedError, errorText);
    }

    @Test
    @DisplayName("Ошибка при вводе некорректного телефона")
    void shouldShowErrorForInvalidPhone() {
        fillForm("Иванов Иван", "+7927000000", true);

        String errorText = getErrorForField("phone");
        String expectedError = "Телефон указан неверно. Должно быть 11 цифр, например, +79012345678.";

        assertEquals(expectedError, errorText);
    }

    @Test
    @DisplayName("Ошибка при незаполненном чекбоксе согласия")
    void shouldShowErrorWithoutAgreement() {
        fillForm("Иванов Иван", "+79270000000", false);

        // Ждем пока чекбокс станет невалидным
        wait.until(ExpectedConditions.attributeContains(
                By.cssSelector("[data-test-id=agreement]"),
                "class", "input_invalid"));

        WebElement agreementCheckbox = driver.findElement(
                By.cssSelector("[data-test-id=agreement].input_invalid"));
        assertTrue(agreementCheckbox.isDisplayed(),
                "Чекбокс должен быть помечен как невалидный при отсутствии согласия");
    }

    @Test
    @DisplayName("Ошибка при пустом поле имени")
    void shouldShowErrorForEmptyName() {
        // Используем fillForm для пустого имени
        fillForm("", "+79270000000", true);

        String errorText = getErrorForField("name");
        assertEquals("Поле обязательно для заполнения", errorText);
    }

    @Test
    @DisplayName("Ошибка при пустом поле телефона")
    void shouldShowErrorForEmptyPhone() {
        // Используем fillForm для пустого телефона
        fillForm("Иванов Иван", "", true);

        String errorText = getErrorForField("phone");
        assertEquals("Поле обязательно для заполнения", errorText);
    }

    @Test
    @DisplayName("Валидное имя с дефисом принимается")
    void shouldAcceptNameWithHyphen() {
        fillForm("Салтыков-Щедрин Михаил", "+79270000000", true);

        String actualText = getSuccessMessage();
        String expectedText = "Ваша заявка успешно отправлена! Наш менеджер свяжется с вами в ближайшее время.";

        assertEquals(expectedText, actualText);
    }
}