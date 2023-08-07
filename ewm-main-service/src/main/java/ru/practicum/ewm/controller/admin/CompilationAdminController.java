package ru.practicum.ewm.controller.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.compilation.CompilationDto;
import ru.practicum.ewm.dto.compilation.NewCompilationDto;
import ru.practicum.ewm.dto.compilation.UpdateCompilationRequest;
import ru.practicum.ewm.service.CompilationService;
import ru.practicum.ewm.util.CheckRequest;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "admin/compilations")
@RequiredArgsConstructor
@Slf4j
public class CompilationAdminController {
    private final CompilationService compilationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompilationDto createCompilation(@RequestBody @Valid NewCompilationDto newCompilationDto, BindingResult bindingResult) {
        log.info("Добавление новой подборки {}", newCompilationDto);
        CheckRequest.check("Подборка - " + newCompilationDto, bindingResult);
        return compilationService.createCompilation(newCompilationDto);
    }

    @DeleteMapping("/{compilationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCompilation(@PathVariable Long compilationId) {
        log.info("Удаление подборки {}", compilationId);
        compilationService.deleteCompilation(compilationId);
    }

    @PatchMapping("/{compilationId}")
    public CompilationDto updateCompilation(@PathVariable Long compilationId, @RequestBody UpdateCompilationRequest  updateCompilationRequest) {
        log.info("Изменение подборкки {} на {}", compilationId, updateCompilationRequest);
        return compilationService.updateCompilation(compilationId, updateCompilationRequest);
    }

}
