package ru.practicum.service.category;

import ru.practicum.dto.category.CategoryFilter;
import ru.practicum.dto.category.CategoryRequest;
import ru.practicum.dto.category.CategoryResponse;

import java.util.Collection;

public interface CategoryService {

    CategoryResponse save(CategoryRequest categoryRequest);

    CategoryResponse update(Long catId, CategoryRequest categoryRequest);

    void delete(Long catId);

    Collection<CategoryResponse> getNeeded(CategoryFilter categoryFilter);

    CategoryResponse findCategoryById(Long catId);
}
