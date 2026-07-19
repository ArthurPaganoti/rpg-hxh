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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomPlayerRepository roomPlayerRepository;
    private final RedisInviteService redisInviteService;
    private final String inviteBaseUrl;

    public RoomService(RoomRepository roomRepository,
                       UserRepository userRepository,
                       RoomPlayerRepository roomPlayerRepository,
                       RedisInviteService redisInviteService,
                       @Value("${INVITE_BASE_URL:https://api.rpg.com/rooms/join/}") String inviteBaseUrl) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.roomPlayerRepository = roomPlayerRepository;
        this.redisInviteService = redisInviteService;
        this.inviteBaseUrl = inviteBaseUrl;
    }

    @Transactional
    public ResponseDTO<RoomResponseDTO> createRoom(CreateRoomDTO dto) {
        User master = getAuthenticatedUser();

        Room room = Room.builder()
                .name(dto.getName())
                .master(master)
                .build();

        Room savedRoom = roomRepository.save(room);

        roomPlayerRepository.save(RoomPlayer.builder().room(savedRoom).user(master).build());

        Room reloadedRoom = roomRepository.findById(savedRoom.getId()).orElse(savedRoom);

        return ResponseDTO.success(buildRoomResponse(reloadedRoom, master), "Sala criada com sucesso");
    }

    public ResponseDTO<InviteResponseDTO> getInviteLink(UUID roomId) {
        Room room = findRoomAsMaster(roomId);

        String inviteHash = redisInviteService.getOrCreateInvite(roomId, room.getMaster().getId());

        InviteResponseDTO response = InviteResponseDTO.builder()
                .inviteUrl(inviteBaseUrl + inviteHash)
                .build();

        return ResponseDTO.success(response, "Link de convite gerado com sucesso");
    }

    @Transactional
    public ResponseDTO<RoomResponseDTO> joinRoom(String hash) {
        User user = getAuthenticatedUser();

        UUID roomId = redisInviteService.getRoomIdByHash(hash)
                .orElseThrow(InvalidInviteException::new);

        Room room = roomRepository.findByIdWithLock(roomId)
                .orElseThrow(RoomNotFoundException::new);

        if (room.getMaster().getId().equals(user.getId())) {
            throw new PlayerAlreadyInRoomException();
        }

        if (roomPlayerRepository.existsByRoomAndUser(room, user)) {
            throw new PlayerAlreadyInRoomException();
        }

        if (room.getCurrentPlayers() >= room.getMaxPlayers()) {
            throw new RoomFullException();
        }

        roomPlayerRepository.save(RoomPlayer.builder().room(room).user(user).build());

        room.setCurrentPlayers((int) roomPlayerRepository.countByRoom(room));
        Room updatedRoom = roomRepository.save(room);

        return ResponseDTO.success(buildRoomResponse(updatedRoom, updatedRoom.getMaster()), "Entrou na sala com sucesso");
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            throw new UserNotFoundException();
        }

        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(UserNotFoundException::new);
    }

    private Room findRoomAsMaster(UUID roomId) {
        User user = getAuthenticatedUser();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        if (!room.getMaster().getId().equals(user.getId())) {
            throw new RoomAccessDeniedException();
        }

        return room;
    }

    private RoomResponseDTO buildRoomResponse(Room room, User master) {
        return RoomResponseDTO.builder()
                .id(room.getId())
                .name(room.getName())
                .masterName(master.getName())
                .currentPlayers(room.getCurrentPlayers())
                .maxPlayers(room.getMaxPlayers())
                .createdAt(room.getCreatedAt())
                .build();
    }
}
