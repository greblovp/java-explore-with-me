package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.IncorrectRequestException;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.ewm.util.CheckRequest;
import ru.practicum.ewm.util.DateFormatter;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
public class EventPrivateController {
    private final EventService eventService;
    private final RequestService requestService;

    @GetMapping
    public Collection<EventShortDto> getUserEvents(@PathVariable Long userId, @RequestParam(defaultValue = "0") Integer from,
                                                       @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение событий, добавленных пользователем: {}. Параметры: from={}, size={}", userId, from, size);
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId, @RequestBody @Valid NewEventDto newEventDto,
                                    BindingResult bindingResult) {
        log.info("Добавление нового события: {}. Пользователь: {}", newEventDto, userId);
        CheckRequest.check("Событие - " + newEventDto, bindingResult);
        checkEventDtoDate(newEventDto.getEventDate());
        return eventService.createEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getUserEventById(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получение полной информации о событии: {}. Пользователь: {}", eventId, userId);
        return eventService.getUserEventById(userId, eventId);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateUserEvent(@PathVariable Long userId, @PathVariable Long eventId,
                                    @RequestBody UpdateEventUserRequest updateEvent) {
        log.info("Изменение события: {}. Пользователь: {}. Данные изменения: {}", eventId, userId, updateEvent);
        checkEventDtoDate(updateEvent.getEventDate());
        return eventService.updateUserEvent(userId, eventId, updateEvent);
    }

    @GetMapping("/{eventId}/requests")
    public Collection<ParticipationRequestDto> getEventRequests(@PathVariable Long userId, @PathVariable Long eventId) {
        log.info("Получение запросов на участие в событии: {}. Пользователь: {}", eventId, userId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId, @PathVariable Long eventId,
                                                              @RequestBody EventRequestStatusUpdateRequest request) {
        log.info("Изменение статуса запроса на участие в событии {}. Пользователь: {}. Данные по заявкам: {}",
                eventId, userId, request);
        return requestService.updateRequestStatus(userId, eventId, request);
    }

    private void checkEventDtoDate(String stringDate) {
        if (stringDate == null) {
            return;
        }
        LocalDateTime eventDate = DateFormatter.stringToDate(stringDate);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(2)) && eventDate.isAfter(LocalDateTime.now())) {
            throw new ConditionsNotMetException("eventDate должно быть минимум на 2 часа позже текущего времени. " +
                    "Текущее значение - " + eventDate);
        }
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IncorrectRequestException("eventDate не может быть в прошлом " +
                    "Текущее значение - " + eventDate);
        }
    }
}
