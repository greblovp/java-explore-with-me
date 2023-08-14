package ru.practicum.ewm.controller.priv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.util.CheckRequest;

import javax.validation.Valid;
import java.util.Collection;

@RestController
@RequestMapping(path = "/users/{userId}/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentPrivateController {
    private final CommentService commentService;

    @GetMapping
    public Collection<CommentDto> getCommentsByUserId(@PathVariable Long userId) {
        log.info("Получение информации o комментариях от пользователя: {}", userId);
        return commentService.getCommentsByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(@PathVariable Long userId,
                                    @RequestParam Long eventId, @RequestBody @Valid NewCommentDto newCommentDto,
                                    BindingResult bindingResult) {
        log.info("Создание комментария {} от пользователя {} для события {}", newCommentDto, userId, eventId);
        CheckRequest.check("Комментарий - " + newCommentDto, bindingResult);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping("/{commentId}")
    public CommentDto editComment(@PathVariable Long userId,
                                  @PathVariable Long commentId, @RequestBody @Valid NewCommentDto newCommentDto,
                                  BindingResult bindingResult) {
        log.info("Редактирование комментария {} от пользователя {}. Изменение на {}", commentId, userId, newCommentDto);
        CheckRequest.check("Комментарий - " + newCommentDto, bindingResult);
        return commentService.editComment(userId, commentId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {
        log.info("Удаление комментария {} от пользователя {}", commentId, userId);
        commentService.deleteComment(userId, commentId);
    }
}
