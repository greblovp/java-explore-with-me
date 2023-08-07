package ru.practicum.ewm.service;


import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

public interface StatisticsService {
    void logHit(HttpServletRequest request);

    Map<Long, Long> getViews(List<Long> ids);
}
