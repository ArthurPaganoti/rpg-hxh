package com.rpg.rpghxh.entities.room.repository;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomBan;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoomBanRepositoryTest {

    @Autowired
    private RoomBanRepository roomBanRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User master;
    private User banned;
    private User free;
    private Room room;

    @BeforeEach
    void setUp() {
        master = userRepository.save(User.builder()
                .name("Gon Freecss").email("gon@hunter.com").senha("encodedPassword").build());
        banned = userRepository.save(User.builder()
                .name("Hisoka Morow").email("hisoka@hunter.com").senha("encodedPassword").build());
        free = userRepository.save(User.builder()
                .name("Killua Zoldyck").email("killua@hunter.com").senha("encodedPassword").build());

        room = roomRepository.save(Room.builder().name("Sala Um").master(master).build());

        roomBanRepository.save(RoomBan.builder().room(room).user(banned).build());
    }

    @Test
    void existsByRoomAndUser_WhenBanned_ShouldReturnTrue() {
        assertTrue(roomBanRepository.existsByRoomAndUser(room, banned));
    }

    @Test
    void existsByRoomAndUser_WhenNotBanned_ShouldReturnFalse() {
        assertFalse(roomBanRepository.existsByRoomAndUser(room, free));
    }

    @Test
    void findByRoomAndUser_WhenBanned_ShouldReturnBan() {
        assertTrue(roomBanRepository.findByRoomAndUser(room, banned).isPresent());
    }

    @Test
    void findByRoomWithUser_ShouldReturnBannedUsersWithUserLoaded() {
        List<RoomBan> bans = roomBanRepository.findByRoomWithUser(room);

        assertEquals(1, bans.size());
        assertEquals("Hisoka Morow", bans.get(0).getUser().getName());
    }
}