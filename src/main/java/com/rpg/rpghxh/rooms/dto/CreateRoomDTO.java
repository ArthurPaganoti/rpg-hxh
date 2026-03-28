package com.rpg.rpghxh.rooms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @Schema(description = "Nome da sala", example = "Sala do Gon")
    private String name;
}
