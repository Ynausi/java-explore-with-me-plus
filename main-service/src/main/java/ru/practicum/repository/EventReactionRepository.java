package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EventReaction;
import ru.practicum.model.ReactionProjection;

import java.util.List;
import java.util.Optional;

public interface EventReactionRepository extends JpaRepository<EventReaction, Long> {

    Optional<EventReaction> findByReactorIdAndEventId(Long userId, Long eventId);


    @Query("SELECT r.event.id AS eventId, r.reactionType AS reaction " +
            "FROM EventReaction r " +
            "WHERE r.event.id IN :eventIds")
    List<ReactionProjection> findEventReactionsByEventIds(@Param("eventIds") List<Long> eventIds);
}
