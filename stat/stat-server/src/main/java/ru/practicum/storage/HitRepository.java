package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Hit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepository extends JpaRepository<Hit, Long> {

    List<Hit> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    List<Hit> findByTimestampBetweenAndUriIn(LocalDateTime timestampAfter, LocalDateTime timestampBefore,
                                             List<String> uris);
}
