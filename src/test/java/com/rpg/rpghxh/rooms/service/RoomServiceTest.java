package com.rpg.rpghxh.rooms.service;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomPlayer;
import com.rpg.rpghxh.entities.room.repository.RoomPlayerRepository;
import com.rpg.rpghxh.entities.room.repository.RoomRepository;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.rooms.dto.CreateRoomDTO;
import com.rpg.rpghxh.rooms.dto.InviteResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.InvalidInviteException;
import com.rpg.rpghxh.shared.exceptions.PlayerAlreadyInRoomException;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomFullException;
import com.rpg.rpghxh.shared.exceptions.RoomNotFoundException;
import com.rpg.rpghxh.shared.exceptions.UserNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomPlayerRepository roomPlayerRepository;

    @Mock
    private RedisInviteService redisInviteService;

    private RoomService roomService;

    private User masterUser;

    @BeforeEach
    void setUp() {
        roomService = new RoomService(roomRepository, userRepository, roomPlayerRepository,
                redisInviteService, "https://api.rpg.com/rooms/join/");

        masterUser = User.builder()
                .id(1L)
                .name("Gon Freecss")
                .email("gon@hunter.com")
                .senha("encodedPassword")
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("gon@hunter.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRoom_ShouldReturnSuccessWithRoomData() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala do Gon")
                .build();

        UUID roomId = UUID.randomUUID();
        Room savedRoom = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
        when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(RoomPlayer.builder().build());
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertTrue(response.isSuccess());
        assertEquals("Sala criada com sucesso", response.getMessage());
        assertNotNull(response.getContent());
        assertEquals("Sala do Gon", response.getContent().getName());
        assertEquals("Gon Freecss", response.getContent().getMasterName());
        assertEquals(1, response.getContent().getCurrentPlayers());
        assertEquals(10, response.getContent().getMaxPlayers());
        assertNotNull(response.getContent().getCreatedAt());
    }

    @Test
    void createRoom_UserNotFound_ShouldThrowUserNotFoundException() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala do Gon")
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> roomService.createRoom(dto));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void createRoom_ShouldSetAuthenticatedUserAsMaster() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala do Mestre")
                .build();

        UUID roomId = UUID.randomUUID();
        Room savedRoom = Room.builder()
                .id(roomId)
                .name("Sala do Mestre")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
        when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(RoomPlayer.builder().build());
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertEquals("Gon Freecss", response.getContent().getMasterName());

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertEquals(masterUser, roomCaptor.getValue().getMaster());

        ArgumentCaptor<RoomPlayer> playerCaptor = ArgumentCaptor.forClass(RoomPlayer.class);
        verify(roomPlayerRepository).save(playerCaptor.capture());
        assertEquals(masterUser, playerCaptor.getValue().getUser());
    }

    @Test
    void createRoom_ShouldDefaultToOneCurrentPlayerAndTenMaxPlayers() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala Padrao")
                .build();

        UUID roomId = UUID.randomUUID();
        Room savedRoom = Room.builder()
                .id(roomId)
                .name("Sala Padrao")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
        when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(RoomPlayer.builder().build());
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertEquals(1, response.getContent().getCurrentPlayers());
        assertEquals(10, response.getContent().getMaxPlayers());
    }

    @Test
    void getInviteLink_AsMaster_ShouldReturnInviteUrl() {
        UUID roomId = UUID.randomUUID();
        String inviteHash = UUID.randomUUID().toString();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(redisInviteService.getOrCreateInvite(eq(roomId), eq(1L))).thenReturn(inviteHash);

        ResponseDTO<InviteResponseDTO> response = roomService.getInviteLink(roomId);

        assertTrue(response.isSuccess());
        assertEquals("Link de convite gerado com sucesso", response.getMessage());
        assertEquals("https://api.rpg.com/rooms/join/" + inviteHash, response.getContent().getInviteUrl());
    }

    @Test
    void getInviteLink_AsMaster_ShouldDelegateToRedisInviteService() {
        UUID roomId = UUID.randomUUID();
        String inviteHash = UUID.randomUUID().toString();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(redisInviteService.getOrCreateInvite(eq(roomId), eq(1L))).thenReturn(inviteHash);

        roomService.getInviteLink(roomId);

        verify(redisInviteService).getOrCreateInvite(roomId, 1L);
    }

    @Test
    void getInviteLink_AsNonMaster_ShouldThrowRoomAccessDeniedException() {
        UUID roomId = UUID.randomUUID();

        User otherUser = User.builder()
                .id(2L)
                .name("Killua Zoldyck")
                .email("gon@hunter.com")
                .build();

        User roomMaster = User.builder()
                .id(99L)
                .name("Outro Mestre")
                .email("master@hunter.com")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Outro")
                .master(roomMaster)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(otherUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThrows(RoomAccessDeniedException.class, () -> roomService.getInviteLink(roomId));
        verify(redisInviteService, never()).getOrCreateInvite(any(), any());
    }

    @Test
    void getInviteLink_RoomNotFound_ShouldThrowRoomNotFoundException() {
        UUID roomId = UUID.randomUUID();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.getInviteLink(roomId));
    }

    // --- joinRoom tests ---

    @Test
    void joinRoom_ShouldAddPlayerAndReturnRoomData() {
        String hash = UUID.randomUUID().toString();
        UUID roomId = UUID.randomUUID();

        User player = User.builder().id(2L).name("Killua Zoldyck").email("gon@hunter.com").build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("gon@hunter.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        Room updatedRoom = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(2)
                .maxPlayers(10)
                .createdAt(room.getCreatedAt())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(player));
        when(redisInviteService.getRoomIdByHash(hash)).thenReturn(Optional.of(roomId));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(false);
        when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(RoomPlayer.builder().build());
        when(roomPlayerRepository.countByRoom(room)).thenReturn(2L);
        when(roomRepository.save(room)).thenReturn(updatedRoom);

        ResponseDTO<RoomResponseDTO> response = roomService.joinRoom(hash);

        assertTrue(response.isSuccess());
        assertEquals("Entrou na sala com sucesso", response.getMessage());
        assertEquals(2, response.getContent().getCurrentPlayers());
    }

    @Test
    void joinRoom_InvalidHash_ShouldThrowInvalidInviteException() {
        String hash = "hash-invalido";

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(redisInviteService.getRoomIdByHash(hash)).thenReturn(Optional.empty());

        assertThrows(InvalidInviteException.class, () -> roomService.joinRoom(hash));
        verify(roomRepository, never()).findById(any());
    }

    @Test
    void joinRoom_AsMaster_ShouldThrowPlayerAlreadyInRoomException() {
        String hash = UUID.randomUUID().toString();
        UUID roomId = UUID.randomUUID();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(redisInviteService.getRoomIdByHash(hash)).thenReturn(Optional.of(roomId));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));

        assertThrows(PlayerAlreadyInRoomException.class, () -> roomService.joinRoom(hash));
        verify(roomPlayerRepository, never()).save(any());
    }

    @Test
    void joinRoom_PlayerAlreadyInRoom_ShouldThrowPlayerAlreadyInRoomException() {
        String hash = UUID.randomUUID().toString();
        UUID roomId = UUID.randomUUID();

        User player = User.builder().id(2L).name("Killua Zoldyck").email("gon@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(2)
                .maxPlayers(10)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(player));
        when(redisInviteService.getRoomIdByHash(hash)).thenReturn(Optional.of(roomId));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);

        assertThrows(PlayerAlreadyInRoomException.class, () -> roomService.joinRoom(hash));
        verify(roomPlayerRepository, never()).save(any());
    }

    @Test
    void deleteRoom_AsMaster_ShouldDeleteRoomPlayersAndInvite() {
        UUID roomId = UUID.randomUUID();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        ResponseDTO<Void> response = roomService.deleteRoom(roomId);

        assertTrue(response.isSuccess());
        assertEquals("Sala deletada com sucesso", response.getMessage());
        verify(redisInviteService).removeInvite(roomId);
        verify(roomPlayerRepository).deleteByRoom(room);
        verify(roomRepository).delete(room);
    }

    @Test
    void deleteRoom_AsNonMaster_ShouldThrowRoomAccessDeniedException() {
        UUID roomId = UUID.randomUUID();

        User otherUser = User.builder()
                .id(2L)
                .name("Killua Zoldyck")
                .email("gon@hunter.com")
                .build();

        User roomMaster = User.builder()
                .id(99L)
                .name("Outro Mestre")
                .email("master@hunter.com")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Outro")
                .master(roomMaster)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(otherUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThrows(RoomAccessDeniedException.class, () -> roomService.deleteRoom(roomId));
        verify(roomRepository, never()).delete(any(Room.class));
        verify(redisInviteService, never()).removeInvite(any());
    }

    @Test
    void deleteRoom_RoomNotFound_ShouldThrowRoomNotFoundException() {
        UUID roomId = UUID.randomUUID();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.deleteRoom(roomId));
        verify(roomRepository, never()).delete(any(Room.class));
    }

    @Test
    void joinRoom_RoomFull_ShouldThrowRoomFullException() {
        String hash = UUID.randomUUID().toString();
        UUID roomId = UUID.randomUUID();

        User player = User.builder().id(2L).name("Killua Zoldyck").email("gon@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(10)
                .maxPlayers(10)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(player));
        when(redisInviteService.getRoomIdByHash(hash)).thenReturn(Optional.of(roomId));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(false);

        assertThrows(RoomFullException.class, () -> roomService.joinRoom(hash));
        verify(roomPlayerRepository, never()).save(any());
    }
}
