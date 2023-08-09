package ru.practicum.ewm.dto.event;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import ru.practicum.ewm.dto.location.LocationDto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class NewEventDto {
    @NotBlank
    @Length(min = 20, max = 2000)
    private String annotation;
    @NotNull
    private Long category;
    @NotBlank
    @Length(min = 20, max = 7000)
    private String description;
    @NotNull
    private String eventDate;
    @NotNull
    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    @NotBlank
    @Length(min = 3, max = 120)
    private String title;
}
