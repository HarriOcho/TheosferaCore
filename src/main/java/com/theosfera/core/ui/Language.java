package com.theosfera.core.ui;

public enum Language {

    SPANISH("es"),
    ENGLISH("en");

    private final String code;

    Language(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Language fromCode(String code) {
        if (code == null || code.isBlank()) {
            return SPANISH;
        }

        String normalized = code.toLowerCase();

        for (Language language : values()) {
            if (language.code.equals(normalized)) {
                return language;
            }
        }

        return SPANISH;
    }

    public static Language fromLocale(String locale) {
        if (locale == null || locale.isBlank()) {
            return ENGLISH;
        }

        String normalized = locale.toLowerCase();

        if (normalized.startsWith("es")) {
            return SPANISH;
        }

        return ENGLISH;
    }
}