package ru.practicum.ewm.util;

import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;

import java.util.*;

public class CompilationMapper {
    public static CompilationDto toCompilationDto(Compilation compilation) {
        return CompilationDto.builder()
                .id(compilation.getId())
                .pinned(compilation.getPinned())
                .title(compilation.getTitle())
                .events(new ArrayList<>())
                .build();
    }

    public static CompilationDto toCompilationDto(Compilation compilation, Map<Long, Long> views, Map<Long, Long> confirmedRequests) {
        CompilationDto compilationDto = toCompilationDto(compilation);
        compilationDto.setEvents(EventMapper.toEventShortDto(compilation.getEvents(), views, confirmedRequests));
        return compilationDto;
    }

    public static List<CompilationDto> toCompilationDto(Iterable<Compilation> compilations, Map<Long, Long> views,
                                                        Map<Long, Long> confirmedRequests) {
        List<CompilationDto> dtos = new ArrayList<>();
        for (Compilation compilation : compilations) {
            dtos.add(toCompilationDto(compilation, views, confirmedRequests));
        }
        return dtos;
    }

    public static Compilation toCompilation(NewCompilationDto newCompilationDto, List<Event> events) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(false);
        } else {
            compilation.setPinned(newCompilationDto.getPinned());
        }
        compilation.setEvents(new HashSet<>(events));
        return compilation;
    }
}
