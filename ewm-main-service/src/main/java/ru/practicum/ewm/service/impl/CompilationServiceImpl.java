package ru.practicum.ewm.service.impl;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.exception.IncorrectRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.model.Compilation;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.QCompilation;
import ru.practicum.ewm.repository.CompilationRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CompilationService;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.ewm.service.StatisticsService;
import ru.practicum.ewm.util.CompilationMapper;
import ru.practicum.ewm.util.PageGetter;

import java.util.*;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final StatisticsService statisticsService;
    private final RequestService requestService;
    private final EventRepository eventRepository;
    private static final int MIN_TITLE_LENGTH = 1;
    private static final int MAX_TITLE_LENGTH = 50;

    @Transactional(readOnly = true)
    @Override
    public Collection<CompilationDto> getCompilations(Boolean pinned, Integer from, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();

        //Обязательное условие
        if (pinned != null) {
            predicate.and(QCompilation.compilation.pinned.eq(pinned));
        }

        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("id"));

        Iterable<Compilation> compilations = compilationRepository.findAll(predicate, page);

        List<Long> eventIds = StreamSupport.stream(compilations.spliterator(), false)
                .flatMap(compilation -> compilation.getEvents().stream())
                .map(Event::getId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Long> views = statisticsService.getViews(eventIds);

        Map<Long, Long> confirmedRequests = requestService.getConfirmedRequests(eventIds);

        return CompilationMapper.toCompilationDto(compilations, views, confirmedRequests);
    }

    @Transactional(readOnly = true)
    @Override
    public CompilationDto getCompilationById(Long compilationId) {
        Compilation compilation = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new ObjectNotFoundException("Подборка событий с идентификатором " + compilationId + " не найдена"));

        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> views = statisticsService.getViews(eventIds);

        Map<Long, Long> confirmedRequests = requestService.getConfirmedRequests(eventIds);

        return CompilationMapper.toCompilationDto(compilation, views, confirmedRequests);
    }

    @Transactional
    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        if (newCompilationDto.getEvents() == null || newCompilationDto.getEvents().isEmpty()) {
            Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, Collections.emptyList());
            return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
        }

        List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
        Compilation compilation = CompilationMapper.toCompilation(newCompilationDto, events);

        List<Long> eventIds = events.stream()
                .map(Event::getId)
                .collect(Collectors.toList());

        Map<Long, Long> views = statisticsService.getViews(eventIds);

        Map<Long, Long> confirmedRequests = requestService.getConfirmedRequests(eventIds);

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation), views, confirmedRequests);
    }

    @Transactional
    @Override
    public void deleteCompilation(Long compilationId) {
        if (!compilationRepository.existsById(compilationId)) {
            throw new ObjectNotFoundException("Подборка событий с ID = " + compilationId + " не найдена.");
        }

        compilationRepository.deleteById(compilationId);
    }

    @Transactional
    @Override
    public CompilationDto updateCompilation(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilationToUpdate = compilationRepository.findById(compilationId)
                .orElseThrow(() -> new ObjectNotFoundException("Подборка событий с ID = " + compilationId + " не найдена."));

        List<Long> eventIds = new ArrayList<>();

        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateCompilationRequest.getEvents());
            compilationToUpdate.setEvents(new HashSet<>(events));
            eventIds = events.stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());
        } else {
            eventIds = compilationToUpdate.getEvents().stream()
                    .map(Event::getId)
                    .collect(Collectors.toList());
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilationToUpdate.setPinned(updateCompilationRequest.getPinned());
        }

        if (updateCompilationRequest.getTitle() != null) {
            String title = updateCompilationRequest.getTitle();
            if (title.length() < MIN_TITLE_LENGTH || title.length() > MAX_TITLE_LENGTH) {
                throw new IncorrectRequestException("Длина заголовка должна быть от " + MIN_TITLE_LENGTH + " до "
                        + MAX_TITLE_LENGTH + " символов.");
            }
            compilationToUpdate.setTitle(title);
        }

        Compilation updatedCompilation = compilationRepository.save(compilationToUpdate);

        Map<Long, Long> views = statisticsService.getViews(eventIds);

        Map<Long, Long> confirmedRequests = requestService.getConfirmedRequests(eventIds);

        return CompilationMapper.toCompilationDto(updatedCompilation, views, confirmedRequests);
    }
}
