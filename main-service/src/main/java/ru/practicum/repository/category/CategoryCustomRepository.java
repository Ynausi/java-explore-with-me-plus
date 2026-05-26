package ru.practicum.repository.category;

import ru.practicum.dto.category.CategoryFilter;
import ru.practicum.model.Category;

import java.util.Collection;

public interface CategoryCustomRepository {
    Collection<Category> search(CategoryFilter categoryFilter);
}
