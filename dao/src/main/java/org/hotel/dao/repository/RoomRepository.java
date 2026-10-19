package org.hotel.dao.repository;

import org.hotel.model.entities.Room;
import org.hotel.model.enums.RoomStatus;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

public interface RoomRepository {
    void save(Room room);
    List<Room> findAll();
    void setAvailable(Room room);
    void setStatus(Room room, Date releasedIn, RoomStatus roomStatus);
    void setNewRoomPrice(Room room, BigDecimal price);
    Room getRoom(String id);
    List<Room> findFreeRoomsByDate(Date date);
    Room getRoomForUpdate(String roomId);
}
