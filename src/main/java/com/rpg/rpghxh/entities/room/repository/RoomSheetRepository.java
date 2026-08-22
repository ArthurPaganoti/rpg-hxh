package com.rpg.rpghxh.entities.room.repository;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomSheet;
import com.rpg.rpghxh.entities.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomSheetRepository extends JpaRepository<RoomSheet, Long> {

    Optional<RoomSheet> findByRoomAndUser(Room room, User user);

    void deleteByRoom(Room room);

    @Query("SELECT rs FROM RoomSheet rs JOIN FETCH rs.user WHERE rs.room = :room ORDER BY rs.uploadedAt ASC")
    List<RoomSheet> findByRoomWithUser(@Param("room") Room room);
}