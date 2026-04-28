package com.finny.domain.master;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "user_sessions")
@Getter
@Setter
public class UserSession {

    @Id
    @Column(columnDefinition = "varchar(36)")
    private String id = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String token;

    @CreationTimestamp
    @Column(name = "session_start", nullable = false, updatable = false)
    private LocalDateTime sessionStartDateTime;

    @Column(name = "client_info", length = 255)
    private String clientInfo;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "invalidated_by")
    private String invalidatedBy;
}
