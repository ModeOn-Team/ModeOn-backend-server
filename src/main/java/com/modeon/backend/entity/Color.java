package com.modeon.backend.entity;

import lombok.Getter;

@Getter
public enum Color {
    WHITE("#FFFFFF"),
    BLACK("#000000"),
    GRAY("#808080"),
    BEIGE("#F5F5DC"),
    BROWN("#8B4513"),
    NAVY("#000080"),
    BLUE("#0000FF"),
    SKY_BLUE("#87CEEB"),
    GREEN("#008000"),
    KHAKI("#78866B"),
    YELLOW("#FFFF00"),
    ORANGE("#FFA500"),
    RED("#FF0000"),
    PINK("#FFC0CB"),
    PURPLE("#800080"),
    IVORY("#FFFFF0"),
    GOLD("#FFD700"),
    SILVER("#C0C0C0");

    private final String hex;

    Color(String hex) {
        this.hex = hex;
    }
}