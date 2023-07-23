package ru.practicum.ewm.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ViewStat {
    private String app;
    private String uri;
    private Long hits;
}