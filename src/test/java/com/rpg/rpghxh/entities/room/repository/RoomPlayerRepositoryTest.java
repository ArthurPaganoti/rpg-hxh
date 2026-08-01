package com.rpg.rpghxh.entities.room.repository;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomPlayer;
import com.rpg.rpghxh.entities.user.entity.User;
import com.rpg.rpghxh.entities.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class RoomPlayerRepositoryTest {

    @Autowired
    private RoomPlayerRepository roomPlayerRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    private User master;
    private User player;
    private Room room1;
    private Room room2;

    @BeforeEach
    void setUp() {
        master = userRepository.save(User.builder()
                .name("Gon Freecss")
                .email("gon@hunter.com")
                .senha("encodedPassword")
                .build());

        player = userRepository.save(User.builder()
                .name("Killua Zoldyck")
                .email("killua@hunter.com")
                .senha("encodedPassword")
                .build());

        room1 = roomRepository.save(Room.builder().name("Sala Um").master(master).build());
        room2 = roomRepository.save(Room.builder().name("Sala Dois").master(master).build());

        roomPlayerRepository.save(RoomPlayer.builder().room(room1).user(master).build());
        roomPlayerRepository.save(RoomPlayer.builder().room(room2).user(master).build());
        roomPlayerRepository.save(RoomPlayer.builder().room(room1).user(player).build());
    }

    @Test
    void findRoomsByUser_ShouldReturnOnlyRoomsWhereUserIsPlayer() {
        List<Room> rooms = roomPlayerRepository.findRoomsByUser(player);

        assertEquals(1, rooms.size());
        assertEquals("Sala Um", rooms.get(0).getName());
        assertEquals("Gon Freecss", rooms.get(0).getMaster().getName());
    }

    @Test
    void findRoomsByUser_ShouldReturnAllRoomsForMaster() {
        List<Room> rooms = roomPlayerRepository.findRoomsByUser(master);

        assertEquals(2, rooms.size());
        assertTrue(rooms.stream().anyMatch(r -> r.getName().equals("Sala Um")));
        assertTrue(rooms.stream().anyMatch(r -> r.getName().equals("Sala Dois")));
    }

    @Test
    void findRoomsByUser_WithNoMemberships_ShouldReturnEmptyList() {
        User outsider = userRepository.save(User.builder()
                .name("Hisoka Morow")
                .email("hisoka@hunter.com")
                .senha("encodedPassword")
                .build());

        List<Room> rooms = roomPlayerRepository.findRoomsByUser(outsider);

        assertTrue(rooms.isEmpty());
    }

    @Test
    void findByRoomWithUser_ShouldReturnMembersOrderedByJoinDate() {
        List<RoomPlayer> members = roomPlayerRepository.findByRoomWithUser(room1);

        assertEquals(2, members.size());
        assertEquals("Gon Freecss", members.get(0).getUser().getName());
        assertEquals("Killua Zoldyck", members.get(1).getUser().getName());
        assertTrue(members.get(0).getJoinedAt().compareTo(members.get(1).getJoinedAt()) <= 0);
    }

    @Test
    void countByRoom_ShouldCountPlayersInRoom() {
        assertEquals(2, roomPlayerRepository.countByRoom(room1));
        assertEquals(1, roomPlayerRepository.countByRoom(room2));
    }
}