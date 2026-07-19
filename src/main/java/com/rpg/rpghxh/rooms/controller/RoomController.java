package com.rpg.rpghxh.rooms.controller;

import com.rpg.rpghxh.rooms.dto.CreateRoomDTO;
import com.rpg.rpghxh.rooms.dto.InviteResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.rooms.dto.UpdateRoomDTO;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
@Tag(name = "Salas", description = "Gerenciamento de salas de RPG")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @Operation(summary = "Criar nova sala", description = "Cria uma nova sala de RPG. O usuario autenticado se torna o Mestre da sala. Todas as salas sao privadas.")
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

    @GetMapping
    @Operation(summary = "Listar minhas salas", description = "Lista todas as salas em que o usuario autenticado participa, como Mestre ou jogador, ordenadas da mais recente para a mais antiga.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Salas listadas com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<List<RoomResponseDTO>>> listMyRooms() {
        ResponseDTO<List<RoomResponseDTO>> response = roomService.listMyRooms();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/invite")
    @Operation(summary = "Gerar link de convite", description = "Gera ou recupera o link de convite da sala. Apenas o Mestre da sala pode acessar.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Link de convite gerado com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Apenas o Mestre da sala pode gerar o link de convite",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sala nao encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<InviteResponseDTO>> getInviteLink(@PathVariable UUID id) {
        ResponseDTO<InviteResponseDTO> response = roomService.getInviteLink(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar sala", description = "Atualiza o nome e, opcionalmente, o numero maximo de jogadores da sala. Apenas o Mestre da sala pode atualizar.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sala atualizada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Erro de validacao: nome invalido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Apenas o Mestre da sala pode atualizar a sala",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sala nao encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Maximo de jogadores menor que o numero atual de jogadores",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<RoomResponseDTO>> updateRoom(@PathVariable UUID id,
                                                                   @Valid @RequestBody UpdateRoomDTO dto) {
        ResponseDTO<RoomResponseDTO> response = roomService.updateRoom(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar sala", description = "Deleta a sala, removendo todos os jogadores e o convite ativo no Redis. Apenas o Mestre da sala pode deletar.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sala deletada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Apenas o Mestre da sala pode deletar a sala",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sala nao encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<Void>> deleteRoom(@PathVariable UUID id) {
        ResponseDTO<Void> response = roomService.deleteRoom(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/join/{hash}")
    @Operation(summary = "Entrar em uma sala via convite", description = "Adiciona o jogador autenticado a uma sala usando o hash do link de convite.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entrou na sala com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Convite invalido ou expirado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Sala cheia ou jogador ja esta na sala",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "401", description = "Token JWT ausente ou invalido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<RoomResponseDTO>> joinRoom(@PathVariable String hash) {
        ResponseDTO<RoomResponseDTO> response = roomService.joinRoom(hash);
        return ResponseEntity.ok(response);
    }
}
