package ru.practicum.service.event;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.requests.EventRequestStatusUpdateRequest;
import ru.practicum.dto.requests.EventRequestStatusUpdateResult;
import ru.practicum.dto.requests.ParticipationRequestDto;

import java.util.List;

public interface EventService {
    List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size);

    List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId);

    EventFullDto createEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventById(Long userId, Long eventId);

    EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest);

    EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest);
}