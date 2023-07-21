package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatDto;

import java.util.Collection;
import java.util.List;

public interface EndpointHitService {
    EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto);

    Collection<ViewStatDto> getEndpointHits(String start, String end, List<String> uris, Boolean unique);
}
