package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatDto;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.EndpointHitMapper;
import ru.practicum.ewm.model.ViewStat;
import ru.practicum.ewm.model.ViewStatMapper;
import ru.practicum.ewm.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EndpointHitServiceImpl implements EndpointHitService {
    private final EndpointHitRepository endpointHitRepository;
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    @Transactional
    @Override
    public EndpointHitDto createEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = EndpointHitMapper.toEndpointHit(endpointHitDto);
        EndpointHit savedHit = endpointHitRepository.save(endpointHit);
        return EndpointHitMapper.toEndpointHitDto(savedHit);
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ViewStatDto> getEndpointHits(String start, String end, List<String> uris, Boolean unique) {
        LocalDateTime startDateTime = LocalDateTime.parse(start, dtFormatter);
        LocalDateTime endDateTime = LocalDateTime.parse(end, dtFormatter);
        List<ViewStat> viewStats;
        if (unique && uris != null) {
            viewStats = endpointHitRepository.countHitsUniqueWithUriList(startDateTime, endDateTime, uris);
        } else if (unique) {
            viewStats = endpointHitRepository.countHitsUnique(startDateTime, endDateTime);
        } else if (uris != null) {
            viewStats = endpointHitRepository.countHitsNotUniqueWithUriList(startDateTime, endDateTime, uris);
        } else {
            viewStats = endpointHitRepository.countHitsNotUnique(startDateTime, endDateTime);
        }
        return ViewStatMapper.toViewStatDto(viewStats);
    }
}
