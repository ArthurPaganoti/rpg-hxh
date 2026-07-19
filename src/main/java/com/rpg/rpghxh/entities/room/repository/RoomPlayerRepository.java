package com.rpg.rpghxh.entities.room.repository;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomPlayer;
import com.rpg.rpghxh.entities.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomPlayerRepository extends JpaRepository<RoomPlayer, Long> {

    boolean existsByRoomAndUser(Room room, User user);

    long countByRoom(Room room);

    void deleteByRoom(Room room);

    @Query("SELECT r FROM RoomPlayer rp JOIN rp.room r JOIN FETCH r.master WHERE rp.user = :user ORDER BY r.createdAt DESC")
    List<Room> findRoomsByUser(@Param("user") User user);
}