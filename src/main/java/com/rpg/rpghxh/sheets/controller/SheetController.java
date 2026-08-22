package com.rpg.rpghxh.sheets.controller;

import com.rpg.rpghxh.sheets.dto.SheetDownload;
import com.rpg.rpghxh.sheets.dto.SheetResponseDTO;
import com.rpg.rpghxh.sheets.service.SheetService;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms/{id}")
@Tag(name = "Fichas", description = "Envio e leitura das fichas de personagem da sala")
public class SheetController {

    private final SheetService sheetService;

    public SheetController(SheetService sheetService) {
        this.sheetService = sheetService;
    }

    @PostMapping(value = "/sheet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Enviar/substituir a propria ficha", description = "Envia a ficha do jogador autenticado para a sala (PDF, DOC, DOCX ou ODT, ate 10MB). Cada jogador tem uma unica ficha por sala; um novo envio substitui a anterior. Qualquer membro da sala pode enviar.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ficha enviada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Apenas membros da sala podem enviar ficha",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sala nao encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "413", description = "Arquivo maior que 10MB",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "415", description = "Tipo de arquivo nao permitido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<SheetResponseDTO>> uploadSheet(@PathVariable UUID id,
                                                                     @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(sheetService.uploadSheet(id, file));
    }

    @GetMapping("/sheets")
    @Operation(summary = "Listar fichas da sala", description = "O Mestre ve as fichas de todos os jogadores; um jogador ve apenas a propria ficha.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fichas listadas com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Apenas membros da sala podem acessar",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sala nao encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<List<SheetResponseDTO>>> listSheets(@PathVariable UUID id) {
        return ResponseEntity.ok(sheetService.listSheets(id));
    }

    @GetMapping("/sheets/{userId}/download")
    @Operation(summary = "Baixar uma ficha", description = "Baixa o arquivo da ficha. O dono baixa a propria ficha; o Mestre baixa a de qualquer jogador.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Arquivo da ficha"),
        @ApiResponse(responseCode = "403", description = "Sem permissao para acessar esta ficha",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sala ou ficha nao encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<InputStreamResource> downloadSheet(@PathVariable UUID id, @PathVariable Long userId) {
        SheetDownload download = sheetService.downloadSheet(id, userId);

        String fileName = download.fileName() != null ? download.fileName() : "ficha";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.parseMediaType(download.contentType()))
                .contentLength(download.sizeBytes())
                .body(new InputStreamResource(download.stream()));
    }

    @DeleteMapping("/sheet")
    @Operation(summary = "Remover a propria ficha", description = "Remove a ficha do jogador autenticado da sala. Qualquer membro pode remover a propria ficha.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ficha removida com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "403", description = "Apenas membros da sala podem acessar",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Sala ou ficha nao encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseDTO.class)))
    })
    public ResponseEntity<ResponseDTO<Void>> deleteSheet(@PathVariable UUID id) {
        return ResponseEntity.ok(sheetService.deleteSheet(id));
    }
}