package ru.practicum.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Hit;

public interface HitRepository extends JpaRepository<Hit, Long> {
}
