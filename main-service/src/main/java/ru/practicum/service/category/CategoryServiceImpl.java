package ru.practicum.service.category;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.dto.category.CategoryFilter;
import ru.practicum.dto.category.CategoryRequest;
import ru.practicum.dto.category.CategoryResponse;
import ru.practicum.exception.CategoryDeleteConflictException;
import ru.practicum.mapper.category.CategoryMapper;
import ru.practicum.model.Category;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.event.EventRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final EventRepository eventRepository;

    @Override
    public CategoryResponse save(CategoryRequest categoryRequest) {
        if (categoryRepository.existsByName(categoryRequest.getName())) {
            throw new EntityExistsException(String.format("Category with name=%s already exist",categoryRequest.getName()));
        }
        Category newCategory = categoryRepository.save(categoryMapper.toModel(categoryRequest));
        return categoryMapper.toResponse(newCategory);
    }

    @Override
    public CategoryResponse update(Long catId,CategoryRequest categoryRequest) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Category with id=%s was not found",catId)));
        if (categoryRepository.existsByNameAndIdNot(categoryRequest.getName(),catId)) {
            throw new EntityExistsException(String.format("Category with name=%s already exist",categoryRequest.getName()));
        }
        category.setName(categoryRequest.getName());
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void delete(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Category with id=%s was not found",catId)));

        if (eventRepository.existsByCategory_Id(catId)) {
            throw  new CategoryDeleteConflictException(String.format("Cannot delete category, because some events have it"));
        }
        categoryRepository.delete(category);
    }

    @Override
    public Collection<CategoryResponse> getNeeded(CategoryFilter categoryFilter) {
        return categoryRepository.findNeededCategories(categoryFilter.getFrom(),categoryFilter.getSize())
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    public CategoryResponse findCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new EntityNotFoundException(String.format("Category with id=%s was not found",catId)));
        return categoryMapper.toResponse(category);
    }


}
