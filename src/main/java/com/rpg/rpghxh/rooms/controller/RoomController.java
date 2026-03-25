package com.rpg.rpghxh.rooms.controller;

import com.rpg.rpghxh.rooms.dto.CreateRoomDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.rooms.service.RoomService;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
@Tag(name = "Salas", description = "Gerenciamento de salas de RPG")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @Operation(summary = "Criar nova sala", description = "Cria uma nova sala de RPG. O usuario autenticado se torna o Mestre da sala.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Sala criada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Erro de validacao: nome da sala vazio",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<RoomResponseDTO>> createRoom(@Valid @RequestBody CreateRoomDTO dto) {
        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
