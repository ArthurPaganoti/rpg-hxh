package com.rpg.rpghxh.rooms.dto;

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
@Schema(description = "Dados de um jogador banido da sala")
public class RoomBanResponseDTO {

    @Schema(description = "ID do usuario banido", example = "2")
    private Long id;

    @Schema(description = "Nome do usuario banido", example = "Killua Zoldyck")
    private String name;

    @Schema(description = "Data do banimento", example = "2026-08-22T15:00:00")
    private LocalDateTime bannedAt;
}