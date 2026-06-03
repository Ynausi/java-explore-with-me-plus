package ru.practicum.controller.event;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.constant.Constants;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventSearchFilterAdmin;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.model.EventState;
import ru.practicum.service.event.EventService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/events")
public class AdminEventsController {

    private final EventService eventService;

    @GetMapping
    public List<EventFullDto> getEventsByAdmin(@RequestParam(required = false) List<Long> users,
                                               @RequestParam(required = false) List<EventState> states,
                                               @RequestParam(required = false) List<Long> categories,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                               LocalDateTime rangeStart,
                                               @RequestParam(required = false)
                                               @DateTimeFormat(pattern = Constants.DATE_TIME_FORMAT)
                                               LocalDateTime rangeEnd,
                                               @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(defaultValue = "10") @PositiveOrZero Integer size) {
        EventSearchFilterAdmin filter = new EventSearchFilterAdmin(users, states, categories, rangeStart, rangeEnd);

        return eventService.getEventsByAdmin(filter, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto patchEventByAdmin(@PathVariable Long eventId,
                                          @Valid @RequestBody UpdateEventAdminRequest request) {
        return eventService.updateEventByAdmin(eventId, request);
    }
}
