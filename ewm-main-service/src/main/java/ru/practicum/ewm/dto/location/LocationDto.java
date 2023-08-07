package ru.practicum.ewm.dto.location;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationDto {
    private Float lat;
    private Float lon;
}
