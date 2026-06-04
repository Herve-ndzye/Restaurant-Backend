package com.mavic.backend.common.enums;

import java.util.Arrays;

public enum Category {
    STARTER,
    MAIN,
    DESSERT,
    DRINK,
    SIDE;

    public static boolean exists(String value) {
        return Arrays.stream(Category.values())
                .anyMatch(c -> c.name().equalsIgnoreCase(value));
    }
}
