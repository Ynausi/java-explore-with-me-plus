package ru.practicum.service.event;

import ru.practicum.dto.event.EventReactionDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.users.UserRatingStatsDto;
import ru.practicum.dto.users.UserShortDto;
import ru.practicum.model.ReactionType;

import java.util.List;

public interface EventReactionService {

    EventReactionDto addLikeEvent(Long userId, Long eventId);

    EventReactionDto addDislikeEvent(Long userId, Long eventId);

    void deleteLikeEvent(Long userId, Long eventId);

    void deleteDislikeEvent(Long userId, Long eventId);

    List<UserRatingStatsDto> getUsersRatingStats(List<Long> userIds);

    List<EventShortDto> getTopEventsByRating(Integer limit, String order);

    List<UserShortDto> getUsersByReaction(Long eventId, ReactionType reactionType, Integer from, Integer size);
}