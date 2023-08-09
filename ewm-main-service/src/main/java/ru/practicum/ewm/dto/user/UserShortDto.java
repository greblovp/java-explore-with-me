package ru.practicum.ewm.dto.user;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserShortDto {
    @NotNull
    private Long id;
    @NotNull
    private String name;
}
