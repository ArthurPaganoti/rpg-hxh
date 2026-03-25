package com.rpg.rpghxh.rooms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomResponseDTO {

    private UUID id;
    private String name;
    private String masterName;

    @JsonProperty("isPrivate")
    private boolean isPrivate;

    private String inviteCode;
    private int currentPlayers;
    private int maxPlayers;
    private LocalDateTime createdAt;
}
