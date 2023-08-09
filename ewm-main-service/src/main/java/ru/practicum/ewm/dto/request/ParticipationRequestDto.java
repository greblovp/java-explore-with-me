package ru.practicum.ewm.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ParticipationRequestDto {
    private String created;
    private Long event;
    private Long id;
    private Long requester;
    private String status;
}
