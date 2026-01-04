package ru.netology.webtesting;

public class DataGenerator {

    public static String generateValidPhone() {
        return "+79270000000";
    }

    public static String generateInvalidPhone() {
        return "+7927000000"; // 10 цифр вместо 11
    }
}