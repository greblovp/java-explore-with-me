package ru.practicum.ewm.dto.user;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    @NotNull
    private String email;
    private Long id;
    @NotNull
    private String name;
}
