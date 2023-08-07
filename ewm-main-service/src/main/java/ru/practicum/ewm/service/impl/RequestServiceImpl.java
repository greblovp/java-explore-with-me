package ru.practicum.ewm.service.impl;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.ewm.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.ewm.dto.request.ParticipationRequestDto;
import ru.practicum.ewm.enm.EventState;
import ru.practicum.ewm.enm.RequestStatus;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.EventRequestStat;
import ru.practicum.ewm.model.ParticipationRequest;
import ru.practicum.ewm.model.QParticipationRequest;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.RequestRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.RequestService;
import ru.practicum.ewm.util.RequestMapper;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {
    private final RequestRepository requestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final QParticipationRequest qParticipationRequest = QParticipationRequest.participationRequest;

    @Transactional(readOnly = true)
    @Override
    public Collection<ParticipationRequestDto> getRequests(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("Пользователь с идентификатором " + userId + " не найден");
        }

        return RequestMapper.toRequestDto(requestRepository.findByRequesterId(userId));
    }

    @Transactional
    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("Пользователь с идентификатором " + userId + " не найден");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с идентификаторома " + eventId + " не найденно"));

        if (requestRepository.existsByEventIdAndRequesterId(eventId, userId)) {
            throw new ConditionsNotMetException("Заявка на участие в событии уже существует");
        }
        if (eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new ConditionsNotMetException("Инициатор события не может добавить запрос на участие в своём событии");
        }

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Нельзя участвовать в неопубликованном событии");
        }
        long confirmedRequests = Optional.ofNullable(getConfirmedRequests(List.of(eventId)).get(eventId))
                .orElse(0L);

        if (event.getParticipantLimit() <= confirmedRequests && event.getParticipantLimit() != 0) {
            throw new ConditionsNotMetException("У события достигнут лимит подтвержденных запросов на участие");
        }

        ParticipationRequest newRequest = new ParticipationRequest();
        newRequest.setRequesterId(userId);
        newRequest.setEventId(eventId);
        newRequest.setCreated(LocalDateTime.now());

        if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
            newRequest.setStatus(RequestStatus.CONFIRMED);
        } else {
            newRequest.setStatus(RequestStatus.PENDING);
        }

        return RequestMapper.toRequestDto(requestRepository.save(newRequest));
    }

    @Transactional
    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        ParticipationRequest requestToCancel = requestRepository.findByIdAndRequesterId(requestId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Заявка на участие в событика не найдена"));
        requestToCancel.setStatus(RequestStatus.CANCELED);
        return RequestMapper.toRequestDto(requestRepository.save(requestToCancel));
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        if (!eventRepository.existsByIdAndInitiatorId(eventId, userId)) {
            throw new ObjectNotFoundException("Событие с идентификаторома " + eventId + " не найденно");
        }
        List<ParticipationRequest> requests = requestRepository.findByEventId(eventId);
        return RequestMapper.toRequestDto(requests);
    }

    @Transactional
    @Override
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest request) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с идентификатором " + eventId + " не найденно"));

        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            throw new ConditionsNotMetException("Подтверждение заявок не требуется");
        }


        BooleanBuilder predicate = new BooleanBuilder();

        predicate.and(qParticipationRequest.eventId.eq(eventId))
                .and(qParticipationRequest.id.in(request.getRequestIds()));

        Iterable<ParticipationRequest> requestsToUpdate = requestRepository.findAll(predicate);

        RequestStatus newStatus = RequestStatus.valueOf(request.getStatus());
        long alreadyConfirmedRequests = Optional.ofNullable(getConfirmedRequests(List.of(eventId)).get(eventId))
                .orElse(0L);

        for (ParticipationRequest requestToUpdate : requestsToUpdate) {
            if (!requestToUpdate.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConditionsNotMetException("Заявка должна быть в статусе PENDING");
            }

            if (alreadyConfirmedRequests >= event.getParticipantLimit()) {
                rejectAllRequests(eventId);
                throw new ConditionsNotMetException("Лимит подтвержденных заявок уже достигнут");
            }
            requestToUpdate.setStatus(newStatus);

            if (newStatus.equals(RequestStatus.CONFIRMED)) {
                alreadyConfirmedRequests++;

            }
        }

        List<ParticipationRequest> updatedRequests = requestRepository.saveAll(requestsToUpdate);

        return RequestMapper.toEventRequestStatusUpdateResult(updatedRequests);
    }


    @Transactional(readOnly = true)
    @Override
    public Map<Long, Long> getConfirmedRequests(List<Long> ids) {
        List<EventRequestStat> requestStats = requestRepository.countConfirmedRequests(ids);

        return requestStats.stream()
                .collect(Collectors.toMap(EventRequestStat::getEventId, EventRequestStat::getConfirmedRequests));
    }

    @Transactional
    protected void rejectAllRequests(Long eventId) {
        BooleanBuilder predicate = new BooleanBuilder();

        predicate.and(qParticipationRequest.eventId.eq(eventId))
                .and(qParticipationRequest.status.eq(RequestStatus.PENDING));

        Iterable<ParticipationRequest> requestsToReject = requestRepository.findAll(predicate);
        for (ParticipationRequest requestToReject : requestsToReject) {
            requestToReject.setStatus(RequestStatus.REJECTED);
        }
        requestRepository.saveAll(requestsToReject);
    }
}
