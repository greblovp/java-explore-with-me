package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.event.EventFullDto;
import ru.practicum.ewm.dto.event.UpdateEventAdminRequest;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.IncorrectRequestException;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.util.DateFormatter;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/events")
@RequiredArgsConstructor
@Slf4j
public class EventAdminController {
    private final EventService eventService;

    @GetMapping
    public Collection<EventFullDto> getAdminEvents(@RequestParam(required = false) List<Long> users,
                                              @RequestParam(required = false) List<String> states,
                                              @RequestParam(required = false) List<Long> categories,
                                              @RequestParam(required = false) String rangeStart,
                                              @RequestParam(required = false) String rangeEnd,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "10") Integer size) {
        log.info("Поиск событийПараметры: users={}, states={}, categories={}, rangeStart={}," +
                " rangeEnd={}, from={}, size={}",
                users, states, categories, rangeStart, rangeEnd, from, size);
        return eventService.getAdminEvents(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto updateAdminEvent(@PathVariable Long eventId, @RequestBody UpdateEventAdminRequest updateEvent) {
        log.info("Изменение события: eventId={}, eventDto={}", eventId, updateEvent);
        checkEventDtoDate(updateEvent.getEventDate());
        return eventService.updateAdminEvent(eventId, updateEvent);
    }

    private void checkEventDtoDate(String stringDate) {
        if (stringDate == null) {
            return;
        }
        LocalDateTime eventDate = DateFormatter.stringToDate(stringDate);
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1)) && eventDate.isAfter(LocalDateTime.now())) {
            throw new ConditionsNotMetException("eventDate должно быть минимум на 1 час позже текущего времени. " +
                    "Текущее значение - " + eventDate);
        }
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw new IncorrectRequestException("eventDate не может быть в прошлом " +
                    "Текущее значение - " + eventDate);
        }
    }
}
