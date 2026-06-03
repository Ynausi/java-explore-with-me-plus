package ru.practicum.controller.event;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.constant.Constants;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventSearchFilterPublic;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.PublicEventSort;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class PublicEventsController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEventsPublic(@RequestParam(required = false) String text,
                                               @RequestParam(required = false) List<@Positive Long> categories,
                                               @RequestParam(required = false) List<@Positive Long> users,
                                               @RequestParam(required = false) Boolean paid,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                               LocalDateTime rangeStart,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                               LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "false") Boolean onlyAvailable,
                                               @RequestParam(required = false) PublicEventSort sort,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(defaultValue = "10") @PositiveOrZero Integer size,
                                               HttpServletRequest request) {
        EventSearchFilterPublic filter = new EventSearchFilterPublic(text, categories, users, paid, rangeStart,
                rangeEnd, onlyAvailable, sort);
        return eventService.getEventsPublic(filter, from, size, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventPublicById(@PathVariable Long id, HttpServletRequest request) {
        return eventService.getPublicEventById(id, request);
    }
}
