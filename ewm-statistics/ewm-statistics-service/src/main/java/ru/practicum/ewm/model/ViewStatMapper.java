package ru.practicum.ewm.model;

import ru.practicum.ewm.dto.ViewStatDto;

import java.util.ArrayList;
import java.util.List;

public class ViewStatMapper {
    public static ViewStatDto toViewStatDto(ViewStat viewStat) {
        return ViewStatDto.builder()
                .app(viewStat.getApp())
                .uri(viewStat.getUri())
                .hits(viewStat.getHits())
                .build();
    }

    public static List<ViewStatDto> toViewStatDto(Iterable<ViewStat> hits) {
        List<ViewStatDto> dtos = new ArrayList<>();
        for (ViewStat hit : hits) {
            dtos.add(toViewStatDto(hit));
        }
        return dtos;
    }

    public static ViewStat toViewStat(ViewStatDto viewStatDto) {
        ViewStat viewStat = new ViewStat();
        viewStat.setApp(viewStatDto.getApp());
        viewStat.setUri(viewStatDto.getUri());
        viewStat.setHits(viewStatDto.getHits());
        return viewStat;
    }
}
