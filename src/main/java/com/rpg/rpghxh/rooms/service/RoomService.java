package com.rpg.rpghxh.rooms.service;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.repository.RoomRepository;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.rooms.dto.CreateRoomDTO;
import com.rpg.rpghxh.rooms.dto.InviteResponseDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.RoomAccessDeniedException;
import com.rpg.rpghxh.shared.exceptions.RoomNotFoundException;
import com.rpg.rpghxh.shared.exceptions.UserNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RoomService {

    private static final String INVITE_BASE_URL = "https://api.rpg.com/rooms/join/";

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RedisInviteService redisInviteService;

    public RoomService(RoomRepository roomRepository,
                       UserRepository userRepository,
                       RedisInviteService redisInviteService) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.redisInviteService = redisInviteService;
    }

    @Transactional
    public ResponseDTO<RoomResponseDTO> createRoom(CreateRoomDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User master = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        Room room = Room.builder()
                .name(dto.getName())
                .master(master)
                .build();

        Room savedRoom = roomRepository.save(room);

        RoomResponseDTO response = buildRoomResponse(savedRoom, master);

        return ResponseDTO.success(response, "Sala criada com sucesso");
    }

    public ResponseDTO<InviteResponseDTO> getInviteLink(UUID roomId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        Room room = roomRepository.findById(roomId)
                .orElseThrow(RoomNotFoundException::new);

        if (!room.getMaster().getId().equals(user.getId())) {
            throw new RoomAccessDeniedException();
        }

        String inviteHash = redisInviteService.getOrCreateInvite(roomId, user.getId());

        InviteResponseDTO response = InviteResponseDTO.builder()
                .inviteUrl(INVITE_BASE_URL + inviteHash)
                .build();

        return ResponseDTO.success(response, "Link de convite gerado com sucesso");
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
