package com.rpg.rpghxh.rooms.controller;

import com.rpg.rpghxh.config.SecurityConfig;
import com.rpg.rpghxh.login.filter.JwtAuthenticationFilter;
import com.rpg.rpghxh.login.filter.RateLimitFilter;
import com.rpg.rpghxh.rooms.dto.InviteResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomBanResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomMemberResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.rooms.service.RoomService;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.GlobalExceptionHandler;
import com.rpg.rpghxh.shared.exceptions.InvalidInviteException;
import com.rpg.rpghxh.shared.exceptions.BanNotFoundException;
import com.rpg.rpghxh.shared.exceptions.CannotBanMasterException;
import com.rpg.rpghxh.shared.exceptions.CannotRemoveMasterException;
import com.rpg.rpghxh.shared.exceptions.MasterCannotLeaveRoomException;
import com.rpg.rpghxh.shared.exceptions.PlayerAlreadyInRoomException;
import com.rpg.rpghxh.shared.exceptions.PlayerNotInRoomException;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomFullException;
import com.rpg.rpghxh.shared.exceptions.RoomMembershipRequiredException;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

        mockMvc.perform(post("/rooms/join/" + hash))
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

        mockMvc.perform(post("/rooms/join/" + hash))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn409WhenRoomIsFull() throws Exception {
        String hash = UUID.randomUUID().toString();

        when(roomService.joinRoom(hash)).thenThrow(new RoomFullException());

        mockMvc.perform(post("/rooms/join/" + hash))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn409WhenPlayerAlreadyInRoom() throws Exception {
        String hash = UUID.randomUUID().toString();

        when(roomService.joinRoom(hash)).thenThrow(new PlayerAlreadyInRoomException());

        mockMvc.perform(post("/rooms/join/" + hash))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnValidationErrorWhenMaxPlayersIsOutOfRange() throws Exception {
        mockMvc.perform(post("/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala do Gon\", \"maxPlayers\": 50}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.maxPlayers").exists());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnMyRoomsList() throws Exception {
        RoomResponseDTO room = RoomResponseDTO.builder()
                .id(UUID.randomUUID())
                .name("Sala do Gon")
                .masterName("Gon Freecss")
                .currentPlayers(2)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .master(true)
                .build();

        when(roomService.listMyRooms())
                .thenReturn(ResponseDTO.success(java.util.List.of(room), "Salas listadas com sucesso"));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Salas listadas com sucesso"))
                .andExpect(jsonPath("$.content[0].name").value("Sala do Gon"))
                .andExpect(jsonPath("$.content[0].currentPlayers").value(2))
                .andExpect(jsonPath("$.content[0].isMaster").value(true))
                .andExpect(jsonPath("$.content[0].master").doesNotExist());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnEmptyListWhenUserHasNoRooms() throws Exception {
        when(roomService.listMyRooms())
                .thenReturn(ResponseDTO.success(java.util.List.of(), "Salas listadas com sucesso"));

        mockMvc.perform(get("/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void shouldDenyListRoomsWhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/rooms"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnRoomMembersWithMasterFlag() throws Exception {
        UUID roomId = UUID.randomUUID();

        RoomMemberResponseDTO master = RoomMemberResponseDTO.builder()
                .id(1L)
                .name("Gon Freecss")
                .joinedAt(LocalDateTime.now().minusHours(1))
                .master(true)
                .build();

        RoomMemberResponseDTO player = RoomMemberResponseDTO.builder()
                .id(2L)
                .name("Killua Zoldyck")
                .joinedAt(LocalDateTime.now())
                .master(false)
                .build();

        when(roomService.listRoomMembers(roomId))
                .thenReturn(ResponseDTO.success(java.util.List.of(master, player), "Membros listados com sucesso"));

        mockMvc.perform(get("/rooms/" + roomId + "/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Membros listados com sucesso"))
                .andExpect(jsonPath("$.content[0].name").value("Gon Freecss"))
                .andExpect(jsonPath("$.content[0].isMaster").value(true))
                .andExpect(jsonPath("$.content[0].master").doesNotExist())
                .andExpect(jsonPath("$.content[1].name").value("Killua Zoldyck"))
                .andExpect(jsonPath("$.content[1].isMaster").value(false));
    }

    @Test
    @WithMockUser(username = "hisoka@hunter.com")
    void shouldReturn403WhenNonMemberListsMembers() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.listRoomMembers(roomId)).thenThrow(new RoomMembershipRequiredException());

        mockMvc.perform(get("/rooms/" + roomId + "/members"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"))
                .andExpect(jsonPath("$.message").value("Apenas membros da sala podem acessar"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenListingMembersOfNonexistentRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.listRoomMembers(roomId)).thenThrow(new RoomNotFoundException());

        mockMvc.perform(get("/rooms/" + roomId + "/members"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyListMembersWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(get("/rooms/" + roomId + "/members"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn200WhenMasterUpdatesRoomName() throws Exception {
        UUID roomId = UUID.randomUUID();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .id(roomId)
                .name("Sala Renovada")
                .masterName("Gon Freecss")
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(roomService.updateRoom(eq(roomId), any()))
                .thenReturn(ResponseDTO.success(roomResponse, "Sala atualizada com sucesso"));

        mockMvc.perform(patch("/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala Renovada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sala atualizada com sucesso"))
                .andExpect(jsonPath("$.content.name").value("Sala Renovada"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnValidationErrorWhenUpdateNameIsTooShort() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(patch("/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"ab\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.content.name").exists());
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenNonMasterUpdatesRoomName() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.updateRoom(eq(roomId), any()))
                .thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(patch("/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala Invadida\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenUpdatingNonexistentRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.updateRoom(eq(roomId), any()))
                .thenThrow(new RoomNotFoundException());

        mockMvc.perform(patch("/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala Fantasma\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyUpdateRoomWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(patch("/rooms/" + roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": \"Sala Renovada\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn200WhenMasterDeletesRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.deleteRoom(roomId)).thenReturn(ResponseDTO.success("Sala deletada com sucesso"));

        mockMvc.perform(delete("/rooms/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Sala deletada com sucesso"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenNonMasterDeletesRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.deleteRoom(roomId)).thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(delete("/rooms/" + roomId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenDeletingNonexistentRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.deleteRoom(roomId)).thenThrow(new RoomNotFoundException());

        mockMvc.perform(delete("/rooms/" + roomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyDeleteRoomWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(delete("/rooms/" + roomId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnRoomDetailsWhenMember() throws Exception {
        UUID roomId = UUID.randomUUID();

        RoomResponseDTO roomResponse = RoomResponseDTO.builder()
                .id(roomId)
                .name("Sala do Gon")
                .masterName("Gon Freecss")
                .currentPlayers(2)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .master(true)
                .build();

        when(roomService.getRoom(roomId))
                .thenReturn(ResponseDTO.success(roomResponse, "Sala encontrada com sucesso"));

        mockMvc.perform(get("/rooms/" + roomId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.content.name").value("Sala do Gon"))
                .andExpect(jsonPath("$.content.isMaster").value(true))
                .andExpect(jsonPath("$.content.master").doesNotExist());
    }

    @Test
    @WithMockUser(username = "hisoka@hunter.com")
    void shouldReturn403WhenNonMemberGetsRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.getRoom(roomId)).thenThrow(new RoomMembershipRequiredException());

        mockMvc.perform(get("/rooms/" + roomId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenGettingNonexistentRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.getRoom(roomId)).thenThrow(new RoomNotFoundException());

        mockMvc.perform(get("/rooms/" + roomId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyGetRoomWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(get("/rooms/" + roomId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn200WhenMasterRevokesInvite() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.revokeInvite(roomId))
                .thenReturn(ResponseDTO.success("Convite revogado com sucesso"));

        mockMvc.perform(delete("/rooms/" + roomId + "/invite"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Convite revogado com sucesso"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenNonMasterRevokesInvite() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.revokeInvite(roomId)).thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(delete("/rooms/" + roomId + "/invite"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenRevokingInviteOfNonexistentRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.revokeInvite(roomId)).thenThrow(new RoomNotFoundException());

        mockMvc.perform(delete("/rooms/" + roomId + "/invite"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyRevokeInviteWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(delete("/rooms/" + roomId + "/invite"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn200WhenMasterRemovesMember() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.removeMember(roomId, 2L))
                .thenReturn(ResponseDTO.success("Jogador removido da sala com sucesso"));

        mockMvc.perform(delete("/rooms/" + roomId + "/members/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jogador removido da sala com sucesso"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenNonMasterRemovesMember() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.removeMember(roomId, 2L)).thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(delete("/rooms/" + roomId + "/members/2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn409WhenRemovingMaster() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.removeMember(roomId, 1L)).thenThrow(new CannotRemoveMasterException());

        mockMvc.perform(delete("/rooms/" + roomId + "/members/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenRemovingPlayerNotInRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.removeMember(roomId, 2L)).thenThrow(new PlayerNotInRoomException());

        mockMvc.perform(delete("/rooms/" + roomId + "/members/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyRemoveMemberWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(delete("/rooms/" + roomId + "/members/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn200WhenMemberLeavesRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.leaveRoom(roomId)).thenReturn(ResponseDTO.success("Voce saiu da sala com sucesso"));

        mockMvc.perform(post("/rooms/" + roomId + "/leave"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Voce saiu da sala com sucesso"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn409WhenMasterLeavesRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.leaveRoom(roomId)).thenThrow(new MasterCannotLeaveRoomException());

        mockMvc.perform(post("/rooms/" + roomId + "/leave"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "hisoka@hunter.com")
    void shouldReturn403WhenNonMemberLeavesRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.leaveRoom(roomId)).thenThrow(new RoomMembershipRequiredException());

        mockMvc.perform(post("/rooms/" + roomId + "/leave"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenLeavingNonexistentRoom() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.leaveRoom(roomId)).thenThrow(new RoomNotFoundException());

        mockMvc.perform(post("/rooms/" + roomId + "/leave"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyLeaveRoomWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(post("/rooms/" + roomId + "/leave"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn200WhenMasterBansUser() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.banUser(roomId, 2L))
                .thenReturn(ResponseDTO.success("Jogador banido da sala com sucesso"));

        mockMvc.perform(post("/rooms/" + roomId + "/bans/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Jogador banido da sala com sucesso"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenNonMasterBansUser() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.banUser(roomId, 2L)).thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(post("/rooms/" + roomId + "/bans/2"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn409WhenBanningMaster() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.banUser(roomId, 1L)).thenThrow(new CannotBanMasterException());

        mockMvc.perform(post("/rooms/" + roomId + "/bans/1"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyBanUserWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(post("/rooms/" + roomId + "/bans/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn200WhenMasterUnbansUser() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.unbanUser(roomId, 2L))
                .thenReturn(ResponseDTO.success("Banimento removido com sucesso"));

        mockMvc.perform(delete("/rooms/" + roomId + "/bans/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Banimento removido com sucesso"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturn404WhenUnbanningNonBannedUser() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.unbanUser(roomId, 2L)).thenThrow(new BanNotFoundException());

        mockMvc.perform(delete("/rooms/" + roomId + "/bans/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    @WithMockUser(username = "gon@hunter.com")
    void shouldReturnBanListWhenMaster() throws Exception {
        UUID roomId = UUID.randomUUID();

        RoomBanResponseDTO ban = RoomBanResponseDTO.builder()
                .id(2L)
                .name("Killua Zoldyck")
                .bannedAt(LocalDateTime.now())
                .build();

        when(roomService.listBans(roomId))
                .thenReturn(ResponseDTO.success(List.of(ban), "Banidos listados com sucesso"));

        mockMvc.perform(get("/rooms/" + roomId + "/bans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Killua Zoldyck"));
    }

    @Test
    @WithMockUser(username = "killua@hunter.com")
    void shouldReturn403WhenNonMasterListsBans() throws Exception {
        UUID roomId = UUID.randomUUID();

        when(roomService.listBans(roomId)).thenThrow(new RoomAccessDeniedException());

        mockMvc.perform(get("/rooms/" + roomId + "/bans"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("BUSINESS_ERROR"));
    }

    @Test
    void shouldDenyListBansWhenNotAuthenticated() throws Exception {
        UUID roomId = UUID.randomUUID();

        mockMvc.perform(get("/rooms/" + roomId + "/bans"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDenyJoinRoomWhenNotAuthenticated() throws Exception {
        String hash = UUID.randomUUID().toString();

        mockMvc.perform(post("/rooms/join/" + hash))
                .andExpect(status().isForbidden());
    }
}
