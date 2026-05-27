package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.StatsClient;
import ru.practicum.dto.StatsViewDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.EventState;
import ru.practicum.model.User;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UsersRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.practicum.constant.Constants.DATE_TIME_FORMAT;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
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

        // ...
    }

    @Override
    public EventFullDto createEvent(Long userId, NewEventDto newEventDto) {
        User initiator = getUserByIdOrThrow(userId);

        if (newEventDto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента");
        }

        Category category = getCategoryByIdOrThrow(newEventDto.getCategory());
        Event newEvent = eventMapper.toEvent(newEventDto);

        newEvent.setInitiator(initiator);
        newEvent.setCategory(category);

        if (newEvent.getPaid() == null) newEvent.setPaid(false);
        if (newEvent.getParticipantLimit() == null) newEvent.setParticipantLimit(0);
        if (newEvent.getRequestModeration() == null) newEvent.setRequestModeration(true);

        newEvent.setEventState(EventState.PENDING);
        newEvent.setCreatedOn(LocalDateTime.now());

        Event createdEvent = eventRepository.save(newEvent);

        EventFullDto fullDto = eventMapper.toEventFullDto(createdEvent);
        fullDto.setViews(0L);
        fullDto.setConfirmedRequests(0);
        return fullDto;
    }

    @Override
    public EventFullDto getEventById(Long userId, Long eventId) {
        getUserByIdOrThrow(userId);
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id - " + eventId + " не найдено"));

        Map<Long, Long> viewsMap = getViewsMap(List.of(eventId));

        Integer confirmedRequests = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        EventFullDto eventFullDto = eventMapper.toEventFullDto(event);
        eventFullDto.setViews(viewsMap.getOrDefault(eventId, 0L));
        eventFullDto.setConfirmedRequests(confirmedRequests);
        return eventFullDto;
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

    private User getUserByIdOrThrow(Long userId) {
        return usersRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь c id - " + userId + " не найден или недоступен"));
    }

    private Category getCategoryByIdOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId).orElseThrow(() -> new NotFoundException("Категория с id - " + categoryId + " не найдена"));
    }

    private Event getEventByIdOrThrow(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id - " + eventId + " не найдено"));
    }
}