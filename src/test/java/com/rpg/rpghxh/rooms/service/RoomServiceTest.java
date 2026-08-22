package com.rpg.rpghxh.rooms.service;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomPlayer;
import com.rpg.rpghxh.entities.room.repository.RoomPlayerRepository;
import com.rpg.rpghxh.entities.room.repository.RoomRepository;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.rooms.dto.CreateRoomDTO;
import com.rpg.rpghxh.rooms.dto.InviteResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomMemberResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.rooms.dto.UpdateRoomDTO;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.CannotRemoveMasterException;
import com.rpg.rpghxh.shared.exceptions.InvalidInviteException;
import com.rpg.rpghxh.shared.exceptions.MasterCannotLeaveRoomException;
import com.rpg.rpghxh.shared.exceptions.PlayerNotInRoomException;
import com.rpg.rpghxh.shared.exceptions.MaxPlayersBelowCurrentException;
import com.rpg.rpghxh.shared.exceptions.PlayerAlreadyInRoomException;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomFullException;
import com.rpg.rpghxh.shared.exceptions.RoomMembershipRequiredException;
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
    void createRoom_WithCustomMaxPlayers_ShouldUseProvidedValue() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala Grande")
                .maxPlayers(8)
                .build();

        UUID roomId = UUID.randomUUID();
        Room savedRoom = Room.builder()
                .id(roomId)
                .name("Sala Grande")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(8)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
        when(roomPlayerRepository.save(any(RoomPlayer.class))).thenReturn(RoomPlayer.builder().build());
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(savedRoom));

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertEquals(8, response.getContent().getMaxPlayers());

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertEquals(8, roomCaptor.getValue().getMaxPlayers());
    }

    @Test
    void listMyRooms_ShouldReturnRoomsOrderedFromRepository() {
        Room room1 = Room.builder()
                .id(UUID.randomUUID())
                .name("Sala Nova")
                .master(masterUser)
                .currentPlayers(2)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        Room room2 = Room.builder()
                .id(UUID.randomUUID())
                .name("Sala Antiga")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomPlayerRepository.findRoomsByUser(masterUser)).thenReturn(List.of(room1, room2));

        ResponseDTO<List<RoomResponseDTO>> response = roomService.listMyRooms();

        assertTrue(response.isSuccess());
        assertEquals("Salas listadas com sucesso", response.getMessage());
        assertEquals(2, response.getContent().size());
        assertEquals("Sala Nova", response.getContent().get(0).getName());
        assertEquals("Sala Antiga", response.getContent().get(1).getName());
        assertEquals("Gon Freecss", response.getContent().get(0).getMasterName());
    }

    @Test
    void listMyRooms_WithNoRooms_ShouldReturnEmptyList() {
        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomPlayerRepository.findRoomsByUser(masterUser)).thenReturn(List.of());

        ResponseDTO<List<RoomResponseDTO>> response = roomService.listMyRooms();

        assertTrue(response.isSuccess());
        assertTrue(response.getContent().isEmpty());
    }

    @Test
    void listMyRooms_UserNotFound_ShouldThrowUserNotFoundException() {
        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> roomService.listMyRooms());
        verify(roomPlayerRepository, never()).findRoomsByUser(any());
    }

    @Test
    void updateRoomName_AsMaster_ShouldUpdateAndReturnRoomData() {
        UUID roomId = UUID.randomUUID();

        UpdateRoomDTO dto = UpdateRoomDTO.builder()
                .name("Sala Renovada")
                .build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        ResponseDTO<RoomResponseDTO> response = roomService.updateRoom(roomId, dto);

        assertTrue(response.isSuccess());
        assertEquals("Sala atualizada com sucesso", response.getMessage());
        assertEquals("Sala Renovada", response.getContent().getName());
        verify(roomRepository).save(room);
    }

    @Test
    void updateRoomName_AsNonMaster_ShouldThrowRoomAccessDeniedException() {
        UUID roomId = UUID.randomUUID();

        UpdateRoomDTO dto = UpdateRoomDTO.builder()
                .name("Sala Invadida")
                .build();

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

        assertThrows(RoomAccessDeniedException.class, () -> roomService.updateRoom(roomId, dto));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void updateRoomName_RoomNotFound_ShouldThrowRoomNotFoundException() {
        UUID roomId = UUID.randomUUID();

        UpdateRoomDTO dto = UpdateRoomDTO.builder()
                .name("Sala Fantasma")
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.updateRoom(roomId, dto));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void updateRoom_WithMaxPlayers_ShouldUpdateBothFields() {
        UUID roomId = UUID.randomUUID();

        UpdateRoomDTO dto = UpdateRoomDTO.builder()
                .name("Sala Maior")
                .maxPlayers(8)
                .build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(3)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomRepository.save(room)).thenReturn(room);

        ResponseDTO<RoomResponseDTO> response = roomService.updateRoom(roomId, dto);

        assertEquals("Sala Maior", response.getContent().getName());
        assertEquals(8, response.getContent().getMaxPlayers());
    }

    @Test
    void updateRoom_MaxPlayersBelowCurrent_ShouldThrowMaxPlayersBelowCurrentException() {
        UUID roomId = UUID.randomUUID();

        UpdateRoomDTO dto = UpdateRoomDTO.builder()
                .name("Sala Encolhida")
                .maxPlayers(2)
                .build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(5)
                .maxPlayers(10)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        assertThrows(MaxPlayersBelowCurrentException.class, () -> roomService.updateRoom(roomId, dto));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void listRoomMembers_AsMember_ShouldReturnMembersWithMasterFlag() {
        UUID roomId = UUID.randomUUID();

        User player = User.builder().id(2L).name("Killua Zoldyck").email("gon@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        RoomPlayer masterEntry = RoomPlayer.builder().room(room).user(masterUser).joinedAt(LocalDateTime.now().minusHours(1)).build();
        RoomPlayer playerEntry = RoomPlayer.builder().room(room).user(player).joinedAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(roomPlayerRepository.findByRoomWithUser(room)).thenReturn(List.of(masterEntry, playerEntry));

        ResponseDTO<List<RoomMemberResponseDTO>> response = roomService.listRoomMembers(roomId);

        assertTrue(response.isSuccess());
        assertEquals("Membros listados com sucesso", response.getMessage());
        assertEquals(2, response.getContent().size());
        assertEquals("Gon Freecss", response.getContent().get(0).getName());
        assertTrue(response.getContent().get(0).isMaster());
        assertEquals("Killua Zoldyck", response.getContent().get(1).getName());
        assertFalse(response.getContent().get(1).isMaster());
    }

    @Test
    void listRoomMembers_AsMaster_ShouldReturnMembers() {
        UUID roomId = UUID.randomUUID();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        RoomPlayer masterEntry = RoomPlayer.builder().room(room).user(masterUser).joinedAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, masterUser)).thenReturn(true);
        when(roomPlayerRepository.findByRoomWithUser(room)).thenReturn(List.of(masterEntry));

        ResponseDTO<List<RoomMemberResponseDTO>> response = roomService.listRoomMembers(roomId);

        assertEquals(1, response.getContent().size());
        assertTrue(response.getContent().get(0).isMaster());
    }

    @Test
    void listRoomMembers_AsNonMember_ShouldThrowRoomMembershipRequiredException() {
        UUID roomId = UUID.randomUUID();

        User outsider = User.builder().id(3L).name("Hisoka Morow").email("gon@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(outsider));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, outsider)).thenReturn(false);

        assertThrows(RoomMembershipRequiredException.class, () -> roomService.listRoomMembers(roomId));
        verify(roomPlayerRepository, never()).findByRoomWithUser(any());
    }

    @Test
    void listRoomMembers_RoomNotFound_ShouldThrowRoomNotFoundException() {
        UUID roomId = UUID.randomUUID();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.listRoomMembers(roomId));
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

    // --- removeMember tests ---

    @Test
    void removeMember_AsMaster_ShouldRemovePlayerAndRecalculateCurrentPlayers() {
        UUID roomId = UUID.randomUUID();

        User target = User.builder().id(2L).name("Killua Zoldyck").email("killua@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(2)
                .maxPlayers(10)
                .build();

        RoomPlayer targetEntry = RoomPlayer.builder().room(room).user(target).joinedAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(roomPlayerRepository.findByRoomAndUser(room, target)).thenReturn(Optional.of(targetEntry));
        when(roomPlayerRepository.countByRoom(room)).thenReturn(1L);
        when(roomRepository.save(room)).thenReturn(room);

        ResponseDTO<Void> response = roomService.removeMember(roomId, 2L);

        assertTrue(response.isSuccess());
        assertEquals("Jogador removido da sala com sucesso", response.getMessage());
        verify(roomPlayerRepository).delete(targetEntry);

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertEquals(1, roomCaptor.getValue().getCurrentPlayers());
    }

    @Test
    void removeMember_AsNonMaster_ShouldThrowRoomAccessDeniedException() {
        UUID roomId = UUID.randomUUID();

        User requester = User.builder().id(2L).name("Killua Zoldyck").email("gon@hunter.com").build();

        User roomMaster = User.builder().id(99L).name("Outro Mestre").email("master@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Outro")
                .master(roomMaster)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(requester));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));

        assertThrows(RoomAccessDeniedException.class, () -> roomService.removeMember(roomId, 3L));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
    }

    @Test
    void removeMember_TargetIsMaster_ShouldThrowCannotRemoveMasterException() {
        UUID roomId = UUID.randomUUID();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));

        assertThrows(CannotRemoveMasterException.class, () -> roomService.removeMember(roomId, 1L));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
    }

    @Test
    void removeMember_TargetUserNotFound_ShouldThrowUserNotFoundException() {
        UUID roomId = UUID.randomUUID();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> roomService.removeMember(roomId, 2L));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
    }

    @Test
    void removeMember_TargetNotInRoom_ShouldThrowPlayerNotInRoomException() {
        UUID roomId = UUID.randomUUID();

        User target = User.builder().id(2L).name("Killua Zoldyck").email("killua@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(roomPlayerRepository.findByRoomAndUser(room, target)).thenReturn(Optional.empty());

        assertThrows(PlayerNotInRoomException.class, () -> roomService.removeMember(roomId, 2L));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
    }

    @Test
    void removeMember_RoomNotFound_ShouldThrowRoomNotFoundException() {
        UUID roomId = UUID.randomUUID();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.removeMember(roomId, 2L));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
    }

    // --- leaveRoom tests ---

    @Test
    void leaveRoom_AsMember_ShouldRemovePlayerAndRecalculateCurrentPlayers() {
        UUID roomId = UUID.randomUUID();

        User player = User.builder().id(2L).name("Killua Zoldyck").email("gon@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(2)
                .maxPlayers(10)
                .build();

        RoomPlayer playerEntry = RoomPlayer.builder().room(room).user(player).joinedAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.findByRoomAndUser(room, player)).thenReturn(Optional.of(playerEntry));
        when(roomPlayerRepository.countByRoom(room)).thenReturn(1L);
        when(roomRepository.save(room)).thenReturn(room);

        ResponseDTO<Void> response = roomService.leaveRoom(roomId);

        assertTrue(response.isSuccess());
        assertEquals("Voce saiu da sala com sucesso", response.getMessage());
        verify(roomPlayerRepository).delete(playerEntry);

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertEquals(1, roomCaptor.getValue().getCurrentPlayers());
    }

    @Test
    void leaveRoom_AsMaster_ShouldThrowMasterCannotLeaveRoomException() {
        UUID roomId = UUID.randomUUID();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));

        assertThrows(MasterCannotLeaveRoomException.class, () -> roomService.leaveRoom(roomId));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void leaveRoom_AsNonMember_ShouldThrowRoomMembershipRequiredException() {
        UUID roomId = UUID.randomUUID();

        User outsider = User.builder().id(3L).name("Hisoka Morow").email("gon@hunter.com").build();

        Room room = Room.builder()
                .id(roomId)
                .name("Sala do Gon")
                .master(masterUser)
                .currentPlayers(1)
                .maxPlayers(10)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(outsider));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.findByRoomAndUser(room, outsider)).thenReturn(Optional.empty());

        assertThrows(RoomMembershipRequiredException.class, () -> roomService.leaveRoom(roomId));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void leaveRoom_RoomNotFound_ShouldThrowRoomNotFoundException() {
        UUID roomId = UUID.randomUUID();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.findByIdWithLock(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> roomService.leaveRoom(roomId));
        verify(roomPlayerRepository, never()).delete(any(RoomPlayer.class));
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
