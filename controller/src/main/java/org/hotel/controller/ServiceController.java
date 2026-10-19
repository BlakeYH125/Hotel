package org.hotel.controller;

import org.hotel.service.dto.ServiceDto;
import org.hotel.service.dto.UsedServiceDto;
import org.hotel.model.entities.UsedService;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.service.mapper.DtoMapper;
import org.hotel.service.AdministratorService;
import org.hotel.model.interfaces.Priceable;
import org.hotel.service.ServiceService;
import org.hotel.model.entities.Service;
import org.hotel.model.enums.SortType;
import org.hotel.service.UsedServiceService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import java.util.List;


@RestController
@RequestMapping("/hotel/services")
public class ServiceController {
    /**
     * Логгер для фиксации логов.
     */
    private static final Logger LOGGER = LogManager.getLogger(ServiceController.class);

    /**
     * Администратор.
     */
    private final AdministratorService administratorService;

    /**
     * Управление использованными услугами.
     */
    private final UsedServiceService usedServiceService;

    /**
     * Управление услугами.
     */
    private final ServiceService serviceService;

    /**
     * Управление DTO.
     */
    private final DtoMapper dtoMapper;

    public ServiceController(final AdministratorService administratorServiceP, final UsedServiceService usedServiceServiceP, final ServiceService serviceServiceP, final DtoMapper dtoMapperP) {
        this.administratorService = administratorServiceP;
        this.usedServiceService = usedServiceServiceP;
        this.serviceService = serviceServiceP;
        this.dtoMapper = dtoMapperP;
    }

    @PatchMapping("/{id}/new-price")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> changeServicePrice(@PathVariable("id") String id, @RequestParam("newPrice") BigDecimal newPrice) {
        LOGGER.info("Начат процесс изменения цены для услуги " + id);
        serviceService.setNewServicePrice(id, newPrice);
        LOGGER.info("Процесс изменения цены успешен");
        return ResponseEntity.ok("Изменение цены успешно");
    }

    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> addService(@RequestBody ServiceDto serviceDto) {
        LOGGER.info("Начат процесс добавления новой услуги");
        Service newService = dtoMapper.toServiceEntity(serviceDto);
        serviceService.addNewService(newService);
        LOGGER.info("Процесс добавления новой услуги успешен");
        return ResponseEntity.ok("Процесс добавления новой услуги успешен");
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<ServiceDto>> showServices(@RequestParam(required = false, defaultValue = "PRICE") String sort) {
        LOGGER.info("Начат процесс вывода всех услуг");
        SortType sortType;
        if (sort.equalsIgnoreCase("PRICE")) {
            sortType = SortType.PRICE;
        } else if (sort.equalsIgnoreCase("SECTION")) {
            sortType = SortType.SECTION;
        } else {
            throw new WrongSortTypeException();
        }
        List<Service> services = serviceService.getServicesWithSort(sortType);
        List<ServiceDto> serviceDtos = services.stream()
                .map(dtoMapper::toServiceDto)
                .toList();
        LOGGER.info("Процесс вывода всех услуг успешен");
        return ResponseEntity.ok(serviceDtos);
    }

    @GetMapping("/catalog")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<List<Priceable>> showCatalog(@RequestParam(required = false, defaultValue = "PRICE") String sort) {
        LOGGER.info("Начат процесс вывода каталога");
        SortType sortType;
        if (sort.equalsIgnoreCase("PRICE")) {
            sortType = SortType.PRICE;
        } else if (sort.equalsIgnoreCase("SECTION")) {
            sortType = SortType.SECTION;
        } else {
            throw new WrongSortTypeException();
        }
        List<Priceable> catalog = administratorService.getPriceOfRoomsAndServicesWithSort(sortType);
        LOGGER.info("Процесс вывода каталога успешен");
        return ResponseEntity.ok(catalog);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> importServiceData(@RequestParam("file") MultipartFile multipartFile) throws IOException {
        LOGGER.info("Начат процесс импорта из csv файла");
        if (multipartFile.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пуст.");
        }
        String report = serviceService.importServices(multipartFile);
        LOGGER.info(report);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/export")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> exportServiceData() {
        LOGGER.info("Начат процесс экспорта данных из csv файла");
        String csvData = serviceService.exportServices();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=service_export.csv");
        httpHeaders.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8");
        LOGGER.info("Процесс экспорта успешен");
        return ResponseEntity.ok().headers(httpHeaders).body(csvData);
    }

    @GetMapping("/used/{guestId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or (hasAuthority('ROLE_USER') and @administratorService.isUserOwnerOfGuest(authentication.name, #guestId))")
    public ResponseEntity<List<UsedServiceDto>> showServicesUsedByGuest(@PathVariable("guestId") String guestId, @RequestParam(required = false, defaultValue = "DATE") String sort) {
        LOGGER.info("Начат процесс вывода услуг, использованных гостем");
        List<UsedService> usedServices = usedServiceService.getUsedServices(guestId);
        SortType sortType;
        if (sort.equalsIgnoreCase("DATE")) {
            sortType = SortType.DATE;
        } else if (sort.equalsIgnoreCase("PRICE")) {
            sortType = SortType.PRICE;
        } else {
            throw new WrongSortTypeException();
        }
        List<UsedServiceDto> usedServiceDtos = usedServiceService.getUsedServicesByGuestWithSort(usedServices, sortType).stream()
                .map(dtoMapper::toUsedServiceDto)
                .toList();
        LOGGER.info("Процесс вывода услуг успешен");
        return ResponseEntity.ok(usedServiceDtos);
    }
}
