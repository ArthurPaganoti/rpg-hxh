package com.rpg.rpghxh.sheets.dto;

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
@Schema(description = "Metadados de uma ficha enviada para a sala")
public class SheetResponseDTO {

    @Schema(description = "ID do dono da ficha", example = "2")
    private Long userId;

    @Schema(description = "Nome do dono da ficha", example = "Killua Zoldyck")
    private String ownerName;

    @Schema(description = "Nome do arquivo enviado", example = "ficha-killua.pdf")
    private String fileName;

    @Schema(description = "Tipo do arquivo", example = "application/pdf")
    private String contentType;

    @Schema(description = "Tamanho do arquivo em bytes", example = "204800")
    private long sizeBytes;

    @Schema(description = "Data do envio", example = "2026-08-22T15:00:00")
    private LocalDateTime uploadedAt;

    @JsonProperty("isMine")
    @Schema(description = "Indica se a ficha e do usuario autenticado", example = "true")
    private boolean mine;
}