package ru.practicum.ewm.model;

import ru.practicum.ewm.dto.EndpointHitDto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EndpointHitMapper {
    private static final DateTimeFormatter dtFormatter = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneOffset.UTC);

    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp().format(dtFormatter))
                .build();
    }

    public static List<EndpointHitDto> toEndpointHitDto(Iterable<EndpointHit> hits) {
        List<EndpointHitDto> dtos = new ArrayList<>();
        for (EndpointHit hit : hits) {
            dtos.add(toEndpointHitDto(hit));
        }
        return dtos;
    }

    public static EndpointHit toEndpointHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setIp(endpointHitDto.getIp());
        endpointHit.setTimestamp(LocalDateTime.parse(endpointHitDto.getTimestamp(), dtFormatter));
        return endpointHit;
    }
}
