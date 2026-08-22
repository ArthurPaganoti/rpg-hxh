package com.rpg.rpghxh.entities.room.repository;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomPlayer;
import com.rpg.rpghxh.entities.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomPlayerRepository extends JpaRepository<RoomPlayer, Long> {

    boolean existsByRoomAndUser(Room room, User user);

    Optional<RoomPlayer> findByRoomAndUser(Room room, User user);

    long countByRoom(Room room);

    void deleteByRoom(Room room);

    @Query("SELECT r FROM RoomPlayer rp JOIN rp.room r JOIN FETCH r.master WHERE rp.user = :user ORDER BY r.createdAt DESC")
    List<Room> findRoomsByUser(@Param("user") User user);

    @Query("SELECT rp FROM RoomPlayer rp JOIN FETCH rp.user WHERE rp.room = :room ORDER BY rp.joinedAt ASC")
    List<RoomPlayer> findByRoomWithUser(@Param("room") Room room);
}
