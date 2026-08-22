package com.rpg.rpghxh.sheets.service;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomSheet;
import com.rpg.rpghxh.entities.room.repository.RoomPlayerRepository;
import com.rpg.rpghxh.entities.room.repository.RoomRepository;
import com.rpg.rpghxh.entities.room.repository.RoomSheetRepository;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.sheets.dto.SheetDownload;
import com.rpg.rpghxh.sheets.dto.SheetResponseDTO;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.InvalidFileTypeException;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomMembershipRequiredException;
import com.rpg.rpghxh.shared.exceptions.RoomNotFoundException;
import com.rpg.rpghxh.shared.exceptions.SheetNotFoundException;
import com.rpg.rpghxh.shared.storage.FileStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SheetServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoomPlayerRepository roomPlayerRepository;

    @Mock
    private RoomSheetRepository roomSheetRepository;

    @Mock
    private FileStorageService fileStorageService;

    private SheetService sheetService;

    private User master;
    private User player;

    @BeforeEach
    void setUp() {
        sheetService = new SheetService(roomRepository, userRepository, roomPlayerRepository,
                roomSheetRepository, fileStorageService);

        master = User.builder().id(1L).name("Gon Freecss").email("gon@hunter.com").build();
        player = User.builder().id(2L).name("Killua Zoldyck").email("killua@hunter.com").build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("killua@hunter.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Room room(UUID id) {
        return Room.builder().id(id).name("Sala do Gon").master(master).build();
    }

    private MultipartFile pdf() {
        return new MockMultipartFile("file", "ficha.pdf", "application/pdf", "conteudo".getBytes());
    }

    @Test
    void uploadSheet_AsMember_ShouldStoreAndSaveMetadata() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.empty());
        when(roomSheetRepository.save(any(RoomSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseDTO<SheetResponseDTO> response = sheetService.uploadSheet(roomId, pdf());

        assertTrue(response.isSuccess());
        assertEquals("Ficha enviada com sucesso", response.getMessage());
        assertEquals("ficha.pdf", response.getContent().getFileName());
        assertTrue(response.getContent().isMine());
        verify(fileStorageService).upload(anyString(), any(), anyLong(), eq("application/pdf"));
        verify(roomSheetRepository).save(any(RoomSheet.class));
    }

    @Test
    void uploadSheet_WhenExisting_ShouldReplaceSameRow() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        RoomSheet existing = RoomSheet.builder().id(5L).room(room).user(player)
                .objectKey("rooms/x/sheets/2").fileName("velha.pdf").contentType("application/pdf").sizeBytes(10).build();

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.of(existing));
        when(roomSheetRepository.save(any(RoomSheet.class))).thenAnswer(inv -> inv.getArgument(0));

        sheetService.uploadSheet(roomId, pdf());

        assertEquals("ficha.pdf", existing.getFileName());
        assertEquals(5L, existing.getId());
        verify(roomSheetRepository).save(existing);
    }

    @Test
    void uploadSheet_InvalidType_ShouldThrowInvalidFileTypeException() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        MultipartFile png = new MockMultipartFile("file", "x.png", "image/png", "x".getBytes());

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);

        assertThrows(InvalidFileTypeException.class, () -> sheetService.uploadSheet(roomId, png));
        verify(fileStorageService, never()).upload(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void uploadSheet_EmptyFile_ShouldThrowInvalidFileTypeException() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        MultipartFile empty = new MockMultipartFile("file", "x.pdf", "application/pdf", new byte[0]);

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);

        assertThrows(InvalidFileTypeException.class, () -> sheetService.uploadSheet(roomId, empty));
    }

    @Test
    void uploadSheet_AsNonMember_ShouldThrowRoomMembershipRequiredException() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(false);

        assertThrows(RoomMembershipRequiredException.class, () -> sheetService.uploadSheet(roomId, pdf()));
    }

    @Test
    void uploadSheet_RoomNotFound_ShouldThrowRoomNotFoundException() {
        UUID roomId = UUID.randomUUID();

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class, () -> sheetService.uploadSheet(roomId, pdf()));
    }

    @Test
    void listSheets_AsMaster_ShouldReturnAllSheets() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        RoomSheet s1 = RoomSheet.builder().room(room).user(player).fileName("k.pdf")
                .contentType("application/pdf").sizeBytes(10).uploadedAt(LocalDateTime.now()).build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("gon@hunter.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(master));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomSheetRepository.findByRoomWithUser(room)).thenReturn(List.of(s1));

        ResponseDTO<List<SheetResponseDTO>> response = sheetService.listSheets(roomId);

        assertEquals(1, response.getContent().size());
        assertEquals("Killua Zoldyck", response.getContent().get(0).getOwnerName());
        assertFalse(response.getContent().get(0).isMine());
        verify(roomSheetRepository, never()).findByRoomAndUser(any(), any());
    }

    @Test
    void listSheets_AsMember_ShouldReturnOnlyOwn() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        RoomSheet mine = RoomSheet.builder().room(room).user(player).fileName("k.pdf")
                .contentType("application/pdf").sizeBytes(10).uploadedAt(LocalDateTime.now()).build();

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.of(mine));

        ResponseDTO<List<SheetResponseDTO>> response = sheetService.listSheets(roomId);

        assertEquals(1, response.getContent().size());
        assertTrue(response.getContent().get(0).isMine());
        verify(roomSheetRepository, never()).findByRoomWithUser(any());
    }

    @Test
    void listSheets_AsNonMember_ShouldThrowRoomMembershipRequiredException() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);

        User outsider = User.builder().id(3L).name("Hisoka").email("killua@hunter.com").build();

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(outsider));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, outsider)).thenReturn(false);

        assertThrows(RoomMembershipRequiredException.class, () -> sheetService.listSheets(roomId));
    }

    @Test
    void downloadSheet_AsOwner_ShouldReturnFile() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        RoomSheet sheet = RoomSheet.builder().room(room).user(player).objectKey("rooms/x/sheets/2")
                .fileName("k.pdf").contentType("application/pdf").sizeBytes(8).build();

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.of(sheet));
        when(fileStorageService.download("rooms/x/sheets/2")).thenReturn(new ByteArrayInputStream("data".getBytes()));

        SheetDownload download = sheetService.downloadSheet(roomId, 2L);

        assertEquals("k.pdf", download.fileName());
        assertEquals("application/pdf", download.contentType());
    }

    @Test
    void downloadSheet_AsMaster_ShouldReturnOtherPlayerFile() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        RoomSheet sheet = RoomSheet.builder().room(room).user(player).objectKey("rooms/x/sheets/2")
                .fileName("k.pdf").contentType("application/pdf").sizeBytes(8).build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("gon@hunter.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        when(userRepository.findByEmail("gon@hunter.com")).thenReturn(Optional.of(master));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.of(sheet));
        when(fileStorageService.download("rooms/x/sheets/2")).thenReturn(new ByteArrayInputStream("data".getBytes()));

        SheetDownload download = sheetService.downloadSheet(roomId, 2L);

        assertEquals("k.pdf", download.fileName());
    }

    @Test
    void downloadSheet_MemberRequestingOtherSheet_ShouldThrowRoomAccessDeniedException() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);

        assertThrows(RoomAccessDeniedException.class, () -> sheetService.downloadSheet(roomId, 99L));
        verify(fileStorageService, never()).download(anyString());
    }

    @Test
    void downloadSheet_SheetNotFound_ShouldThrowSheetNotFoundException() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(player));
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.empty());

        assertThrows(SheetNotFoundException.class, () -> sheetService.downloadSheet(roomId, 2L));
    }

    @Test
    void deleteSheet_AsOwner_ShouldRemoveObjectAndRow() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);
        RoomSheet sheet = RoomSheet.builder().room(room).user(player).objectKey("rooms/x/sheets/2")
                .fileName("k.pdf").contentType("application/pdf").sizeBytes(8).build();

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.of(sheet));

        ResponseDTO<Void> response = sheetService.deleteSheet(roomId);

        assertTrue(response.isSuccess());
        verify(fileStorageService).delete("rooms/x/sheets/2");
        verify(roomSheetRepository).delete(sheet);
    }

    @Test
    void deleteSheet_WhenNoSheet_ShouldThrowSheetNotFoundException() {
        UUID roomId = UUID.randomUUID();
        Room room = room(roomId);

        when(userRepository.findByEmail("killua@hunter.com")).thenReturn(Optional.of(player));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(roomPlayerRepository.existsByRoomAndUser(room, player)).thenReturn(true);
        when(roomSheetRepository.findByRoomAndUser(room, player)).thenReturn(Optional.empty());

        assertThrows(SheetNotFoundException.class, () -> sheetService.deleteSheet(roomId));
        verify(fileStorageService, never()).delete(anyString());
    }
}