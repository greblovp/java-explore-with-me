package ru.practicum.ewm.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.client.StatisticsClient;
import ru.practicum.ewm.dto.EndpointHitDto;
import ru.practicum.ewm.dto.ViewStatDto;
import ru.practicum.ewm.service.StatisticsService;
import ru.practicum.ewm.util.DateFormatter;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private static final LocalDateTime MIN_DATE = LocalDateTime.of(1900, 1, 1, 0, 0);
    private final StatisticsClient statisticsClient;

    @Override
    public void logHit(HttpServletRequest request) {
        EndpointHitDto endpointHitDto = EndpointHitDto.builder()
                .app("ewm-main-service")
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(DateFormatter.dateToString(LocalDateTime.now()))
                .build();
        statisticsClient.createEndpointHit(endpointHitDto);
    }

    @Override
    public Map<Long, Long> getViews(List<Long> ids) {
        List<String> uris = ids.stream().map(id -> "/events/" + id).collect(Collectors.toList());

        String[] urisArray = uris.toArray(new String[uris.size()]);

        ResponseEntity<Object> response = statisticsClient.getEndpointHits(MIN_DATE, LocalDateTime.now().plusSeconds(1),
                urisArray, true);

        Object responseBody = response.getBody();

        ObjectMapper objectMapper = new ObjectMapper();

        Collection<ViewStatDto> viewStatDtos = objectMapper.convertValue(responseBody, new TypeReference<Collection<ViewStatDto>>() {
        });

        assert viewStatDtos != null;

        Map<Long, Long> viewsMap = new HashMap<>();

        for (ViewStatDto viewStatDto : viewStatDtos) {
            String str = viewStatDto.getUri();
            int index = str.lastIndexOf("/") + 1;
            Long id = Long.parseLong(str.substring(index));
            viewsMap.put(id, viewStatDto.getHits());
        }

        return viewsMap;
    }
}

