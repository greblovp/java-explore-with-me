package ru.practicum.ewm.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatDto;
import ru.practicum.ewm.service.EndpointHitService;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceController {
    private final EndpointHitService endpointHitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public EndpointHitDto createEndpointHit(@RequestBody EndpointHitDto endpointHitDto) {
        log.info("Сохранение информации о том, что к эндпоинту был запрос - {}", endpointHitDto);
        return endpointHitService.createEndpointHit(endpointHitDto);
    }

    @GetMapping("/stats")
    public Collection<ViewStatDto> getBookings(@RequestParam String start,
                                               @RequestParam String end,
                                               @RequestParam(required = false) List<String> uris,
                                               @RequestParam(defaultValue = "false") Boolean unique) {
        log.info("Получение статистики по посещениям. Дата и время начала - {}. Дата и время конца - {}. " +
                "Список uri - {}. Нужно ли учитывать только уникальные посещения - {}", start, end, uris, unique);
        return endpointHitService.getEndpointHits(start, end, uris, unique);
    }
}
