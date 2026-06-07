package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.users.UserRatingStatsDto;
import ru.practicum.dto.users.UserShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ReactionType;
import ru.practicum.model.User;
import ru.practicum.repository.EventReactionRepository;
import ru.practicum.repository.event.EventRepository;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventReactionServiceImpl implements EventReactionService {

    private final EventReactionRepository reactionRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    public List<UserShortDto> getUsersByReaction(Long eventId, ReactionType reactionType, Integer from, Integer size) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id - " + eventId + " не найдено");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<User> reactors = reactionRepository.findReactorsByEventIdAndReactionType(eventId, reactionType, pageable);

        return reactors
                .stream()
                .map(userMapper::userToUserShortDto)
                .toList();
    }

    @Override
    public List<UserRatingStatsDto> getUsersRatingStats(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Collections.emptyList();
        return reactionRepository.getStatsByUserIds(userIds);
    }

    @Override
    public List<EventShortDto> getTopEventsByRating(Integer limit, String order) {
        List<Event> events;

        if (order != null && order.equalsIgnoreCase("ASC")) {
            events = eventRepository.findTopEventsByRatingAsc(limit);
        } else {
            events = eventRepository.findTopEventsByRatingDesc(limit);
        }
        return events.stream()
                .map(eventMapper::toEventShortDto)
                .toList();
    }
}