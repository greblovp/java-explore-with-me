package ru.practicum.ewm.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class EventRequestStatusUpdateRequest {
    List<Long> requestIds;
    String status;
    //<status>
}
