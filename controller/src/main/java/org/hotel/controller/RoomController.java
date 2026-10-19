package org.hotel.controller;

import org.hotel.service.dto.GuestDto;
import org.hotel.service.dto.RoomDto;
import org.hotel.service.dto.SettleRequestDto;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.service.mapper.DtoMapper;
import org.hotel.service.AdministratorService;
import org.hotel.model.entities.Room;
import org.hotel.service.RoomService;
import org.hotel.model.entities.Guest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/hotel/rooms")
public class RoomController {
    /**
     * Логгер для фиксации логов.
     */
    private static final Logger LOGGER = LogManager.getLogger(RoomController.class);

    /**
     * Администратор.
     */
    private final AdministratorService administratorService;

    /**
     * Управление комнатами.
     */
    private final RoomService roomService;

    /**
     * Преобразователь в DTO.
     */
    private final DtoMapper dtoMapper;

    public RoomController(final AdministratorService administratorServiceP, final RoomService roomServiceP, final DtoMapper dtoMapperP) {
        this.administratorService = administratorServiceP;
        this.roomService = roomServiceP;
        this.dtoMapper = dtoMapperP;
    }

    @PostMapping("/settle")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<String> settle(@RequestBody SettleRequestDto settleRequestDto, Principal principal) {
        LOGGER.info("Начат процесс заселения в комнату " + settleRequestDto.getRoomId());
        List<Guest> guests = settleRequestDto.getGuests().stream()
                .map(dtoMapper::toGuestEntity)
                .toList();
        administratorService.settle(settleRequestDto.getRoomId(), guests, settleRequestDto.getDaysCount(), principal.getName());
        LOGGER.info("Заселение успешно");
        return ResponseEntity.ok("Заселение успешно.");
    }

    @PostMapping("/{id}/evict")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> evict(@PathVariable("id") String idP) {
        LOGGER.info("Начат процесс выселения из комнаты " + idP);
        administratorService.evict(idP);
        LOGGER.info("Выселение успешно");
        return ResponseEntity.ok("Выселение успешно");
    }

    @PostMapping("/{id}/set-available")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> setAvailable(@PathVariable("id") String idP) {
        LOGGER.info("Начат процесс установки статуса 'доступна' для комнаты " + idP);
        roomService.setAvailable(idP);
        LOGGER.info("Метод setAvailable успешно завершил работу");
        return ResponseEntity.ok("Установка статуса успешна");
    }

    @PostMapping("/{id}/set-occupied")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> setOccupied(@PathVariable("id") String idP, @RequestParam("daysCount") int daysCount) {
        LOGGER.info("Начат процесс установки статуса 'занята' для комнаты: " + idP);
        roomService.setOccupied(idP, daysCount);
        LOGGER.info("Установка статуса успешна");
        return ResponseEntity.ok("Установка статуса успешна");
    }

    @PostMapping("/{id}/set-in-service")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> setInService(@PathVariable("id") String idP, @RequestParam("daysCount") int daysCount) {
        LOGGER.info("Начат процесс установки статуса 'на обслуживании' для комнаты: " + idP);
        roomService.setInService(idP, daysCount);
        LOGGER.info("Установка статуса успешна.");
        return ResponseEntity.ok("Установка статуса успешна");
    }

    @PatchMapping("/{id}/setNewPrice")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> changeRoomPrice(@PathVariable("id") String idP, @RequestParam("newPrice") BigDecimal newPrice) {
        LOGGER.info("Начат процесс изменения цены для комнаты " + idP);
        roomService.setNewRoomPrice(idP, newPrice);
        LOGGER.info("Смена цены успешна");
        return ResponseEntity.ok("Смена цены успешна");
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addRoom(@RequestBody RoomDto roomDto) {
        LOGGER.info("Начат процесс добавления новой комнаты");
        Room newRoom = dtoMapper.toRoomEntity(roomDto);
        roomService.addNewRoom(newRoom);
        LOGGER.info("Добавление новой комнаты успешно");
        return ResponseEntity.ok("Добавление новой комнаты успешно");
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<RoomDto>> showAllRooms(@RequestParam(required = false, defaultValue = "PRICE") String sort) {
        LOGGER.info("Начат процесс вывода всех комнат");
        SortType sortType;
        if (sort.equalsIgnoreCase("PRICE")) {
            sortType = SortType.PRICE;
        } else if (sort.equalsIgnoreCase("CAPACITY")) {
            sortType = SortType.CAPACITY;
        } else if (sort.equalsIgnoreCase("STARS")) {
            sortType = SortType.STARS;
        } else {
            throw new WrongSortTypeException();
        }
        List<Room> rooms = roomService.getAllRoomsWithSort(sortType);
        List<RoomDto> roomsDto = rooms.stream()
                .map(dtoMapper::toRoomDto)
                .toList();
        LOGGER.info("Вывод комнат успешен");
        return ResponseEntity.ok(roomsDto);
    }

    @GetMapping("/free")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<RoomDto>> showAllFreeRooms(@RequestParam(required = false, defaultValue = "PRICE") String sort) {
        LOGGER.info("Начат процесс вывода всех свободных комнат");
        SortType sortType;
        if (sort.equalsIgnoreCase("PRICE")) {
            sortType = SortType.PRICE;
        } else if (sort.equalsIgnoreCase("CAPACITY")) {
            sortType = SortType.CAPACITY;
        } else if (sort.equalsIgnoreCase("STARS")) {
            sortType = SortType.STARS;
        } else {
            throw new WrongSortTypeException();
        }
        List<Room> rooms = roomService.getFreeRoomsWithSort(sortType);
        List<RoomDto> roomsDto = rooms.stream()
                .map(dtoMapper::toRoomDto)
                .toList();
        LOGGER.info("Вывод комнат успешен");
        return ResponseEntity.ok(roomsDto);
    }

    @GetMapping("/available/count")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Integer> getFreeRoomsCount() {
        LOGGER.info("Начат процесс подсчета свободных комнат");
        int count = roomService.getFreeRoomsCount();
        LOGGER.info("Подсчет комнат успешен");
        return ResponseEntity.ok(count);
    }

    @GetMapping("/free-by-date")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<RoomDto>> showFreeRoomsByDate(@RequestParam("date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        LOGGER.info("Начат процесс вывода комнат свободных к дате " + date);
        List<Room> rooms = roomService.getFreeRoomsByDate(date);
        List<RoomDto> roomsDto = rooms.stream()
                .map(dtoMapper::toRoomDto)
                .toList();
        LOGGER.info("Вывод комнат успешен");
        return ResponseEntity.ok(roomsDto);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<RoomDto> getRoomDetails(@PathVariable("id") String idP) {
        LOGGER.info("Начат процесс вывода данных о комнате " + idP);
        RoomDto roomDto = dtoMapper.toRoomDto(roomService.getRoom(idP));
        LOGGER.info("Вывод данных о комнате успешен");
        return ResponseEntity.ok(roomDto);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> importRoomData(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        LOGGER.info("Начат процесс импорта данных о комнате из csv файла");
        if (multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пуст.");
        }
        String report = roomService.importRooms(multipartFile);
        LOGGER.info(report);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> exportRoomData() {
        LOGGER.info("Начат процесс экспорта комнат");
        String csvData = roomService.exportRooms();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rooms_export.csv");
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        LOGGER.info("Процесс экспорта завершен");
        return ResponseEntity.ok().headers(httpHeaders).body(csvData);
    }

    @GetMapping("/{id}/last-guests")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<GuestDto>> getThreePrevGuests(@PathVariable("id") String idP) {
        LOGGER.info("Начат процесс вывода последних трех или меньше гостей комнаты " + idP);
        List<Guest> guests = roomService.getThreePrevRoomGuests(idP);
        List<GuestDto> guestsDto = guests.stream()
                .map(dtoMapper::toGuestDto)
                .toList();
        LOGGER.info("Вывод успешен");
        return ResponseEntity.ok(guestsDto);
    }
}
