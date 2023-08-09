package ru.practicum.ewm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
