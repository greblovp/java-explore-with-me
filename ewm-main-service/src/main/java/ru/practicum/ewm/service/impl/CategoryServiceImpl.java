package ru.practicum.ewm.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.model.Category;
import ru.practicum.ewm.repository.CategoryRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.service.CategoryService;
import ru.practicum.ewm.util.CategoryMapper;
import ru.practicum.ewm.util.PageGetter;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional(readOnly = true)
    @Override
    public Collection<CategoryDto> getCategories(Integer from, Integer size) {
        Pageable page = PageGetter.getPageRequest(from, size, Sort.by("id"));

        return CategoryMapper.toCategoryDto(categoryRepository.findAll(page));
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Категория с ID = " + categoryId
                        + " не найдена."));

        return CategoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.toCategory(newCategoryDto);

        return CategoryMapper.toCategoryDto(categoryRepository.save(category));
    }

    @Transactional
    @Override
    public void deleteCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new ObjectNotFoundException("Категория с ID = " + categoryId + " не найдена.");
        }

        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConditionsNotMetException("Категория с ID = " + categoryId
                    + " не может быть удалена, так как с ней связаны события.");
        }

        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(NewCategoryDto newCategoryDto, Long categoryId) {
        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ObjectNotFoundException("Категория с ID = " + categoryId + " не найдена."));

        categoryToUpdate.setName(newCategoryDto.getName());

        return CategoryMapper.toCategoryDto(categoryRepository.save(categoryToUpdate));
    }
}
