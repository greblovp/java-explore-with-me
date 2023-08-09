package ru.practicum.ewm.dto.category;

import javax.validation.constraints.Size;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private Long id;

    @Size(min = 1, max = 50)
    private String name;
}
