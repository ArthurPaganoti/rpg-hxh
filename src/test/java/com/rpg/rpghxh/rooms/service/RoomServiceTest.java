package com.rpg.rpghxh.rooms.service;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.repository.RoomRepository;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.rooms.dto.CreateRoomDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.UserNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RoomService roomService;

    private User masterUser;

    @BeforeEach
    void setUp() {
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
    void createRoom_PublicRoom_ShouldReturnSuccessWithoutInviteCode() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala do Gon")
                .isPrivate(false)
                .build();

        Room savedRoom = Room.builder()
                .id(UUID.randomUUID())
                .name("Sala do Gon")
                .master(masterUser)
                .isPrivate(false)
                .inviteCode(null)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertTrue(response.isSuccess());
        assertEquals("Sala criada com sucesso", response.getMessage());
        assertNotNull(response.getContent());
        assertEquals("Sala do Gon", response.getContent().getName());
        assertEquals("Gon Freecss", response.getContent().getMasterName());
        assertFalse(response.getContent().isPrivate());
        assertNull(response.getContent().getInviteCode());
        assertEquals(1, response.getContent().getCurrentPlayers());
        assertEquals(10, response.getContent().getMaxPlayers());

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertNull(roomCaptor.getValue().getInviteCode());
    }

    @Test
    void createRoom_PrivateRoom_ShouldReturnSuccessWithInviteCode() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala Secreta")
                .isPrivate(true)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> {
            Room room = invocation.getArgument(0);
            room.setId(UUID.randomUUID());
            room.setCreatedAt(LocalDateTime.now());
            return room;
        });

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertTrue(response.isSuccess());
        assertEquals("Sala criada com sucesso", response.getMessage());
        assertTrue(response.getContent().isPrivate());
        assertNotNull(response.getContent().getInviteCode());
        assertEquals(6, response.getContent().getInviteCode().length());

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertNotNull(roomCaptor.getValue().getInviteCode());
        assertEquals(6, roomCaptor.getValue().getInviteCode().length());
    }

    @Test
    void createRoom_UserNotFound_ShouldThrowRuntimeException() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala do Gon")
                .isPrivate(false)
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> roomService.createRoom(dto));
        verify(roomRepository, never()).save(any(Room.class));
    }

    @Test
    void createRoom_ShouldSetAuthenticatedUserAsMaster() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala do Mestre")
                .isPrivate(false)
                .build();

        Room savedRoom = Room.builder()
                .id(UUID.randomUUID())
                .name("Sala do Mestre")
                .master(masterUser)
                .isPrivate(false)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertEquals("Gon Freecss", response.getContent().getMasterName());

        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        verify(roomRepository).save(roomCaptor.capture());
        assertEquals(masterUser, roomCaptor.getValue().getMaster());
    }

    @Test
    void createRoom_ShouldDefaultToOneCurrentPlayerAndTenMaxPlayers() {
        CreateRoomDTO dto = CreateRoomDTO.builder()
                .name("Sala Padrao")
                .isPrivate(false)
                .build();

        Room savedRoom = Room.builder()
                .id(UUID.randomUUID())
                .name("Sala Padrao")
                .master(masterUser)
                .isPrivate(false)
                .currentPlayers(1)
                .maxPlayers(10)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(masterUser));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);

        ResponseDTO<RoomResponseDTO> response = roomService.createRoom(dto);

        assertEquals(1, response.getContent().getCurrentPlayers());
        assertEquals(10, response.getContent().getMaxPlayers());
    }
}
