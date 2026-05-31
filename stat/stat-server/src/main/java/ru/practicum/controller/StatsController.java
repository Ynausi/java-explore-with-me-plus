package ru.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.HitRequestDto;
import ru.practicum.dto.StatsViewDto;
import ru.practicum.model.Hit;
import ru.practicum.service.StatsService;

import java.time.LocalDateTime;
import java.util.List;

import static ru.practicum.constant.Constants.DATE_TIME_FORMAT;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/hit")
    public Hit saveHit(@RequestBody @Valid HitRequestDto hitRequestDto) {
        return statsService.saveHit(hitRequestDto);
    }

    @GetMapping("/stats")
    public List<StatsViewDto> getStats(@RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime start,
                                       @RequestParam @DateTimeFormat(pattern = DATE_TIME_FORMAT) LocalDateTime end,
                                       @RequestParam(required = false) List<String> uris,
                                       @RequestParam(defaultValue = "false") Boolean unique) {

        return statsService.getStats(start, end, uris, unique);
    }
}