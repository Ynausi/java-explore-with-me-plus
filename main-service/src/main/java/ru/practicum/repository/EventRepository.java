package ru.practicum.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    @Query("SELECT MIN(e.createdOn) FROM Event e WHERE e.id IN :eventIds")
    Optional<LocalDateTime> findEarliestCreatedOnByEventIds(@Param("eventIds") List<Long> eventIds);

    @Query("select r.event.id, count(r.id) from Request r " +
            "where r.event.id in :eventIds and r.status = :status " +
            "group by r.event.id")
    Optional<Long> countByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds, @Param("status") RequestStatus status);

    List<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long userId);
}