package ru.practicum.ewm.dto.user;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@Builder
public class NewUserRequest {
    @NotBlank
    @Length(min = 6, max = 254)
    @Email
    private String email;
    @NotBlank
    @Length(min = 2, max = 250)
    private String name;
}
