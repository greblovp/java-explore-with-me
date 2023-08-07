package ru.practicum.ewm.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.event.*;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.enm.EventSort;
import ru.practicum.ewm.enm.EventState;
import ru.practicum.ewm.enm.RequestStatus;
import ru.practicum.ewm.enm.StateAction;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.IncorrectRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.model.QEvent;
import ru.practicum.ewm.model.QParticipationRequest;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.LocationRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.EventService;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.ewm.service.StatisticsService;
import ru.practicum.ewm.util.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final StatisticsService statisticsService;
    private final RequestService requestService;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;

    private final QEvent qEvent = QEvent.event;
    private final QParticipationRequest qParticipationRequest = QParticipationRequest.participationRequest;

    private static final int MIN_ANNOTATION_LENGTH = 20;
    private static final int MAX_ANNOTATION_LENGTH = 2000;
    private static final int MIN_DESCRIPTION_LENGTH = 20;
    private static final int MAX_DESCRIPTION_LENGTH = 7000;
    private static final int MIN_TITLE_LENGTH = 3;
    private static final int MAX_TITLE_LENGTH = 120;

    @Transactional(readOnly = true)
    @Override
    public Collection<EventShortDto> getEvents(String text, List<Long> categories, Boolean paid, String rangeStart,
                                               String rangeEnd, Boolean onlyAvailable, String sortParam, Integer from, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();

        //Обязательное условие
        predicate.and(qEvent.state.eq(EventState.PUBLISHED));

        //Опицональные условия
        predicate.and(makeTextCondition(text))
                .and(makeCategoriesCondition(categories))
                .and(makePaidCondition(paid))
                .and(makeDateRangeCondition(rangeStart, rangeEnd))
                .and(makeOnlyAvailableCondition(onlyAvailable));
        //====================

        EventSort eventSort = EventSort.UNSORTED;
        if (sortParam != null) {
            eventSort = EventSort.from(sortParam)
                    .orElseThrow(() -> new IncorrectRequestException("Unknown event sort parameter: " + sortParam));
        }

        Sort sort = makeOrderByClause(eventSort);

        Pageable page = PageGetter.getPageRequest(from, size, sort);

        List<EventShortDto> eventShortDtos = getEventShortDtosByPredicateAndPage(predicate, page);

        if (eventSort.equals(EventSort.VIEWS)) {
            eventShortDtos.sort(Comparator.comparing(EventShortDto::getViews, Comparator.reverseOrder()));
        }

        return eventShortDtos;
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getEventById(Long eventId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);

        if (optionalEvent.isEmpty() || !optionalEvent.get().getState().equals(EventState.PUBLISHED)) {
            throw new ObjectNotFoundException("Опубликованное событие с идентификатором " + eventId + " не найденно");
        }

        return getEventFullDtoByEvent(optionalEvent.get());
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<EventFullDto> getAdminEvents(List<Long> users, List<String> states, List<Long> categories,
                                                   String rangeStart, String rangeEnd, Integer from, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();

        //Опицональные условия
        predicate.and(makeUsersCondition(users))
                .and(makeStatesCondition(states))
                .and(makeCategoriesCondition(categories))
                .and(makeDateRangeCondition(rangeStart, rangeEnd));
        //====================

        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("id"));

        return getEventFullDtosByPredicateAndPage(predicate, page);
    }

    @Transactional
    @Override
    public EventFullDto updateAdminEvent(Long eventId, UpdateEventAdminRequest updateEvent) {
        Event eventToUpdate = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с идентификатором " + eventId + " не найдено"));

        //Обновляем при заполненности соответствующего поля
        updateEvent(eventToUpdate, updateEvent.getAnnotation(), updateEvent.getCategory(), updateEvent.getDescription(),
                updateEvent.getEventDate(), updateEvent.getLocation(), updateEvent.getPaid(),
                updateEvent.getParticipantLimit(), updateEvent.getRequestModeration(), updateEvent.getTitle());

        Optional.ofNullable(updateEvent.getStateAction())
                .ifPresent(stateAction -> {
                    if (stateAction.equals(StateAction.PUBLISH_EVENT.name())) {
                        if (!eventToUpdate.getState().equals(EventState.PENDING)) {
                            throw new ConditionsNotMetException("Событие должно быть в статусе PENDING");
                        }
                        LocalDateTime publishedOn = LocalDateTime.now();
                        if (publishedOn.isAfter(eventToUpdate.getEventDate().minusHours(1))) {
                            throw new ConditionsNotMetException("Дата начала изменяемого события должна быть не ранее чем за час от даты публикации.");
                        }
                        eventToUpdate.setState(EventState.PUBLISHED);
                        eventToUpdate.setPublishedOn(publishedOn);
                    }
                    if (stateAction.equals(StateAction.REJECT_EVENT.name())) {
                        if (eventToUpdate.getState().equals(EventState.PUBLISHED)) {
                            throw new ConditionsNotMetException("Нельзя отменить опубликованное событие");
                        }
                        eventToUpdate.setState(EventState.CANCELED);
                    }
                });

        return getEventFullDtoByEvent(eventRepository.save(eventToUpdate));
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<EventShortDto> getUserEvents(Long userId, Integer from, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();

        //Обязательное условие
        predicate.and(qEvent.initiator.id.eq(userId));

        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("id"));

        return getEventShortDtosByPredicateAndPage(predicate, page);
    }

    @Transactional
    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        Category category = categoryRepository.findById(newEventDto.getCategory())
                .orElseThrow(() -> new ObjectNotFoundException("Категория с ID = " + newEventDto.getCategory()
                        + " не найдена."));
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException("Пользователь с ID = " + userId + " не найден."));

        Location location = locationRepository.findByLatAndLon(newEventDto.getLocation().getLat(), newEventDto.getLocation().getLon())
                .orElseGet(() -> locationRepository.save(LocationMapper.toLocation(newEventDto.getLocation())));


        Event event = EventMapper.toEvent(newEventDto, category, initiator, location);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Transactional(readOnly = true)
    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с идентификатором " + eventId + " не найденно"));

        return getEventFullDtoByEvent(event);
    }

    @Transactional
    @Override
    public EventFullDto updateUserEvent(Long userId, Long eventId, UpdateEventUserRequest updateEvent) {
        Event eventToUpdate = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с идентификатором " + eventId + " не найденно"));

        if (eventToUpdate.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Изменить можно только события в статусе PENDING и CANCELED");
        }

        //Обновляем при заполненности соответствующего поля
        updateEvent(eventToUpdate, updateEvent.getAnnotation(), updateEvent.getCategory(), updateEvent.getDescription(),
                updateEvent.getEventDate(), updateEvent.getLocation(), updateEvent.getPaid(),
                updateEvent.getParticipantLimit(), updateEvent.getRequestModeration(), updateEvent.getTitle());

        Optional.ofNullable(updateEvent.getStateAction())
                .ifPresent(stateAction -> {
                    if (stateAction.equals(StateAction.CANCEL_REVIEW.name())) {
                        eventToUpdate.setState(EventState.CANCELED);
                    }
                    if (stateAction.equals(StateAction.SEND_TO_REVIEW.name())) {
                        eventToUpdate.setState(EventState.PENDING);
                    }
                });

        return getEventFullDtoByEvent(eventRepository.save(eventToUpdate));
    }


    private BooleanExpression makeCategoriesCondition(List<Long> categories) {
        return categories != null && !categories.isEmpty() ? qEvent.category.id.in(categories) : null;
    }

    private BooleanExpression makePaidCondition(Boolean paid) {
        return paid != null ? qEvent.paid.eq(paid) : null;
    }

    private BooleanExpression makeDateRangeCondition(String rangeStart, String rangeEnd) {
        if (rangeStart != null && rangeEnd != null) {
            LocalDateTime startDateTime = DateFormatter.stringToDate(rangeStart);
            LocalDateTime endDateTime = DateFormatter.stringToDate(rangeEnd);
            if (startDateTime.isAfter(endDateTime)) {
                throw new IncorrectRequestException("Дата начала диапазона не может быть больше даты оконачания.");
            }
            return qEvent.eventDate.between(startDateTime, endDateTime);
        }
        return qEvent.eventDate.after(LocalDateTime.now());
    }

    private BooleanExpression makeTextCondition(String text) {
        return text != null && !text.isBlank()
                ? qEvent.annotation.containsIgnoreCase(text).or(qEvent.description.containsIgnoreCase(text)) : null;
    }

    private BooleanExpression makeOnlyAvailableCondition(Boolean onlyAvailable) {
        if (onlyAvailable) {
            return qEvent.participantLimit.gt(
                    JPAExpressions.select(qParticipationRequest.count())
                            .from(qParticipationRequest)
                            .where(qParticipationRequest.eventId.eq(qEvent.id)
                                    .and(qParticipationRequest.status.eq(RequestStatus.CONFIRMED)))
                            .groupBy(qParticipationRequest.eventId)
            );
        }
        return null;
    }

    private BooleanExpression makeStatesCondition(List<String> states) {
        if (states == null || states.isEmpty()) {
            return null;
        }
        List<EventState> eventStates = states.stream().map(EventState::valueOf).collect(Collectors.toList());
        return qEvent.state.in(eventStates);
    }

    private BooleanExpression makeUsersCondition(List<Long> users) {
        return users != null && !users.isEmpty() ? qEvent.initiator.id.in(users) : null;
    }

    private Sort makeOrderByClause(EventSort eventSort) {
        Sort sort;

        if (eventSort.equals(EventSort.EVENT_DATE)) {
            sort = Sort.by("eventDate");
        } else {
            sort = Sort.by("id");
        }
        return sort;
    }


    private List<EventShortDto> getEventShortDtosByPredicateAndPage(BooleanBuilder predicate, Pageable page) {
        Iterable<Event> events = eventRepository.findAll(predicate, page);

        List<Long> ids = StreamSupport.stream(events.spliterator(), false)
                .map(Event::getId)
                .collect(Collectors.toList());

        return EventMapper.toEventShortDto(events, statisticsService.getViews(ids), requestService.getConfirmedRequests(ids));
    }

    private List<EventFullDto> getEventFullDtosByPredicateAndPage(BooleanBuilder predicate, Pageable page) {
        Iterable<Event> events = eventRepository.findAll(predicate, page);

        List<Long> ids = StreamSupport.stream(events.spliterator(), false)
                .map(Event::getId)
                .collect(Collectors.toList());

        return EventMapper.toEventFullDto(events, statisticsService.getViews(ids), requestService.getConfirmedRequests(ids));
    }

    private EventFullDto getEventFullDtoByEvent(Event event) {
        Long eventId = event.getId();
        Map<Long, Long> viewsMap = statisticsService.getViews(List.of(eventId));
        Map<Long, Long> confirmedRequestsMap = requestService.getConfirmedRequests(List.of(eventId));

        return EventMapper.toEventFullDto(event, viewsMap, confirmedRequestsMap);
    }

    private void updateEvent(Event eventToUpdate, String annotation, Long category, String description, String eventDate,
                             LocationDto location, Boolean paid, Integer participantLimit, Boolean requestModeration, String title) {
        if (annotation != null) {
            if (annotation.length() > MAX_ANNOTATION_LENGTH || annotation.length() < MIN_ANNOTATION_LENGTH) {
                throw new IncorrectRequestException("Длина аннотациима должна быть от " + MIN_ANNOTATION_LENGTH + " до "
                        + MAX_ANNOTATION_LENGTH + " символов");
            }
            eventToUpdate.setAnnotation(annotation);
        }

        if (category != null) {
            Category newCategory = categoryRepository.findById(category)
                    .orElseThrow(() -> new ObjectNotFoundException("Категория с ID = " + category + " не найдена."));
            eventToUpdate.setCategory(newCategory);
        }

        if (description != null) {
            if (description.length() > MAX_DESCRIPTION_LENGTH || description.length() < MIN_DESCRIPTION_LENGTH) {
                throw new IncorrectRequestException("Длина описания должна быть от " + MIN_DESCRIPTION_LENGTH + " до "
                        + MAX_DESCRIPTION_LENGTH + " символов");
            }
            eventToUpdate.setDescription(description);
        }

        Optional.ofNullable(eventDate)
                .map(DateFormatter::stringToDate)
                .ifPresent(eventToUpdate::setEventDate);

        if (location != null) {
            Location newLocation = locationRepository.findByLatAndLon(location.getLat(), location.getLon())
                    .orElseGet(() -> locationRepository.save(LocationMapper.toLocation(location)));
            eventToUpdate.setLocation(newLocation);
        }

        Optional.ofNullable(paid)
                .ifPresent(eventToUpdate::setPaid);

        Optional.ofNullable(participantLimit)
                .ifPresent(eventToUpdate::setParticipantLimit);

        Optional.ofNullable(requestModeration)
                .ifPresent(eventToUpdate::setRequestModeration);

        if (title != null) {
            if (title.length() > MAX_TITLE_LENGTH || title.length() < MIN_TITLE_LENGTH) {
                throw new IncorrectRequestException("Длина заголовка должна быть от " + MIN_TITLE_LENGTH + " до "
                        + MAX_TITLE_LENGTH + " символов");
            }
            eventToUpdate.setTitle(title);
        }
    }
}
