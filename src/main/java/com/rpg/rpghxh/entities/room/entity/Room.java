package com.rpg.rpghxh.entities.room.entity;

import com.rpg.rpghxh.entities.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private User master;

    @Column(name = "current_players", nullable = false)
    @Builder.Default
    private int currentPlayers = 1;

    @Column(name = "max_players", nullable = false)
    @Builder.Default
    private int maxPlayers = 10;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
