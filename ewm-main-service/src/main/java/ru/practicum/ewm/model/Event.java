package ru.practicum.ewm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewm.enm.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "events", schema = "public")
@Getter
@Setter
@ToString
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    // исключаем все поля с отложенной загрузкой из
    // метода toString, чтобы не было случайных обращений
    // базе данных, например при выводе в лог.
    @ToString.Exclude
    private Category category;

    @Column(nullable = false, name = "create_dttm")
    private LocalDateTime createdOn = LocalDateTime.now();

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, name = "event_dttm")
    private LocalDateTime eventDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @ToString.Exclude
    private User initiator;

    @Column(nullable = false, name = "is_paid")
    private Boolean paid;

    @Column(name = "participant_limit")
    private Integer participantLimit;

    @Column(name = "published_dttm")
    private LocalDateTime publishedOn;

    @Column(name = "is_moderated")
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;

    @Column(nullable = false)
    private String title;
}

