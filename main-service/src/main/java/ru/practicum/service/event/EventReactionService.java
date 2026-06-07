package ru.practicum.service.event;

import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.users.UserRatingStatsDto;
import ru.practicum.dto.users.UserShortDto;
import ru.practicum.model.ReactionType;

import java.util.List;

public interface EventReactionService {
    List<UserShortDto> getUsersByReaction(Long eventId, ReactionType reactionType, Integer from, Integer size);

    List<UserRatingStatsDto> getUsersRatingStats(List<Long> userIds);

    List<EventShortDto> getTopEventsByRating(Integer limit, String order);
}