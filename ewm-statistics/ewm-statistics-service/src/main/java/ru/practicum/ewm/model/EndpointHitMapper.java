package ru.practicum.ewm.model;

import ru.practicum.ewm.dto.EndpointHitDto;

import java.util.ArrayList;
import java.util.List;

public class EndpointHitMapper {
    public static EndpointHitDto toEndpointHitDto(EndpointHit endpointHit) {
        return EndpointHitDto.builder()
                .app(endpointHit.getApp())
                .uri(endpointHit.getUri())
                .ip(endpointHit.getIp())
                .timestamp(endpointHit.getTimestamp())
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
        endpointHit.setTimestamp(endpointHitDto.getTimestamp());
        return endpointHit;
    }
}
