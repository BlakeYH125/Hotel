package org.hotel.controller;

import org.hotel.service.dto.GuestDto;
import org.hotel.service.mapper.DtoMapper;
import org.hotel.service.AdministratorService;
import org.hotel.service.GuestService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/hotel/guests")
public class GuestController {
    /**
     * Логгер для фиксации логов.
     */
    private static final Logger LOGGER = LogManager.getLogger(GuestController.class);

    /**
     * Администратор.
     */
    private final AdministratorService administratorService;

    /**
     * Управление гостями.
     */
    private final GuestService guestService;

    /**
     * Преобразователь в DTO.
     */
    private final DtoMapper dtoMapper;

    public GuestController(final AdministratorService administratorServiceP, final GuestService guestServiceP, final DtoMapper dtoMapperP) {
        this.administratorService = administratorServiceP;
        this.guestService = guestServiceP;
        this.dtoMapper = dtoMapperP;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<GuestDto>> showGuests() {
        LOGGER.info("Начат процесс вывода всех гостей");
        List<GuestDto> guestDtos = guestService.getActualGuests().stream()
                .map(dtoMapper::toGuestDto)
                .toList();
        LOGGER.info("Процесс вывода всех гостей успешен");
        return ResponseEntity.ok(guestDtos);
    }

    @GetMapping("/count")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Integer> getGuestsCount() {
        LOGGER.info("Начат процесс вывода количества гостей");
        int count = guestService.getGuestsCount();
        LOGGER.info("Метод getGuestsCount успешно завершил работу");
        return ResponseEntity.ok(count);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> importGuestData(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        LOGGER.info("Начат процесс импорта данных о госте из csv файла");
        if (multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пуст.");
        }
        String report = guestService.importGuests(multipartFile);
        LOGGER.info(report);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> exportGuestData() {
        LOGGER.info("Начат процесс экспорта гостей");
        String csvData = guestService.exportGuests();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=guest_export.csv");
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        LOGGER.info("Процесс экспорта успешен");
        return ResponseEntity.ok().headers(httpHeaders).body(csvData);
    }

    @GetMapping("/{rent-room-id}/total-cost")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<BigDecimal> getTotalCost(@PathVariable("rent-room-id") String rentRoomId, Authentication authentication) {
        String currentUsername = authentication.getName();

        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            boolean isOwner = guestService.isRoomBelongsToUser(currentUsername, rentRoomId);
            if (!isOwner) {
                LOGGER.warn("Пользователь {} пытался получить счет для чужой комнаты {}", currentUsername, rentRoomId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        LOGGER.info("Начат процесс вывода суммы которую должен заплатить гость");
        BigDecimal totalCost = guestService.getTotalCost(rentRoomId);
        LOGGER.info("Процесс вывода суммы успешен.");
        return ResponseEntity.ok(totalCost);
    }

    @PostMapping("/{gId}/use-service/{sId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<String> useService(@PathVariable("gId") String gId, @PathVariable("sId") String sId, Authentication authentication) {
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            boolean isOwner = administratorService.isUserOwnerOfGuest(currentUsername, gId);
            if (!isOwner) {
                LOGGER.warn("Пользователь {} пытался заказать услугу для чужого профиля {}", currentUsername, gId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Вы не можете управлять чужими услугами.");
            }
        }
        LOGGER.info("Начат процесс использования услуги гостем");
        administratorService.useServiceByGuest(gId, sId);
        LOGGER.info("Процесс использования услуги гостем успешен");
        return ResponseEntity.ok("Процесс использования услуги гостем успешен");
    }
}
