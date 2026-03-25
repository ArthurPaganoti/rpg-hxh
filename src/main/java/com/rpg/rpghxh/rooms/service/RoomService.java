package com.rpg.rpghxh.rooms.service;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.repository.RoomRepository;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import com.rpg.rpghxh.rooms.dto.CreateRoomDTO;
import com.rpg.rpghxh.rooms.dto.RoomResponseDTO;
import com.rpg.rpghxh.shared.dto.ResponseDTO;
import com.rpg.rpghxh.shared.exceptions.UserNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
public class RoomService {

    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public RoomService(RoomRepository roomRepository, UserRepository userRepository) {
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ResponseDTO<RoomResponseDTO> createRoom(CreateRoomDTO dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User master = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        String inviteCode = dto.isPrivate() ? generateInviteCode() : null;

        Room room = Room.builder()
                .name(dto.getName())
                .master(master)
                .isPrivate(dto.isPrivate())
                .inviteCode(inviteCode)
                .build();

        Room savedRoom = roomRepository.save(room);

        RoomResponseDTO response = RoomResponseDTO.builder()
                .id(savedRoom.getId())
                .name(savedRoom.getName())
                .masterName(master.getName())
                .isPrivate(savedRoom.isPrivate())
                .inviteCode(savedRoom.getInviteCode())
                .currentPlayers(savedRoom.getCurrentPlayers())
                .maxPlayers(savedRoom.getMaxPlayers())
                .createdAt(savedRoom.getCreatedAt())
                .build();

        return ResponseDTO.success(response, "Sala criada com sucesso");
    }

    private String generateInviteCode() {
        StringBuilder code = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            code.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return code.toString();
    }
}
