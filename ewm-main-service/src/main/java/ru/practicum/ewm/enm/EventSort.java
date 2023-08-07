package ru.practicum.ewm.enm;

import java.util.Optional;

public enum EventSort {
    EVENT_DATE,
    VIEWS,
    UNSORTED;

    public static Optional<EventSort> from(String stringSort) {
        for (EventSort sort : values()) {
            if (sort.name().equalsIgnoreCase(stringSort)) {
                return Optional.of(sort);
            }
        }
        return Optional.empty();
    }
}
