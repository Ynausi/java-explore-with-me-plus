package ru.practicum.repository.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.model.Category;

@RestController
public interface CategoryRepository extends JpaRepository<Category,Long>,
                                            CategoryCustomRepository {
    Boolean existsByName(String name);
}
