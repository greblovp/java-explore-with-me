package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.model.EndpointHit;
import ru.practicum.ewm.model.ViewStat;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.ewm.model.ViewStat(h.app, h.uri, count(h.ip))" +
            "from EndpointHit as h "+
            "where h.timestamp between ?1 and ?2 "+
            "group by h.app, h.uri "+
            "order by count(h.ip) desc")
    List<ViewStat> countHitsNotUnique(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select new ru.practicum.ewm.model.ViewStat(h.app, h.uri, count(distinct h.ip))" +
            "from EndpointHit as h "+
            "where h.timestamp between ?1 and ?2 "+
            "group by h.app, h.uri "+
            "order by count(distinct h.ip) desc")
    List<ViewStat> countHitsUnique(LocalDateTime startDate, LocalDateTime endDate);

    @Query("select new ru.practicum.ewm.model.ViewStat(h.app, h.uri, count(h.ip))" +
            "from EndpointHit as h "+
            "where h.timestamp between ?1 and ?2 " +
            "and h.uri in ?3 "+
            "group by h.app, h.uri "+
            "order by count(h.ip) desc")
    List<ViewStat> countHitsNotUniqueWithUriList(LocalDateTime startDate, LocalDateTime endDate, List<String> uris);

    @Query("select new ru.practicum.ewm.model.ViewStat(h.app, h.uri, count(distinct h.ip))" +
            "from EndpointHit as h "+
            "where h.timestamp between ?1 and ?2 " +
            "and h.uri in ?3 "+
            "group by h.app, h.uri "+
            "order by count(distinct h.ip) desc")
    List<ViewStat> countHitsUniqueWithUriList(LocalDateTime startDate, LocalDateTime endDate, List<String> uris);
}
