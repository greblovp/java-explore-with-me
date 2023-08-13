package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import ru.practicum.ewm.model.Comment;

import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment> {
    Iterable<Comment> findByAuthorId(Long userId);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long userId);

    boolean existsByIdAndAuthorId(Long commentId, Long userId);
}
