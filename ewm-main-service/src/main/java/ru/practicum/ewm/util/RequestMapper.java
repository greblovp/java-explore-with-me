package ru.practicum.ewm.util;

import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.enm.RequestStatus;
import ru.practicum.ewm.model.ParticipationRequest;

import java.util.ArrayList;
import java.util.List;

public class RequestMapper {
    public static ParticipationRequestDto toRequestDto(ParticipationRequest request) {
        return ParticipationRequestDto.builder()
                .id(request.getId())
                .event(request.getEventId())
                .requester(request.getRequesterId())
                .status(request.getStatus().name())
                .created(request.getCreated().toString())
                .build();
    }

    public static List<ParticipationRequestDto> toRequestDto(Iterable<ParticipationRequest> requests) {
        List<ParticipationRequestDto> dtos = new ArrayList<>();
        for (ParticipationRequest request : requests) {
            dtos.add(toRequestDto(request));
        }
        return dtos;
    }

    public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(List<ParticipationRequest> updatedRequests) {
        EventRequestStatusUpdateResult dto = EventRequestStatusUpdateResult.builder()
                .confirmedRequests(new ArrayList<>())
                .rejectedRequests(new ArrayList<>())
                .build();
        for (ParticipationRequest request : updatedRequests) {
            if (request.getStatus() == RequestStatus.CONFIRMED) {
                dto.getConfirmedRequests().add(toRequestDto(request));
            } else if (request.getStatus() == RequestStatus.REJECTED) {
                dto.getRejectedRequests().add(toRequestDto(request));
            }
        }
        return dto;
    }
}
