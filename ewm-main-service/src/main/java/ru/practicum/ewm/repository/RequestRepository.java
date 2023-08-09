package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.model.EventRequestStat;
import ru.practicum.ewm.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long>, QuerydslPredicateExecutor<ParticipationRequest> {
    @Query("select new ru.practicum.ewm.model.EventRequestStat(r.eventId, count(r.requesterId))" +
            "from ParticipationRequest as r " +
            "where r.eventId in ?1 " +
            "and r.status = 'CONFIRMED' " +
            "group by r.eventId")
    List<EventRequestStat> countConfirmedRequests(List<Long> eventIds);

    List<ParticipationRequest> findByRequesterId(Long userId);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long userId);

    List<ParticipationRequest> findByEventId(Long eventId);
}
