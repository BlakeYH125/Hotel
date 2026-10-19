package org.hotel.service;

import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hotel.model.constants.CommandConstants;
import org.hotel.model.constants.GuestCountConstants;
import org.hotel.model.constants.ParametersConstants;
import org.hotel.model.constants.TimeConstants;
import org.hotel.model.entities.Guest;
import org.hotel.model.entities.Room;
import org.hotel.model.enums.RoomStatus;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.RoomNotFoundException;
import org.hotel.model.exceptions.RoomAlreadyOccupiedException;
import org.hotel.model.exceptions.RoomAlreadyAvailableException;
import org.hotel.model.exceptions.RoomAlreadyExistsException;
import org.hotel.model.exceptions.RoomAlreadyInServiceException;
import org.hotel.model.exceptions.ChangeStatusBannedException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.dao.repository.GuestRepository;
import org.hotel.dao.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;



@Service
@Transactional
public class RoomService {
    /**
     * Логгер.
     */
    private static final Logger LOGGER = LogManager.getLogger(RoomService.class);

    /**
     * Репозиторий для работы с комнатами в БД.
     */
    private final RoomRepository roomRepository;

    /**
     * Репозиторий для работы с гостями в БД.
     */
    private final GuestRepository guestRepository;

    /**
     * Можно ли менять статус комнаты вручную.
     */
    @Value("${hotel.room.status.changing}")
    private boolean isAllowChange;

    /**
     * Лимит отображения предыдущих гостей комнаты.
     */
    @Value("${hotel.room.history.limit}")
    private int previousGuestsLimit;

    public RoomService(final RoomRepository roomRepositoryP, final GuestRepository guestRepositoryP) {
        this.roomRepository = roomRepositoryP;
        this.guestRepository = guestRepositoryP;
    }

    public List<Room> getRooms() {
        return roomRepository.findAll();
    }

    public boolean isThereRoom(final String idP) {
        return roomRepository.getRoom(idP) != null;
    }

    public Room getRoom(final String idP) {
        if (isThereRoom(idP)) {
            return roomRepository.getRoom(idP);
        } else {
            throw new RoomNotFoundException();
        }
    }

    public void addNewRoom(final Room roomP) {
        if (isThereRoom(roomP.getId())) {
            throw new RoomAlreadyExistsException();
        }
        roomRepository.save(roomP);
    }

    public void setAvailable(String roomId) {
        Room room = getRoomForUpdate(roomId);

        if (!isAllowChange) {
            throw new ChangeStatusBannedException();
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new RoomAlreadyOccupiedException();
        }
        if (room.getStatus() == RoomStatus.AVAILABLE) {
            throw new RoomAlreadyAvailableException();
        }

        roomRepository.setAvailable(room);
    }

    public void setAvailableToEvict(String roomId) {
        Room room = getRoomForUpdate(roomId);

        if (room.getStatus() == RoomStatus.AVAILABLE) {
            throw new RoomAlreadyAvailableException();
        }

        roomRepository.setAvailable(room);
    }

    public void setOccupied(String roomId, int daysCount) {
        Room room = getRoomForUpdate(roomId);

        if (!isAllowChange) {
            throw new ChangeStatusBannedException();
        }
        if (room.getStatus() == RoomStatus.IN_SERVICE) {
            throw new RoomAlreadyInServiceException();
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new RoomAlreadyOccupiedException();
        }

        java.sql.Date releasedIn = new java.sql.Date(System.currentTimeMillis() + daysCount * TimeConstants.MSEC_IN_DAY);
        roomRepository.setStatus(room, releasedIn, RoomStatus.OCCUPIED);
    }

    public void setOccupiedToSettle(String roomId, int daysCount) {
        Room room = getRoomForUpdate(roomId);

        if (room.getStatus() == RoomStatus.IN_SERVICE) {
            throw new RoomAlreadyInServiceException();
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new RoomAlreadyOccupiedException();
        }

        java.sql.Date releasedIn = new java.sql.Date(System.currentTimeMillis() + daysCount * TimeConstants.MSEC_IN_DAY);
        roomRepository.setStatus(room, releasedIn, RoomStatus.OCCUPIED);
    }

    public void setInService(String roomId, int daysCount) {
        Room room = getRoomForUpdate(roomId);

        if (!isAllowChange) {
            throw new ChangeStatusBannedException();
        }
        if (room.getStatus() == RoomStatus.IN_SERVICE) {
            throw new RoomAlreadyInServiceException();
        }
        if (room.getStatus() == RoomStatus.OCCUPIED) {
            throw new RoomAlreadyOccupiedException();
        }

        java.sql.Date releasedIn = new java.sql.Date(System.currentTimeMillis() + daysCount * TimeConstants.MSEC_IN_DAY);
        roomRepository.setStatus(room, releasedIn, RoomStatus.IN_SERVICE);
    }

    public boolean isAvailable(final String idP) {
        return getRoom(idP).getStatus() == RoomStatus.AVAILABLE;
    }

    public boolean isServicing(final String idP) {
        return getRoom(idP).getStatus() == RoomStatus.IN_SERVICE;
    }

    public boolean isOccupied(final String idP) {
        return getRoom(idP).getStatus() == RoomStatus.OCCUPIED;
    }

    public void setNewRoomPrice(String roomId, BigDecimal newPrice) {
        Room room = getRoomForUpdate(roomId);

        if (newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Цена номера не может быть отрицательной");
        }

        roomRepository.setNewRoomPrice(room, newPrice);
    }

    public List<Guest> getThreePrevRoomGuests(final String idP) {
        if (!isThereRoom(idP)) {
            throw new RoomNotFoundException();
        }
        return guestRepository.findPreviousGuests(getRoom(idP), Math.min(GuestCountConstants.THREE_GUESTS, previousGuestsLimit));
    }

    public int getFreeRoomsCount() {
        return new ArrayList<>(getFreeRoomsByDate(new Date(System.currentTimeMillis()))).size();
    }

    public List<Room> getFreeRoomsByDate(final Date dateP) {
        return roomRepository.findFreeRoomsByDate(new java.sql.Date(dateP.getTime()));
    }

    public List<Room> getAllRoomsWithSort(final SortType sortTypeP) {
        if (sortTypeP != SortType.PRICE && sortTypeP != SortType.CAPACITY && sortTypeP != SortType.STARS) {
            throw new WrongSortTypeException();
        }
        List<Room> listRooms = new ArrayList<>(getRooms());
        if (sortTypeP == SortType.PRICE) {
            listRooms.sort(Comparator.comparing(Room::getPrice));
        } else if (sortTypeP == SortType.CAPACITY) {
            listRooms.sort(Comparator.comparing(Room::getCapacity));
        } else if (sortTypeP == SortType.STARS) {
            listRooms.sort(Comparator.comparing(Room::getStars));
        }
        return listRooms;
    }

    public List<Room> getFreeRoomsWithSort(final SortType sortTypeP) {
        if (sortTypeP != SortType.PRICE && sortTypeP != SortType.CAPACITY && sortTypeP != SortType.STARS) {
            throw new WrongSortTypeException();
        }
        List<Room> listRooms = new ArrayList<>(getFreeRoomsByDate(new Date(System.currentTimeMillis())));
        if (sortTypeP == SortType.PRICE) {
            listRooms.sort(Comparator.comparing(Room::getPrice));
        } else if (sortTypeP == SortType.CAPACITY) {
            listRooms.sort(Comparator.comparing(Room::getCapacity));
        } else if (sortTypeP == SortType.STARS) {
            listRooms.sort(Comparator.comparing(Room::getStars));
        }
        return listRooms;
    }

    public List<Guest> getCurrentGuests(final Room roomP) {
        if (!isThereRoom(roomP.getId())) {
            throw new RoomNotFoundException();
        }
        return guestRepository.findCurrentGuestsInRoom(roomP);
    }

    public String importRooms(MultipartFile multipartFile) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(multipartFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            int successCount = 0;
            int errorCount = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == ParametersConstants.ROOM_PARAMETERS_COUNT) {
                    try {
                        Room room = new Room();
                        room.setId(parts[CommandConstants.COMMAND_ZERO]);
                        room.setNumber(Integer.parseInt(parts[CommandConstants.COMMAND_ONE]));
                        room.setPrice(new BigDecimal(parts[CommandConstants.COMMAND_TWO]));
                        room.setStatus(RoomStatus.valueOf(parts[CommandConstants.COMMAND_THREE]));
                        room.setCapacity(Integer.parseInt(parts[CommandConstants.COMMAND_FOUR]));
                        room.setStars(Integer.parseInt(parts[CommandConstants.COMMAND_FIVE]));
                        if (isThereRoom(room.getId())) {
                            roomRepository.save(room);
                        } else {
                            addNewRoom(room);
                        }
                        successCount++;
                    } catch (Exception e) {
                        LOGGER.error("Ошибка обработки строки " + line + ": " + e.getMessage());
                        errorCount++;
                    }
                } else {
                    LOGGER.error("Строка " + line + " пропущена. Неверное количество параметров");
                    errorCount++;
                }
            }
            return "Импорт завершен. Количество ошибок: " + errorCount + ", количество успешно считанных строк: " + successCount;
        }
    }

    public String exportRooms() {
        List<Room> rooms = getRooms();
        StringBuilder stringBuilder = new StringBuilder();
        for (Room room : rooms) {
            String[] data = new String[ParametersConstants.ROOM_PARAMETERS_COUNT];
            data[CommandConstants.COMMAND_ZERO] = room.getId();
            data[CommandConstants.COMMAND_ONE] = String.valueOf(room.getNumber());
            data[CommandConstants.COMMAND_TWO] = String.valueOf(room.getPrice());
            data[CommandConstants.COMMAND_THREE] = room.getStatus().name();
            data[CommandConstants.COMMAND_FOUR] = String.valueOf(room.getCapacity());
            data[CommandConstants.COMMAND_FIVE] = String.valueOf(room.getStars());
            String resultLine = String.join(";", data);
            stringBuilder.append(resultLine).append("\n");
        }
        return stringBuilder.toString();
    }

    public Room getRoomForUpdate(String roomId) {
        Room room = roomRepository.getRoomForUpdate(roomId);
        if (room == null) {
            throw new RoomNotFoundException();
        }
        return room;
    }
}
