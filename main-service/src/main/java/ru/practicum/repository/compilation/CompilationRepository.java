package ru.practicum.repository.compilation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Compilation;
import java.util.Collection;

public interface CompilationRepository extends JpaRepository<Compilation,Long> {

    @Query(value = """
            select exists (
                select 1
                from compilations c
                left join compilation_events ce on c.id = ce.compilation_id
                where c.title = :title
                group by c.id
                having count(distinct ce.event_id) = :eventsSize
                   and count(distinct case
                        when ce.event_id in (:eventIds) then ce.event_id
                   end) = :eventsSize
            )
            """, nativeQuery = true)
    boolean existsByTitleAndSameEvents(@Param("title") String title,
                                       @Param("eventIds") Collection<Long> eventIds,
                                       @Param("eventsSize") long eventsSize);

}
