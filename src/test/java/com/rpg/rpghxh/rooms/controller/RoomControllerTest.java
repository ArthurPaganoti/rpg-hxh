package com.rpg.rpghxh.rooms.controller;

import com.rpg.rpghxh.config.SecurityConfig;
import com.rpg.rpghxh.login.filter.JwtAuthenticationFilter;
import com.rpg.rpghxh.login.filter.RateLimitFilter;
import com.rpg.rpghxh.rooms.dto.InviteResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.rooms.service.RoomService;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.GlobalExceptionHandler;
import com.rpg.rpghxh.shared.exceptions.InvalidInviteException;
import com.rpg.rpghxh.shared.exceptions.PlayerAlreadyInRoomException;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomFullException;
import com.rpg.rpghxh.shared.exceptions.RoomNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = RoomController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = {SecurityConfig.class, JwtAuthenticationFilter.class, RateLimitFilter.class}
    )
)
@Import({GlobalExceptionHandler.class, RoomControllerTest.TestSecurityConfig.class})
class RoomControllerTest {

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
    private RoomService roomService;

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnCreatedWhenDataIsValid() throws Exception {
        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Sala do Gon")
                .masterName("Gon Freecss")
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(roomService.createRoom(any())).thenReturn(ResponseDTO.success(roomResponse, "Sala criada com sucesso"));

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala do Gon\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sala criada com sucesso"))
                .andExpect(jsonPath("$.content.name").value("Sala do Gon"))
                .andExpect(jsonPath("$.content.masterName").value("Gon Freecss"))
                .andExpect(jsonPath("$.content.currentPlayers").value(1))
                .andExpect(jsonPath("$.content.maxPlayers").value(10));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnValidationErrorWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.name").exists());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnValidationErrorWhenNameIsNull() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldDenyAccessWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala do Gon\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnInviteLinkWhenUserIsMaster() throws Exception {
        UUID roomId = UUID.randomUUID();
        String inviteHash = UUID.randomUUID().toString();

        InviteResponseDTO inviteResponse = InviteResponseDTO.builder()
                .inviteUrl("https://api.rpg.com/rooms/join/" + inviteHash)
                .build();

        when(roomService.getInviteLink(roomId)).thenReturn(ResponseDTO.success(inviteResponse, "Link de convite gerado com sucesso"));

        mockMvc.perform(get("/rooms/" + roomId + "/invite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Link de convite gerado com sucesso"))
                .andExpect(jsonPath("$.content.inviteUrl").value("https://api.rpg.com/rooms/join/" + inviteHash));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenNonMasterRequestsInviteLink() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.getInviteLink(roomId)).thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(get("/rooms/" + roomId + "/invite"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenRoomNotFound() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.getInviteLink(roomId)).thenThrow(new RoomNotFoundException());

        mockMvc.perform(get("/rooms/" + roomId + "/invite"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyInviteLinkAccessWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(get("/rooms/" + roomId + "/invite"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn200WhenPlayerJoinsRoomSuccessfully() throws Exception {
        String hash = UUID.randomUUID().toString();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Sala do Gon")
                .masterName("Gon Freecss")
                .currentPlayers(2)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(roomService.joinRoom(hash)).thenReturn(ResponseDTO.success(roomResponse, "Entrou na sala com sucesso"));

        mockMvc.perform(get("/rooms/join/" + hash))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Entrou na sala com sucesso"))
                .andExpect(jsonPath("$.content.currentPlayers").value(2));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn404WhenInviteHashIsInvalidOrExpired() throws Exception {
        String hash = "hash-invalido";

        when(roomService.joinRoom(hash)).thenThrow(new InvalidInviteException());

        mockMvc.perform(get("/rooms/join/" + hash))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn409WhenRoomIsFull() throws Exception {
        String hash = UUID.randomUUID().toString();

        when(roomService.joinRoom(hash)).thenThrow(new RoomFullException());

        mockMvc.perform(get("/rooms/join/" + hash))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn409WhenPlayerAlreadyInRoom() throws Exception {
        String hash = UUID.randomUUID().toString();

        when(roomService.joinRoom(hash)).thenThrow(new PlayerAlreadyInRoomException());

        mockMvc.perform(get("/rooms/join/" + hash))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyJoinRoomWhenNotAuthenticated() throws Exception {
        String hash = UUID.randomUUID().toString();

        mockMvc.perform(get("/rooms/join/" + hash))
                .andExpect(status().isForbidden());
    }
}
