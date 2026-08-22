package com.rpg.rpghxh.entities.room.repository;

import com.rpg.rpghxh.entities.room.entity.Room;
import com.rpg.rpghxh.entities.room.entity.RoomBan;
import com.rpg.rpghxh.entities.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RoomBanRepository extends JpaRepository<RoomBan, Long> {

    boolean existsByRoomAndUser(Room room, User user);

    Optional<RoomBan> findByRoomAndUser(Room room, User user);

    void deleteByRoom(Room room);

    @Query("SELECT rb FROM RoomBan rb JOIN FETCH rb.user WHERE rb.room = :room ORDER BY rb.bannedAt ASC")
    List<RoomBan> findByRoomWithUser(@Param("room") Room room);
}
