package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Category;
import java.util.Collection;

public interface CategoryRepository extends JpaRepository<Category,Long> {

    Boolean existsByName(String name);

    Boolean existsByNameAndIdNot(String name,Long catId);

    @Query(value = """
                    select *
                    from categories
                    order by id asc
                    limit :size offset :from
                    """,
            nativeQuery = true)
    Collection<Category> findNeededCategories(@Param("from") Integer from,
                                              @Param("size") Integer size);
}
