package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryFilter;
import ru.practicum.dto.category.CategoryRequest;
import ru.practicum.dto.category.CategoryResponse;
import ru.practicum.exception.CategoryExistByName;
import ru.practicum.exception.CategoryNotFoundException;
import ru.practicum.mapper.category.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.category.CategoryRepository;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponse save(CategoryRequest categoryRequest) {
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new CategoryExistByName(String.format("Category with name=%s already exist",categoryRequest.getName()));
        }
        Category newCategory = categoryRepository.save(categoryMapper.toModel(categoryRequest));
        return categoryMapper.toResponse(newCategory);
    }

    @Override
    public CategoryResponse update(Long catId,CategoryRequest categoryRequest) {
        categoryRepository.findById(catId).orElseThrow(
                () -> new CategoryNotFoundException(String.format("Category with id=%s was not found",catId)));
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new CategoryExistByName(String.format("Category with name=%s already exist",categoryRequest.getName()));
        }
        Category updated = categoryRepository.save(categoryMapper.toModel(categoryRequest));
        return categoryMapper.toResponse(updated);
    }

    @Override
    public void delete(Long catId) {
        categoryRepository.findById(catId).orElseThrow(
                () -> new CategoryNotFoundException(String.format("Category with id=%s was not found",catId)));
        categoryRepository.deleteById(catId);
    }

    @Override
    public Collection<CategoryResponse> getNeeded(CategoryFilter categoryFilter) {
        return categoryRepository.search(categoryFilter).stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse findCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new CategoryNotFoundException(String.format("Category with id=%s was not found",catId)));
        return categoryMapper.toResponse(category);
    }


}
