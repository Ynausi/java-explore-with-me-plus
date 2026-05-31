package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.dto.StatsViewDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.requests.EventRequestStatusUpdateRequest;
import ru.practicum.dto.requests.EventRequestStatusUpdateResult;
import ru.practicum.dto.requests.ParticipationRequestDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.ParticipationRequestMapper;
import ru.practicum.model.*;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UsersRepository;
import ru.practicum.repository.event.EventRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.constant.Constants.DATE_TIME_FORMAT;
import static ru.practicum.model.EventState.PUBLISHED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    private final ParticipationRequestRepository requestRepository;
    private final ParticipationRequestMapper requestMapper;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final UsersRepository usersRepository;
    private final StatsClient statsClient;
    private final EventMapper eventMapper;

    @Override
    public List<EventShortDto> getEventsByUser(Long userId, Integer from, Integer size) {
        getUserByIdOrThrow(userId);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        if (events.isEmpty()) return Collections.emptyList();

        List<Long> eventIds = events.stream().map(Event::getId).toList();

        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);

        return events.stream()
                .map(event -> {
                    EventShortDto shortDto = eventMapper.toEventShortDto(event);
                    shortDto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    shortDto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    return shortDto;
                })
                .toList();
    }

    @Override
    public List<ParticipationRequestDto> getRequestsByEvent(Long userId, Long eventId) {
        getUserByIdOrThrow(userId);
        Event event = getEventByIdOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId))
            throw new NotFoundException("Пользователь не является инициатором этого события");
        return requestRepository.findAllByEventId(eventId).stream()
                .map(requestMapper::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = getUserByIdOrThrow(userId);
        LocalDateTime now = LocalDateTime.now();

        if (newEventDto.getEventDate().isBefore(now.plusHours(2))) {
            throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        Category category = getCategoryByIdOrThrow(newEventDto.getCategory());
        Event newEvent = eventMapper.toEvent(newEventDto);

        newEvent.setInitiator(initiator);
        newEvent.setCategory(category);

        if (newEvent.getPaid() == null) newEvent.setPaid(false);
        if (newEvent.getParticipantLimit() == null) newEvent.setParticipantLimit(0);
        if (newEvent.getRequestModeration() == null) newEvent.setRequestModeration(true);

        newEvent.setEventState(EventState.PENDING);
        newEvent.setCreatedOn(now);

        Event createdEvent = eventRepository.save(newEvent);

        EventFullDto fullDto = eventMapper.toEventFullDto(createdEvent);
        fullDto.setViews(0L);
        fullDto.setConfirmedRequests(0L);
        return fullDto;
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        getUserByIdOrThrow(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id - " + eventId + " не найдено"));

        Map<Long, Long> viewsMap = getViewsMap(List.of(eventId));

        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setViews(viewsMap.getOrDefault(eventId, 0L));
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        getUserByIdOrThrow(userId);
        Event event = getEventByIdOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не является инициатором этого события");
        }

        if (updateEventUserRequest.getEventDate() != null) {
            if (updateEventUserRequest.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
            }
            event.setEventDate(updateEventUserRequest.getEventDate());
        }

        if (event.getEventState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Событие уже опубликовано");
        }

        if (updateEventUserRequest.getStateAction() != null) {
            if (updateEventUserRequest.getStateAction().equals(UserStateAction.SEND_TO_REVIEW)) {
                event.setEventState(EventState.PENDING);
            } else if (updateEventUserRequest.getStateAction().equals(UserStateAction.CANCEL_REVIEW)) {
                event.setEventState(EventState.CANCELED);
            }
        }

        if (updateEventUserRequest.getAnnotation() != null)
            event.setAnnotation(updateEventUserRequest.getAnnotation());

        if (updateEventUserRequest.getCategory() != null)
            event.setCategory(getCategoryByIdOrThrow(updateEventUserRequest.getCategory()));

        if (updateEventUserRequest.getDescription() != null)
            event.setDescription(updateEventUserRequest.getDescription());

        if (updateEventUserRequest.getLocation() != null)
            event.setLocation(eventMapper.toLocation(updateEventUserRequest.getLocation()));

        if (updateEventUserRequest.getPaid() != null)
            event.setPaid(updateEventUserRequest.getPaid());

        if (updateEventUserRequest.getParticipantLimit() != null)
            event.setParticipantLimit(updateEventUserRequest.getParticipantLimit());

        if (updateEventUserRequest.getRequestModeration() != null)
            event.setRequestModeration(updateEventUserRequest.getRequestModeration());

        if (updateEventUserRequest.getTitle() != null)
            event.setTitle(updateEventUserRequest.getTitle());

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateRequest) {
        getUserByIdOrThrow(userId);
        Event event = getEventByIdOrThrow(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException("Пользователь не является инициатором этого события");
        }

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        if (event.getParticipantLimit() > 0 && confirmedCount >= event.getParticipantLimit()) {
            throw new ConflictException("Лимит участников для данного события уже исчерпан");
        }

        List<ParticipationRequest> requests = requestRepository.findAllById(updateRequest.getRequestIds());

        for (ParticipationRequest request : requests) {
            if (!request.getEvent().getId().equals(eventId)) {
                throw new BadRequestException("Запрос не относится к данному событию");
            }
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new ConflictException("Статус можно менять только у заявок, находящихся в состоянии ожидания");
            }
        }

        List<ParticipationRequest> confirmedRequests = new ArrayList<>();
        List<ParticipationRequest> rejectedRequests = new ArrayList<>();
        RequestStatus targetStatus = updateRequest.getStatus();

        if (targetStatus == RequestStatus.REJECTED) {
            for (ParticipationRequest request : requests) {
                request.setStatus(RequestStatus.REJECTED);
                rejectedRequests.add(request);
            }
        } else if (targetStatus == RequestStatus.CONFIRMED) {
            for (ParticipationRequest request : requests) {
                if (event.getParticipantLimit() == 0 || confirmedCount < event.getParticipantLimit()) {
                    request.setStatus(RequestStatus.CONFIRMED);
                    confirmedRequests.add(request);
                    confirmedCount++;
                } else {
                    request.setStatus(RequestStatus.REJECTED);
                    rejectedRequests.add(request);
                }
            }
        }

        requestRepository.saveAll(requests);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(confirmedRequests.stream().map(requestMapper::toDto).toList())
                .rejectedRequests(rejectedRequests.stream().map(requestMapper::toDto).toList())
                .build();
    }

    private Map<Long, Long> getViewsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();

        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        LocalDateTime earliestDateTime = eventRepository.findEarliestCreatedOnByEventIds(eventIds)
                .orElse(LocalDateTime.now().minusMonths(6));

        String start = earliestDateTime.format(FORMATTER);
        String end = LocalDateTime.now().format(FORMATTER);

        List<StatsViewDto> stats = statsClient.getStats(start, end, uris, true);
        if (stats == null || stats.isEmpty()) return Collections.emptyMap();

        return stats.stream()
                .collect(Collectors.toMap(
                        statsDto -> {
                            String uri = statsDto.getUri();
                            return Long.parseLong(uri.substring(uri.lastIndexOf("/") + 1));
                        },
                        StatsViewDto::getHits,
                        (existing, replacement) -> existing
                ));
    }

    private Map<Long, Long> getConfirmedRequestsMap(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) return Collections.emptyMap();

        List<Object[]> result = requestRepository.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);

        return result.stream()
                .collect(Collectors.toMap(
                        objects -> (Long) objects[0],
                        objects -> (Long) objects[1],
                        (existing, replacement) -> existing
                ));
    }

    private User getUserByIdOrThrow(Long userId) {
        return usersRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь c id - " + userId + " не найден или недоступен"));
    }

    private Category getCategoryByIdOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Категория с id - " + categoryId + " не найдена"));
    }

    private Event getEventByIdOrThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id - " + eventId + " не найдено"));
    }

    @Override
    public List<EventFullDto> getEventsByAdmin(List<Long> users,
                                               List<EventState> states,
                                               List<Long> categories,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Integer from,
                                               Integer size) {
        EventSearchFilterAdmin filter = new EventSearchFilterAdmin(users, states, categories, rangeStart, rangeEnd);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        List<Event> events = eventRepository.searchAdmin(filter, pageable);

        if (events.isEmpty()) return Collections.emptyList();

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);

        return events.stream()
                .map(event -> {
                    EventFullDto dto = eventMapper.toEventFullDto(event);
                    dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    @Transactional
    @Override
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventByIdOrThrow(eventId);

        if (request.getEventDate() != null) {
            if (request.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Дата и время на которые намечено событие не может быть раньше, чем " +
                        "через два часа от текущего момента");
            }
            event.setEventDate(request.getEventDate());
        }

        if (request.getStateAction() != null) {
            if (request.getStateAction().equals(AdminStateAction.PUBLISH_EVENT)) {
                if (!event.getEventState().equals(EventState.PENDING)) {
                    throw new ConflictException("Можно публиковать только события в состоянии ожидания публикации");
                }
                event.setEventState(PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            } else if (request.getStateAction().equals(AdminStateAction.REJECT_EVENT)) {
                if (event.getEventState().equals(PUBLISHED)) {
                    throw new ConflictException("Нельзя отклонить уже опубликованное событие");
                }
                event.setEventState(EventState.CANCELED);
            }
        }

        if (request.getAnnotation() != null)
            event.setAnnotation(request.getAnnotation());

        if (request.getCategory() != null)
            event.setCategory(getCategoryByIdOrThrow(request.getCategory()));

        if (request.getDescription() != null)
            event.setDescription(request.getDescription());

        if (request.getLocation() != null)
            event.setLocation(eventMapper.toLocation(request.getLocation()));

        if (request.getPaid() != null)
            event.setPaid(request.getPaid());

        if (request.getParticipantLimit() != null)
            event.setParticipantLimit(request.getParticipantLimit());

        if (request.getRequestModeration() != null)
            event.setRequestModeration(request.getRequestModeration());

        if (request.getTitle() != null)
            event.setTitle(request.getTitle());

        log.info("Событие с id - {} обновлено администратором", eventId);

        return eventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<EventShortDto> getEventsPublic(String text,
                                               List<Long> categories,
                                               List<Long> users,
                                               Boolean paid,
                                               LocalDateTime rangeStart,
                                               LocalDateTime rangeEnd,
                                               Boolean onlyAvailable,
                                               PublicEventSort sort,
                                               Integer from,
                                               Integer size) {
        EventSearchFilterPublic filter = new EventSearchFilterPublic(text, categories, users, paid, rangeStart, rangeEnd,
                onlyAvailable, sort);
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        List<Event> events = eventRepository.searchPublic(filter, pageable);

        if (events.isEmpty()) return Collections.emptyList();

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Map<Long, Long> viewsMap = getViewsMap(eventIds);
        Map<Long, Long> confirmedRequestsMap = getConfirmedRequestsMap(eventIds);

        return events.stream()
                .map(event -> {
                    EventShortDto dto = eventMapper.toEventShortDto(event);
                    dto.setViews(viewsMap.getOrDefault(event.getId(), 0L));
                    dto.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    @Override
    public EventFullDto getPublicEventById(Long eventId) {
        Event event = getEventByIdOrThrow(eventId);

        if (!event.getEventState().equals(PUBLISHED)) {
            throw new NotFoundException("Событие с id - " + eventId + " не найдено");
        }

        Map<Long, Long> viewsMap = getViewsMap(List.of(eventId));
        Long confirmedRequests = requestRepository.countConfirmedRequestsByEventId(eventId);

        EventFullDto dto = eventMapper.toEventFullDto(event);
        dto.setViews(viewsMap.getOrDefault(eventId, 0L));
        dto.setConfirmedRequests(confirmedRequests);
        return dto;
    }
}