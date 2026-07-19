package com.rpg.rpghxh.rooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Dados para atualizacao de uma sala")
public class UpdateRoomDTO {

    @NotBlank(message = "O nome da sala e obrigatorio")
    @Size(min = 3, max = 100, message = "O nome da sala deve ter entre 3 e 100 caracteres")
    @Schema(description = "Novo nome da sala", example = "Sala do Gon Renovada")
    private String name;
}