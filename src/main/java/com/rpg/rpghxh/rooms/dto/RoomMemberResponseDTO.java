package com.rpg.rpghxh.rooms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados de um membro da sala")
public class RoomMemberResponseDTO {

    @Schema(description = "ID do usuario", example = "1")
    private Long id;

    @Schema(description = "Nome do usuario", example = "Gon Freecss")
    private String name;

    @Schema(description = "Data de entrada na sala", example = "2026-08-01T12:00:00")
    private LocalDateTime joinedAt;

    @JsonProperty("isMaster")
    @Schema(description = "Indica se o membro e o Mestre da sala", example = "true")
    private boolean master;
}
