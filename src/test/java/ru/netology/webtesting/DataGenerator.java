package ru.netology.webtesting;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DataGenerator {

    public static String generateDate(int days) {
        return LocalDate.now().plusDays(days)
                .format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
    }

    public static String generateInvalidPhone() {
        return "+7927000000"; // 10 цифр вместо 11
    }
}