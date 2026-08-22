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
import com.rpg.rpghxh.shared.exceptions.FileStorageException;
import com.rpg.rpghxh.shared.exceptions.InvalidFileTypeException;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomMembershipRequiredException;
import com.rpg.rpghxh.shared.exceptions.RoomNotFoundException;
import com.rpg.rpghxh.shared.exceptions.SheetNotFoundException;
import com.rpg.rpghxh.shared.exceptions.UserNotFoundException;
import com.rpg.rpghxh.shared.storage.FileStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class SheetService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.oasis.opendocument.text"
    );

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final RoomSheetRepository roomSheetRepository;
    private final FileStorageService fileStorageService;

    public SheetService(RoomRepository roomRepository,
                        UserRepository userRepository,
                        RoomPlayerRepository roomPlayerRepository,
                        RoomSheetRepository roomSheetRepository,
                        FileStorageService fileStorageService) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.roomSheetRepository = roomSheetRepository;
        this.fileStorageService = fileStorageService;
    }

    @Transactional
    public ResponseDTO<SheetResponseDTO> uploadSheet(UUID roomId, MultipartFile file) {
        User user = getAuthenticatedUser();
        Room room = findRoomAsMember(roomId, user);

        if (file == null || file.isEmpty()) {
            throw new InvalidFileTypeException();
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileTypeException();
        }

        String objectKey = buildObjectKey(roomId, user.getId());

        try {
            fileStorageService.upload(objectKey, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (java.io.IOException ex) {
            throw new FileStorageException("Falha ao ler o arquivo enviado", ex);
        }

        RoomSheet sheet = roomSheetRepository.findByRoomAndUser(room, user)
                .orElseGet(() -> RoomSheet.builder().room(room).user(user).build());
        sheet.setObjectKey(objectKey);
        sheet.setFileName(file.getOriginalFilename());
        sheet.setContentType(file.getContentType());
        sheet.setSizeBytes(file.getSize());

        RoomSheet saved = roomSheetRepository.save(sheet);

        return ResponseDTO.success(toDTO(saved, user), "Ficha enviada com sucesso");
    }

    public ResponseDTO<List<SheetResponseDTO>> listSheets(UUID roomId) {
        User user = getAuthenticatedUser();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        boolean isMaster = room.getMaster().getId().equals(user.getId());

        if (!isMaster && !roomPlayerRepository.existsByRoomAndUser(room, user)) {
            throw new RoomMembershipRequiredException();
        }

        List<SheetResponseDTO> sheets;
        if (isMaster) {
            sheets = roomSheetRepository.findByRoomWithUser(room).stream()
                    .map(sheet -> toDTO(sheet, user))
                    .toList();
        } else {
            sheets = roomSheetRepository.findByRoomAndUser(room, user).stream()
                    .map(sheet -> toDTO(sheet, user))
                    .toList();
        }

        return ResponseDTO.success(sheets, "Fichas listadas com sucesso");
    }

    public SheetDownload downloadSheet(UUID roomId, Long targetUserId) {
        User user = getAuthenticatedUser();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        boolean isMaster = room.getMaster().getId().equals(user.getId());

        if (!isMaster) {
            if (!roomPlayerRepository.existsByRoomAndUser(room, user)) {
                throw new RoomMembershipRequiredException();
            }
            if (!targetUserId.equals(user.getId())) {
                throw new RoomAccessDeniedException();
            }
        }

        User target = userRepository.findById(targetUserId)
                .orElseThrow(SheetNotFoundException::new);

        RoomSheet sheet = roomSheetRepository.findByRoomAndUser(room, target)
                .orElseThrow(SheetNotFoundException::new);

        return new SheetDownload(
                fileStorageService.download(sheet.getObjectKey()),
                sheet.getFileName(),
                sheet.getContentType(),
                sheet.getSizeBytes());
    }

    @Transactional
    public ResponseDTO<Void> deleteSheet(UUID roomId) {
        User user = getAuthenticatedUser();
        Room room = findRoomAsMember(roomId, user);

        RoomSheet sheet = roomSheetRepository.findByRoomAndUser(room, user)
                .orElseThrow(SheetNotFoundException::new);

        fileStorageService.delete(sheet.getObjectKey());
        roomSheetRepository.delete(sheet);

        return ResponseDTO.success("Ficha removida com sucesso");
    }

    private String buildObjectKey(UUID roomId, Long userId) {
        return "rooms/" + roomId + "/sheets/" + userId;
    }

    private SheetResponseDTO toDTO(RoomSheet sheet, User requester) {
        return SheetResponseDTO.builder()
                .userId(sheet.getUser().getId())
                .ownerName(sheet.getUser().getName())
                .fileName(sheet.getFileName())
                .contentType(sheet.getContentType())
                .sizeBytes(sheet.getSizeBytes())
                .uploadedAt(sheet.getUploadedAt())
                .mine(sheet.getUser().getId().equals(requester.getId()))
                .build();
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UserNotFoundException();
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
    }

    private Room findRoomAsMember(UUID roomId, User user) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);
        boolean isMaster = room.getMaster().getId().equals(user.getId());
        if (!isMaster && !roomPlayerRepository.existsByRoomAndUser(room, user)) {
            throw new RoomMembershipRequiredException();
        }
        return room;
    }
}