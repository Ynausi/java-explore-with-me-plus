package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.RequestStatus;

import java.util.List;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    @Query("SELECT COUNT(r) FROM ParticipationRequest r " +
            "WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    long countConfirmedRequestsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT pr.event.id, COUNT(pr.id) " +
            "FROM ParticipationRequest pr " +
            "WHERE pr.event.id IN :eventIds AND pr.status = :status " +
            "GROUP BY pr.event.id")
    List<Object[]> countByEventIdsAndStatus(@Param("eventIds") List<Long> eventIds,
                                            @Param("status") RequestStatus status);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    Integer countByEventIdAndStatus(Long eventId, RequestStatus status);
}