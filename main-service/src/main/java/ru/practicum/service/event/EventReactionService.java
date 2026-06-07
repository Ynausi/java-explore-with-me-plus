package ru.practicum.service.event;

import ru.practicum.dto.event.EventReactionDto;

public interface EventReactionService {

    EventReactionDto addLikeEvent(Long userId, Long eventId);

    EventReactionDto addDislikeEvent(Long userId, Long eventId);

    void deleteLikeEvent(Long userId, Long eventId);

    void deleteDislikeEvent(Long userId, Long eventId);
}
