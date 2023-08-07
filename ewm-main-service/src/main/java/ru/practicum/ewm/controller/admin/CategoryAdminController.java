package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.category.CategoryDto;
import ru.practicum.ewm.dto.category.NewCategoryDto;
import ru.practicum.ewm.service.CategoryService;
import ru.practicum.ewm.util.CheckRequest;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryAdminController {
    private final CategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@RequestBody @Valid NewCategoryDto newCategoryDto, BindingResult bindingResult) {
        log.info("Добавление новой категории: {}", newCategoryDto);
        CheckRequest.check("Категория - " + newCategoryDto, bindingResult);
        return categoryService.createCategory(newCategoryDto);
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long categoryId) {
        log.info("Удаление катебории с id: {}", categoryId);
        categoryService.deleteCategory(categoryId);
    }

    @PatchMapping("/{categoryId}")
    public CategoryDto updateCategory(@RequestBody @Valid NewCategoryDto newCategoryDto,
                                      @PathVariable Long categoryId, BindingResult bindingResult) {
        log.info("Изменение категории с id: {}, на значения: {}", categoryId, newCategoryDto);
        CheckRequest.check("Категория - " + newCategoryDto, bindingResult);
        return categoryService.updateCategory(newCategoryDto, categoryId);
    }

}
