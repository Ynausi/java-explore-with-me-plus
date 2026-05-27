package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.ParticipationRequest;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

}
