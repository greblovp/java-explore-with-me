package ru.practicum.ewm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments", schema = "public")
@Getter
@Setter
@ToString
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY)
    // исключаем все поля с отложенной загрузкой из
    // метода toString, чтобы не было случайных обращений
    // базе данных, например при выводе в лог.
    @ToString.Exclude
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User author;

    @Column(name = "created_dttm")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_dttm")
    private LocalDateTime updatedDate = LocalDateTime.now();
}
