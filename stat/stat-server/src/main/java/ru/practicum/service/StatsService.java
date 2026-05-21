package ru.practicum.service;

import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatsViewDto;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface StatsService {

    List<StatsViewDto> getStats(LocalDateTime start,
                                LocalDateTime end,
                                List<String> uris,
                                Boolean unique);

    Hit saveHit(HitRequestDto hitRequestDto);
}
