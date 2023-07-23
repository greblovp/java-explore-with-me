package ru.practicum.ewm.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ViewStatDto {
    private String app;
    private String uri;
    private Long hits;
}
