package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Data
@Builder
public class EndpointHitDto {
    @NotBlank
    @NotNull
    private String app;

    @NotBlank
    @NotNull
    private String uri;

    @NotBlank
    @NotNull
    private String ip;

    @NotNull
    private String timestamp;
}
