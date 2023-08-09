package ru.practicum.ewm.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import ru.practicum.ewm.exception.IncorrectRequestException;

@Slf4j
public class CheckRequest {
    public static void check(String customMessage, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.warn("Ошибка в заполнении поля {} - {}. customMessage", bindingResult.getFieldError().getField(),
                    bindingResult.getFieldError().getDefaultMessage());
            throw new IncorrectRequestException("Ошибка в заполнении поля " + bindingResult.getFieldError().getField() + " - " +
                    bindingResult.getFieldError().getDefaultMessage() + ". " + customMessage);
        }
    }
}
