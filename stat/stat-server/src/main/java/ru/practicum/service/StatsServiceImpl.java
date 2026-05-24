package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatsViewDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.storage.HitRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final HitRepository hitRepository;

    @Override
    public List<StatsViewDto> getStats(LocalDateTime start,
                                       LocalDateTime end,
                                       List<String> uris,
                                       Boolean unique) {
        List<Hit> hits;

        if (uris != null && !uris.isEmpty()) {
            hits = hitRepository.findByTimestampBetweenAndUriIn(start, end, uris);
        } else {
            hits = hitRepository.findByTimestampBetween(start, end);
        }

        List<StatsViewDto> statsViewDtos = new ArrayList<>();

        Map<String, List<Hit>> appHits = hits.stream()
                .collect(Collectors.groupingBy(Hit::getApp));

        if (Boolean.TRUE.equals(unique)) {
            for (var appHitEntry : appHits.entrySet()) {
                Map<String, Set<String>> uriUniqueIPs = appHitEntry.getValue()
                        .stream()
                        .collect(Collectors.groupingBy(
                                Hit::getUri,
                                Collectors.mapping(Hit::getIp, Collectors.toSet())
                        ));

                uriUniqueIPs.forEach((key, value) ->
                        statsViewDtos.add(makeStatsDto(key, appHitEntry.getKey(), value.size())));
            }
        } else {
            for (var appHitEntry : appHits.entrySet()) {
                Map<String, List<String>> uriIPs = appHitEntry.getValue()
                        .stream()
                        .collect(Collectors.groupingBy(
                                Hit::getUri,
                                Collectors.mapping(Hit::getIp, Collectors.toList())
                        ));

                uriIPs.forEach((key, value) ->
                        statsViewDtos.add(makeStatsDto(key, appHitEntry.getKey(), value.size())));
            }
        }

        return statsViewDtos.stream()
                .sorted(Comparator.comparing(StatsViewDto::getHits).reversed())
                .toList();
    }

    public StatsViewDto makeStatsDto(String app, String uri, int hitsCount) {
        return new StatsViewDto(app, uri, hitsCount);
    }

    @Override
    @Transactional
    public Hit saveHit(HitRequestDto hitRequestDto) {
        Hit hit = HitMapper.toHit(hitRequestDto);
        Hit savedHit = hitRepository.save(hit);

        log.info("Сохранено посещение с id = {}", savedHit.getId());
        return savedHit;
    }
}