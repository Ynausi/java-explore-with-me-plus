package ru.practicum.service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventReactionDto;
import ru.practicum.dto.users.UserRatingStatsDto;
import ru.practicum.dto.users.UserShortDto;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.mapper.UserMapper;
import ru.practicum.model.*;
import ru.practicum.repository.EventReactionRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UsersRepository;
import ru.practicum.repository.event.EventRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventReactionServiceImpl implements EventReactionService {

    private final EventReactionRepository reactionRepository;
    private final UsersRepository usersRepository;
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public EventReactionDto addLikeEvent(Long userId, Long eventId) {
        return addEventReaction(userId, eventId, ReactionType.LIKE);
    }

    @Override
    @Transactional
    public EventReactionDto addDislikeEvent(Long userId, Long eventId) {
        return addEventReaction(userId, eventId, ReactionType.DISLIKE);
    }

    @Override
    @Transactional
    public void deleteLikeEvent(Long userId, Long eventId) {
        deleteEventReaction(userId, eventId, ReactionType.LIKE);
    }

    @Override
    @Transactional
    public void deleteDislikeEvent(Long userId, Long eventId) {
        deleteEventReaction(userId, eventId, ReactionType.DISLIKE);
    }

    @Override
    public List<UserShortDto> getUsersByReaction(Long eventId, ReactionType reactionType, Integer from, Integer size) {
        getEventByIdOrThrow(eventId);

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

    private EventReactionDto addEventReaction(Long userId,
                                              Long eventId,
                                              ReactionType reactionType) {
        getUserByIdOrThrow(userId);
        getEventByIdOrThrow(eventId);

        Optional<EventReaction> existingReaction = reactionRepository.findByReactorIdAndEventId(userId, eventId);

        validateUserParticipant(userId, eventId);

        EventReaction reaction;
        if (existingReaction.isPresent()) {
            reaction = existingReaction.get();

            if (reaction.getReactionType().equals(reactionType)) {
                throw new ConflictException("Already reacted this event");
            }

            reaction.setReactionType(reactionType);
            reaction.setUpdatedAt(LocalDateTime.now());

        } else {
            EventReaction newReaction = eventMapper.toReaction(userId, eventId, reactionType);
            reaction = reactionRepository.save(newReaction);
        }


        return eventMapper.toReactionDto(reaction);
    }

    private void deleteEventReaction(Long userId, Long eventId, ReactionType reactionType) {
        getUserByIdOrThrow(userId);
        getEventByIdOrThrow(eventId);

        EventReaction reaction = reactionRepository.findByReactorIdAndEventId(userId, eventId)
                .filter(r -> r.getReactionType().equals(reactionType))
                .orElseThrow(() -> new NotFoundException(
                        String.format("Reaction %s not found for this event", reactionType)
                ));

        reactionRepository.delete(reaction);
    }

    private void validateUserParticipant(Long userId, Long eventId) {
        boolean isParticipant = requestRepository.existsByRequesterIdAndEventIdAndStatus(
                userId, eventId, RequestStatus.CONFIRMED
        );

        if (!isParticipant) {
            throw new BadRequestException("Only participants can react to events");
        }
    }

    private User getUserByIdOrThrow(Long userId) {
        return usersRepository.findById(userId)
                .orElseThrow(
                        () -> new NotFoundException("Пользователь c id - " + userId + " не найден или недоступен")
                );
    }

    private Event getEventByIdOrThrow(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(
                        () -> new NotFoundException("Событие с id - " + eventId + " не найдено")
                );
    }
}