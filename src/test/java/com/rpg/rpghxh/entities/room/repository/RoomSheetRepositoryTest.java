package com.rpg.rpghxh.entities.room.repository;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomSheet;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoomSheetRepositoryTest {

    @Autowired
    private RoomSheetRepository roomSheetRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User master;
    private User player;
    private Room room;

    @BeforeEach
    void setUp() {
        master = userRepository.save(User.builder()
                .name("Gon Freecss").email("gon@hunter.com").senha("encodedPassword").build());
        player = userRepository.save(User.builder()
                .name("Killua Zoldyck").email("killua@hunter.com").senha("encodedPassword").build());

        room = roomRepository.save(Room.builder().name("Sala Um").master(master).build());

        roomSheetRepository.save(RoomSheet.builder()
                .room(room).user(player).objectKey("rooms/x/sheets/2")
                .fileName("ficha.pdf").contentType("application/pdf").sizeBytes(100).build());
    }

    @Test
    void findByRoomAndUser_WhenExists_ShouldReturnSheet() {
        assertTrue(roomSheetRepository.findByRoomAndUser(room, player).isPresent());
    }

    @Test
    void findByRoomAndUser_WhenMissing_ShouldReturnEmpty() {
        assertTrue(roomSheetRepository.findByRoomAndUser(room, master).isEmpty());
    }

    @Test
    void findByRoomWithUser_ShouldReturnSheetsWithUserLoaded() {
        List<RoomSheet> sheets = roomSheetRepository.findByRoomWithUser(room);

        assertEquals(1, sheets.size());
        assertEquals("Killua Zoldyck", sheets.get(0).getUser().getName());
        assertEquals("ficha.pdf", sheets.get(0).getFileName());
    }
}
