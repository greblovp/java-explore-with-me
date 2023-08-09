package ru.practicum.ewm.util;

import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.EventShortDto;
import ru.practicum.ewm.dto.event.NewEventDto;
import ru.practicum.ewm.enm.EventState;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.Location;
import ru.practicum.ewm.model.User;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventMapper {
    public static Event toEvent(NewEventDto newEventDto, Category category, User initiator, Location location) {
        Event event = new Event();
        event.setAnnotation(newEventDto.getAnnotation());
        event.setCategory(category);
        event.setDescription(newEventDto.getDescription());
        event.setEventDate(DateFormatter.stringToDate(newEventDto.getEventDate()));
        event.setLocation(location);
        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        } else {
            event.setPaid(newEventDto.getPaid());
        }
        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        } else {
            event.setParticipantLimit(newEventDto.getParticipantLimit());
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        } else {
            event.setRequestModeration(newEventDto.getRequestModeration());
        }
        event.setTitle(newEventDto.getTitle());

        event.setState(EventState.PENDING);
        event.setInitiator(initiator);
        event.setCreatedOn(LocalDateTime.now());

        return event;
    }

    public static EventShortDto toEventShortDto(Event event) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .eventDate(DateFormatter.dateToString(event.getEventDate()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.getPaid())
                .build();
    }

    public static List<EventShortDto> toEventShortDto(Iterable<Event> events, Map<Long, Long> viewsMap,
                                                      Map<Long, Long> confirmedRequestsMap) {
        List<EventShortDto> dtos = new ArrayList<>();
        for (Event event : events) {
            dtos.add(toEventShortDto(event));
        }
        for (EventShortDto dto : dtos) {
            Long eventId = dto.getId();
            if (confirmedRequestsMap.containsKey(eventId)) {
                dto.setConfirmedRequests(confirmedRequestsMap.get(eventId));
            }
            if (viewsMap.containsKey(eventId)) {
                dto.setViews(viewsMap.get(eventId));
            }
        }
        return dtos;
    }

    public static EventFullDto toEventFullDto(Event event) {
        return EventFullDto.builder()
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .createdOn(DateFormatter.dateToString(event.getCreatedOn()))
                .description(event.getDescription())
                .eventDate(DateFormatter.dateToString(event.getEventDate()))
                .id(event.getId())
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocationMapper.toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(DateFormatter.dateToString(event.getPublishedOn()))
                .requestModeration(event.getRequestModeration())
                .state(event.getState().toString())
                .title(event.getTitle())
                .confirmedRequests(0L)
                .views(0L)
                .build();
    }

    public static EventFullDto toEventFullDto(Event event, Map<Long, Long> viewsMap, Map<Long, Long> confirmedRequestsMap) {
        EventFullDto dto = toEventFullDto(event);
        if (viewsMap.containsKey(event.getId())) {
            dto.setViews(viewsMap.get(event.getId()));
        }
        if (confirmedRequestsMap.containsKey(event.getId())) {
            dto.setConfirmedRequests(confirmedRequestsMap.get(event.getId()));
        }
        return dto;
    }

    public static List<EventFullDto> toEventFullDto(Iterable<Event> events, Map<Long, Long> viewsMap, Map<Long, Long> confirmedRequestsMap) {
        List<EventFullDto> dtos = new ArrayList<>();
        for (Event event : events) {
            dtos.add(toEventFullDto(event));
        }
        for (EventFullDto dto : dtos) {
            Long eventId = dto.getId();
            if (confirmedRequestsMap.containsKey(eventId)) {
                dto.setConfirmedRequests(confirmedRequestsMap.get(eventId));
            }
            if (viewsMap.containsKey(eventId)) {
                dto.setViews(viewsMap.get(eventId));
            }
        }
        return dtos;
    }
}
