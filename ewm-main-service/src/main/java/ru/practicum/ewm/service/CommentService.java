package ru.practicum.ewm.service;

import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;

import java.util.Collection;

public interface CommentService {
    Collection<CommentDto> getComments(String text, Long eventId, String sortParam, Integer from, Integer size);

    Collection<CommentDto> getCommentsByUserId(Long userId);

    CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    CommentDto editComment(Long userId, Long commentId, NewCommentDto newCommentDto);

    void deleteComment(Long userId, Long commentId);

    CommentDto getCommentById(Long commentId);
}
