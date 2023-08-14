package ru.practicum.ewm.controller.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.service.CommentService;

import java.util.Collection;

@RestController
@RequestMapping(path = "/comments")
@RequiredArgsConstructor
@Slf4j
public class CommentPublicController {
    private final CommentService commentService;

    @GetMapping("")
    public Collection<CommentDto> getComments(@RequestParam(required = false) String text,
                                              @RequestParam(required = false) Long eventId,
                                              @RequestParam(required = false, defaultValue = "ID") String sortParam,
                                              @RequestParam(defaultValue = "0") Integer from,
                                              @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получение комментариев с возможностью фильтрации. Параметры: text = {}, eventId = {}, sortParam = {}, " +
                "from = {}, size = {}.", text, eventId, sortParam, from, size);
        return commentService.getComments(text, eventId, sortParam, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getCommentById(@PathVariable Long commentId) {
        log.info("Получение комментария по id: {}", commentId);
        return commentService.getCommentById(commentId);
    }
}
