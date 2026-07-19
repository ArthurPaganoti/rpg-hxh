package com.rpg.rpghxh.rooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Dados para criacao de uma nova sala")
public class CreateRoomDTO {

    @NotBlank(message = "O nome da sala e obrigatorio")
    @Size(min = 3, max = 100, message = "O nome da sala deve ter entre 3 e 100 caracteres")
    @Schema(description = "Nome da sala", example = "Sala do Gon")
    private String name;

    @Min(value = 2, message = "A sala deve permitir no minimo 2 jogadores")
    @Max(value = 10, message = "A sala deve permitir no maximo 10 jogadores")
    @Schema(description = "Numero maximo de jogadores (opcional, padrao 10)", example = "10")
    private Integer maxPlayers;
}
