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

        Map<String, ArrayList<Hit>> appsAndHitsMap = new HashMap<>();

        for (Hit hit : hits) {
            appsAndHitsMap.computeIfAbsent(hit.getApp(), k -> new ArrayList<>());
            appsAndHitsMap.get(hit.getApp()).add(hit);
        }

        List<StatsViewDto> statsViewDtos = new ArrayList<>();

        if (unique) {
            for (Map.Entry<String, ArrayList<Hit>> appsAndHitsEntry : appsAndHitsMap.entrySet()) {
                Map<String, Long> urisHitsCountMap = new HashMap<>();
                Map<String, ArrayList<String>> urisAndIpsMap = new HashMap<>();

                for (Hit hit : appsAndHitsEntry.getValue()) {
                    urisHitsCountMap.computeIfAbsent(hit.getUri(), k -> 0L);
                    urisAndIpsMap.computeIfAbsent(hit.getUri(), k -> new ArrayList<>());
                    if (!urisAndIpsMap.get(hit.getUri()).contains(hit.getIp())) {
                        urisAndIpsMap.get(hit.getUri()).add(hit.getIp());
                        urisHitsCountMap.put(hit.getUri(), urisHitsCountMap.get(hit.getUri()) + 1);
                    }
                }

                for (Map.Entry<String, Long> uriHitsCountEntry : urisHitsCountMap.entrySet()) {
                    StatsViewDto statsViewDto = new StatsViewDto();

                    statsViewDto.setUri(uriHitsCountEntry.getKey());
                    statsViewDto.setApp(appsAndHitsEntry.getKey());
                    statsViewDto.setHits(uriHitsCountEntry.getValue());

                    statsViewDtos.add(statsViewDto);
                }
            }

            List<StatsViewDto> statsViewDtosListSorted = statsViewDtos.stream()
                    .sorted(Comparator.comparing(StatsViewDto::getHits).reversed())
                    .toList();

            return statsViewDtosListSorted;
        }

        for (Map.Entry<String, ArrayList<Hit>> appsAndHitsEntry : appsAndHitsMap.entrySet()) {
            Map<String, Long> urisHitsCountMap = new HashMap<>();

            for (Hit hit : appsAndHitsEntry.getValue()) {
                urisHitsCountMap.computeIfAbsent(hit.getUri(), k -> 0L);
                urisHitsCountMap.put(hit.getUri(), urisHitsCountMap.get(hit.getUri()) + 1);
            }

            for (Map.Entry<String, Long> uriHitsCountEntry : urisHitsCountMap.entrySet()) {
                StatsViewDto statsViewDto = new StatsViewDto();

                statsViewDto.setUri(uriHitsCountEntry.getKey());
                statsViewDto.setApp(appsAndHitsEntry.getKey());
                statsViewDto.setHits(uriHitsCountEntry.getValue());

                statsViewDtos.add(statsViewDto);
            }
        }

        return statsViewDtos.stream()
                .sorted(Comparator.comparing(StatsViewDto::getHits).reversed())
                .toList();
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