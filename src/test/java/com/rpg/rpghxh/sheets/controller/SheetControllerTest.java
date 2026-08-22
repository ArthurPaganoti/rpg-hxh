package com.rpg.rpghxh.sheets.controller;

import com.rpg.rpghxh.config.SecurityConfig;
import com.rpg.rpghxh.login.filter.JwtAuthenticationFilter;
import com.rpg.rpghxh.login.filter.RateLimitFilter;
import com.rpg.rpghxh.sheets.dto.SheetDownload;
import com.rpg.rpghxh.sheets.dto.SheetResponseDTO;
import com.rpg.rpghxh.sheets.service.SheetService;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.GlobalExceptionHandler;
import com.rpg.rpghxh.shared.exceptions.InvalidFileTypeException;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomMembershipRequiredException;
import com.rpg.rpghxh.shared.exceptions.SheetNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = SheetController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitFilter.class}
    )
)
@Import({GlobalExceptionHandler.class, SheetControllerTest.TestSecurityConfig.class})
class SheetControllerTest {

    @org.springframework.boot.test.context.TestConfiguration
    static class TestSecurityConfig {
        @Bean
        public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SheetService sheetService;

    private MockMultipartFile pdf() {
        return new MockMultipartFile("file", "ficha.pdf", "application/pdf", "conteudo".getBytes());
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn200WhenMemberUploadsSheet() throws Exception {
        UUID roomId = UUID.randomUUID();
        SheetResponseDTO dto = SheetResponseDTO.builder()
                .userId(2L).ownerName("Killua Zoldyck").fileName("ficha.pdf")
                .contentType("application/pdf").sizeBytes(8).mine(true).build();

        when(sheetService.uploadSheet(eq(roomId), any()))
                .thenReturn(ResponseDTO.success(dto, "Ficha enviada com sucesso"));

        mockMvc.perform(multipart("/rooms/" + roomId + "/sheet").file(pdf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.content.fileName").value("ficha.pdf"))
                .andExpect(jsonPath("$.content.isMine").value(true))
                .andExpect(jsonPath("$.content.mine").doesNotExist());
    }

    @Test
    @WithMockUser(username = "hisoka@hunter.com")
    void shouldReturn403WhenNonMemberUploadsSheet() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(sheetService.uploadSheet(eq(roomId), any())).thenThrow(new RoomMembershipRequiredException());

        mockMvc.perform(multipart("/rooms/" + roomId + "/sheet").file(pdf()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn415WhenFileTypeInvalid() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(sheetService.uploadSheet(eq(roomId), any())).thenThrow(new InvalidFileTypeException());

        MockMultipartFile png = new MockMultipartFile("file", "x.png", "image/png", "x".getBytes());

        mockMvc.perform(multipart("/rooms/" + roomId + "/sheet").file(png))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyUploadWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(multipart("/rooms/" + roomId + "/sheet").file(pdf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnSheetListWhenMaster() throws Exception {
        UUID roomId = UUID.randomUUID();
        SheetResponseDTO dto = SheetResponseDTO.builder()
                .userId(2L).ownerName("Killua Zoldyck").fileName("k.pdf")
                .contentType("application/pdf").sizeBytes(8).mine(false).build();

        when(sheetService.listSheets(roomId))
                .thenReturn(ResponseDTO.success(List.of(dto), "Fichas listadas com sucesso"));

        mockMvc.perform(get("/rooms/" + roomId + "/sheets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].ownerName").value("Killua Zoldyck"))
                .andExpect(jsonPath("$.content[0].isMine").value(false));
    }

    @Test
    @WithMockUser(username = "hisoka@hunter.com")
    void shouldReturn403WhenNonMemberListsSheets() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(sheetService.listSheets(roomId)).thenThrow(new RoomMembershipRequiredException());

        mockMvc.perform(get("/rooms/" + roomId + "/sheets"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturnFileWhenDownloadingOwnSheet() throws Exception {
        UUID roomId = UUID.randomUUID();
        SheetDownload download = new SheetDownload(
                new ByteArrayInputStream("data".getBytes()), "k.pdf", "application/pdf", 4);

        when(sheetService.downloadSheet(roomId, 2L)).thenReturn(download);

        mockMvc.perform(get("/rooms/" + roomId + "/sheets/2/download"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"k.pdf\""));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenDownloadingOthersSheetAsMember() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(sheetService.downloadSheet(roomId, 99L)).thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(get("/rooms/" + roomId + "/sheets/99/download"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn404WhenDownloadingMissingSheet() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(sheetService.downloadSheet(roomId, 2L)).thenThrow(new SheetNotFoundException());

        mockMvc.perform(get("/rooms/" + roomId + "/sheets/2/download"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn200WhenDeletingOwnSheet() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(sheetService.deleteSheet(roomId)).thenReturn(ResponseDTO.success("Ficha removida com sucesso"));

        mockMvc.perform(delete("/rooms/" + roomId + "/sheet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Ficha removida com sucesso"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn404WhenDeletingMissingSheet() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(sheetService.deleteSheet(roomId)).thenThrow(new SheetNotFoundException());

        mockMvc.perform(delete("/rooms/" + roomId + "/sheet"))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldDenyListSheetsWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(get("/rooms/" + roomId + "/sheets"))
                .andExpect(status().isForbidden());
    }
}
