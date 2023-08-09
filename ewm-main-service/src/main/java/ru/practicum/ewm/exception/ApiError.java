package ru.practicum.ewm.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Data
@Builder
public class ApiError {
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    private List<String> errors;
    private String message;
    private String reason;
    private HttpStatus status;
    private String timestamp = LocalDateTime.now().format(dtFormatter);
}