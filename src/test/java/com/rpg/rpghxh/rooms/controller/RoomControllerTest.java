package com.rpg.rpghxh.rooms.controller;

import com.rpg.rpghxh.config.SecurityConfig;
import com.rpg.rpghxh.login.filter.JwtAuthenticationFilter;
import com.rpg.rpghxh.login.filter.RateLimitFilter;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.rooms.service.RoomService;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
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

    @TestConfiguration
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
                .isPrivate(false)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(roomService.createRoom(any())).thenReturn(ResponseDTO.success(roomResponse, "Sala criada com sucesso"));

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala do Gon\", \"isPrivate\": false}"))
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
    void shouldReturnCreatedWhenPrivateRoomIsCreated() throws Exception {
        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Sala Secreta")
                .masterName("Gon Freecss")
                .isPrivate(true)
                .inviteCode("Ab3xZ9")
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(roomService.createRoom(any())).thenReturn(ResponseDTO.success(roomResponse, "Sala criada com sucesso"));

        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala Secreta\", \"isPrivate\": true}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.content.isPrivate").value(true))
                .andExpect(jsonPath("$.content.inviteCode").value("Ab3xZ9"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnValidationErrorWhenNameIsBlank() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"\", \"isPrivate\": false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.name").exists());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnValidationErrorWhenNameIsNull() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"isPrivate\": false}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void shouldDenyAccessWhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala do Gon\", \"isPrivate\": false}"))
                .andExpect(status().isForbidden());
    }
}
