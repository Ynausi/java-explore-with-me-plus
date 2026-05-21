package ru.practicum.service;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatsViewDto;
import ru.practicum.mapper.HitMapper;
import ru.practicum.model.Hit;
import ru.practicum.model.QHit;
import ru.practicum.storage.HitRepository;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {
    private final HitRepository hitRepository;
    private final JPAQueryFactory queryFactory;

    QHit qHit = QHit.hit;

    @Override
    public List<StatsViewDto> getStats(LocalDateTime start,
                              LocalDateTime end,
                              ArrayList<String> uris,
                              Boolean unique) {
        BooleanExpression filter = QHit.hit.timestamp.after(start).and(QHit.hit.timestamp.before(end));

        if (uris != null && !uris.isEmpty()) {
            filter = filter.and(QHit.hit.uri.in(uris));
        }

        List<Hit> hits = queryFactory.select(qHit).from(qHit).where(filter).fetch();

        Map<String, ArrayList<Hit>> appsAndHitsMap = new HashMap<>();

        for (Hit hit : hits) {
            appsAndHitsMap.computeIfAbsent(hit.getApp(), k -> new ArrayList<>());
            appsAndHitsMap.get(hit.getApp()).add(hit);
        }

        List<StatsViewDto> statsViewDtos = new ArrayList<>();

        if (unique) {
            for (Map.Entry<String, ArrayList<Hit>> appsAndHitsEntry : appsAndHitsMap.entrySet()) {
                Map<String, Integer> urisHitsCountMap = new HashMap<>();
                Map<String, ArrayList<String>> urisAndIpsMap = new HashMap<>();

                for (Hit hit : appsAndHitsEntry.getValue()) {
                    urisHitsCountMap.computeIfAbsent(hit.getUri(), k -> 0);
                    urisAndIpsMap.computeIfAbsent(hit.getUri(), k -> new ArrayList<>());
                    if (!urisAndIpsMap.get(hit.getUri()).contains(hit.getIp())) {
                        urisAndIpsMap.get(hit.getUri()).add(hit.getIp());
                        urisHitsCountMap.put(hit.getUri(), urisHitsCountMap.get(hit.getUri()) + 1);
                    }
                }

                for (Map.Entry<String, Integer> uriHitsCountEntry : urisHitsCountMap.entrySet()) {
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
            Map<String, Integer> urisHitsCountMap = new HashMap<>();

            for (Hit hit : appsAndHitsEntry.getValue()) {
                urisHitsCountMap.computeIfAbsent(hit.getUri(), k -> 0);
                urisHitsCountMap.put(hit.getUri(), urisHitsCountMap.get(hit.getUri()) + 1);
            }

            for (Map.Entry<String, Integer> uriHitsCountEntry : urisHitsCountMap.entrySet()) {
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

    @Override
    @Transactional
    public Hit saveHit(HitRequestDto hitRequestDto) {
        Hit hit = HitMapper.toHit(hitRequestDto);
        Hit savedHit = hitRepository.save(hit);

        log.info("Сохранено посещение с id = {}", savedHit.getId());
        return savedHit;
    }
}
