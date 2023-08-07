package ru.practicum.ewm.model;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStat {
    private Long eventId;
    private Long confirmedRequests;
}
