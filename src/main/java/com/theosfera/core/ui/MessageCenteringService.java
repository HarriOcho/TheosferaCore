package com.theosfera.core.ui;

public final class MessageCenteringService {

    private static final int CENTER_PX = 154;
    private static final int SPACE_WIDTH = 4;

    private MessageCenteringService() {
    }

    public static String center(final String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        final int messagePxSize = getMessagePxSize(message);
        final int spaces = Math.max(0, (CENTER_PX - messagePxSize / 2) / SPACE_WIDTH);

        return " ".repeat(spaces) + message;
    }

    private static int getMessagePxSize(final String message) {
        int size = 0;
        boolean bold = false;
        boolean colorCode = false;

        for (final char character : message.toCharArray()) {
            if (character == '§' || character == '&') {
                colorCode = true;
                continue;
            }

            if (colorCode) {
                bold = character == 'l' || character == 'L';
                colorCode = false;
                continue;
            }

            size += getCharWidth(character);

            if (bold && character != ' ') {
                size++;
            }
        }

        return size;
    }

    private static int getCharWidth(final char character) {
        return switch (character) {
            case ' ', 'i', '!', '.', ',', ':', ';', '|', '\'' -> 2;
            case 'l', '`' -> 3;
            case 't', 'I', '[', ']', '"' -> 4;
            case 'f', 'k', '{', '}' -> 5;
            default -> 6;
        };
    }
}