package ru.practicum.ewm.enm;

import java.util.Optional;

public enum CommentSort {
    CREATED_DATE,
    UPDATED_DATE,
    EVENT_ID,
    ID;

    public static Optional<CommentSort> from(String stringSort) {
        for (CommentSort sort : values()) {
            if (sort.name().equalsIgnoreCase(stringSort)) {
                return Optional.of(sort);
            }
        }
        return Optional.empty();
    }
}
