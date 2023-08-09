package ru.practicum.ewm.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.ewm.enm.RequestStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "participation_requests", schema = "public")
@Getter
@Setter
@ToString
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "created_dttm")
    private LocalDateTime created = LocalDateTime.now();

    @Column(nullable = false, name = "event_id")
    private Long eventId;

    @Column(nullable = false, name = "requester_id")
    private Long requesterId;

    @Column(nullable = false, name = "status")
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
}
