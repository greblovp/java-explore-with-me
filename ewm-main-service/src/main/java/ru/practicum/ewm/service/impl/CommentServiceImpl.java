package ru.practicum.ewm.service.impl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.dto.comment.CommentDto;
import ru.practicum.ewm.dto.comment.NewCommentDto;
import ru.practicum.ewm.enm.CommentSort;
import ru.practicum.ewm.enm.EventState;
import ru.practicum.ewm.exception.ConditionsNotMetException;
import ru.practicum.ewm.exception.IncorrectRequestException;
import ru.practicum.ewm.exception.ObjectNotFoundException;
import ru.practicum.ewm.model.Comment;
import ru.practicum.ewm.model.Event;
import ru.practicum.ewm.model.QComment;
import ru.practicum.ewm.model.User;
import ru.practicum.ewm.repository.CommentRepository;
import ru.practicum.ewm.repository.EventRepository;
import ru.practicum.ewm.repository.UserRepository;
import ru.practicum.ewm.service.CommentService;
import ru.practicum.ewm.util.CommentMapper;
import ru.practicum.ewm.util.PageGetter;

import java.time.LocalDateTime;
import java.util.Collection;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final QComment qComment = QComment.comment;

    @Transactional(readOnly = true)
    @Override
    public Collection<CommentDto> getComments(String text, Long eventId, String sortParam, Integer from, Integer size) {
        BooleanBuilder predicate = new BooleanBuilder();

//      Опицональные условия
        predicate.and(makeTextCondition(text))
                .and(makeEventIdCondition(eventId));

        CommentSort commentSort = CommentSort.from(sortParam)
                .orElseThrow(() -> new IncorrectRequestException("Unknown event sort parameter: " + sortParam));

        Sort sort = makeOrderByClause(commentSort);

        Pageable page = PageGetter.getPageRequest(from, size, sort);

        return CommentMapper.toCommentDto(commentRepository.findAll(predicate, page));
    }

    @Transactional(readOnly = true)
    @Override
    public Collection<CommentDto> getCommentsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ObjectNotFoundException("Пользователь с ID = " + userId + " не найден.");
        }

        return CommentMapper.toCommentDto(commentRepository.findByAuthorId(userId));
    }

    @Transactional
    @Override
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new ObjectNotFoundException("Пользователь с идентификатором " + userId + " не найден"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ObjectNotFoundException("Событие с идентификаторома " + eventId + " не найденно"));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new ConditionsNotMetException("Нельзя добавлять комментарии в неопубликованном событии");
        }

        Comment comment = CommentMapper.toComment(newCommentDto);
        comment.setAuthor(author);
        comment.setEvent(event);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    @Override
    public CommentDto editComment(Long userId, Long commentId, NewCommentDto newCommentDto) {
        Comment commentToEdit = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new ObjectNotFoundException("Комментарий с идентификатором " + commentId +
                        " у пользователя с идентификаторома " + userId + "не найденн"));
        commentToEdit.setText(newCommentDto.getText());
        commentToEdit.setUpdatedDate(LocalDateTime.now());
        return CommentMapper.toCommentDto(commentRepository.save(commentToEdit));
    }

    @Transactional
    @Override
    public void deleteComment(Long userId, Long commentId) {
        if (!commentRepository.existsByIdAndAuthorId(commentId, userId))
            throw new ObjectNotFoundException("Комментарий с идентификаторома " + commentId +
                    " у пользователя с идентификаторома " + userId + " не найден");
        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ObjectNotFoundException("Комментарий с ID = " + commentId + " не найден."));

        return CommentMapper.toCommentDto(comment);
    }

    private BooleanExpression makeTextCondition(String text) {
        return text != null && !text.isBlank()
                ? qComment.text.containsIgnoreCase(text) : null;
    }

    private BooleanExpression makeEventIdCondition(Long eventId) {
        return eventId != null
                ? qComment.event.id.eq(eventId) : null;
    }

    private Sort makeOrderByClause(CommentSort commentSort) {
        Sort sort;
        switch (commentSort) {
            case CREATED_DATE:
                sort = Sort.by("createdDate");
                break;
            case UPDATED_DATE:
                sort = Sort.by("updatedDate");
                break;
            case EVENT_ID:
                sort = Sort.by("event.id");
                break;
            default:
                sort = Sort.by("id");
                break;
        }
        return sort;
    }
}
