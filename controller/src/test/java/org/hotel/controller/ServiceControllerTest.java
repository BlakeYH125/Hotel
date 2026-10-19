package org.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotel.model.interfaces.Priceable;
import org.hotel.service.AdministratorService;
import org.hotel.service.ServiceService;
import org.hotel.service.UsedServiceService;
import org.hotel.service.dto.RoomDto;
import org.hotel.service.dto.ServiceDto;
import org.hotel.service.dto.UsedServiceDto;
import org.hotel.model.entities.Guest;
import org.hotel.model.entities.Room;
import org.hotel.model.entities.Service;
import org.hotel.model.entities.UsedService;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.ServiceAlreadyExistsException;
import org.hotel.model.exceptions.ServiceNotFoundException;
import org.hotel.service.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(ServiceController.class)
public class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean(name = "administratorService")
    private AdministratorService administratorService;

    @MockBean
    private UsedServiceService usedServiceService;

    @MockBean
    private ServiceService serviceService;

    @MockBean
    private DtoMapper dtoMapper;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeServicePrice_ShouldReturn200_WhenRequestsAdminAndAllCorrect() throws Exception {
        Service service = new Service();
        String id = "s1";
        BigDecimal newPrice = new BigDecimal(500);
        service.setId(id);

        mockMvc.perform(patch("/hotel/services/{id}/new-price", id)
                .with(csrf())
                .param("newPrice", String.valueOf(newPrice)))
                .andExpect(status().isOk())
                .andExpect(content().string("Изменение цены успешно"));
        verify(serviceService, times(1)).setNewServicePrice(id, newPrice);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void changeServicePrice_ShouldReturn403_WhenRequestsUser() throws Exception {
        Service service = new Service();
        String id = "s1";
        BigDecimal newPrice = new BigDecimal(500);
        service.setId(id);

        mockMvc.perform(patch("/hotel/services/{id}/new-price", id)
                        .with(csrf())
                        .param("newPrice", String.valueOf(newPrice)))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeServicePrice_ShouldReturn401_WhenRequestsNoName() throws Exception {
        Service service = new Service();
        String id = "s1";
        BigDecimal newPrice = new BigDecimal(500);
        service.setId(id);

        mockMvc.perform(patch("/hotel/services/{id}/new-price", id)
                        .with(csrf())
                        .param("newPrice", String.valueOf(newPrice)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeServicePrice_ShouldReturn404_WhenServiceNotFound() throws Exception {
        String id = "s1";
        BigDecimal newPrice = new BigDecimal(500);

        doThrow(new ServiceNotFoundException()).when(serviceService).setNewServicePrice(id, newPrice);

        mockMvc.perform(patch("/hotel/services/{id}/new-price", id)
                        .with(csrf())
                        .param("newPrice", String.valueOf(newPrice)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeServicePrice_ShouldReturn400_WhenPriceLessZero() throws Exception {
        String id = "s1";
        BigDecimal newPrice = new BigDecimal(-500);

        doThrow(new IllegalArgumentException()).when(serviceService).setNewServicePrice(id, newPrice);

        mockMvc.perform(patch("/hotel/services/{id}/new-price", id)
                        .with(csrf())
                        .param("newPrice", String.valueOf(newPrice)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void addService_ShouldReturn200_WhenAllCorrectAndRequestsAdmin() throws Exception {
        Service service = new Service();
        service.setId("s1");

        when(dtoMapper.toServiceEntity(any(ServiceDto.class))).thenReturn(service);

        String json = objectMapper.writeValueAsString(service);

        mockMvc.perform(post("/hotel/services/add")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Процесс добавления новой услуги успешен"));
        verify(serviceService, times(1)).addNewService(service);
    }

    @Test
    @WithMockUser(authorities = "ROLE_User")
    void addService_ShouldReturn403_WhenAllCorrectAndRequestsUser() throws Exception {
        Service service = new Service();
        service.setId("s1");

        when(dtoMapper.toServiceEntity(any(ServiceDto.class))).thenReturn(service);

        String json = objectMapper.writeValueAsString(service);

        mockMvc.perform(post("/hotel/services/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void addService_ShouldReturn401_WhenAllCorrectAndRequestsNoName() throws Exception {
        Service service = new Service();
        service.setId("s1");

        when(dtoMapper.toServiceEntity(any(ServiceDto.class))).thenReturn(service);

        String json = objectMapper.writeValueAsString(service);

        mockMvc.perform(post("/hotel/services/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void addService_ShouldReturn409_WhenServiceAlreadyExists() throws Exception {
        Service service = new Service();
        service.setId("s1");

        when(dtoMapper.toServiceEntity(any(ServiceDto.class))).thenReturn(service);

        doThrow(new ServiceAlreadyExistsException()).when(serviceService).addNewService(service);

        String json = objectMapper.writeValueAsString(service);

        mockMvc.perform(post("/hotel/services/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showServices_ShouldReturn200AndList_WhenAllCorrectAndRequestsAdminWithoutParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        when(serviceService.getServicesWithSort(SortType.PRICE)).thenReturn(List.of(service));
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);

        mockMvc.perform(get("/hotel/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("s1"))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(serviceService, times(1)).getServicesWithSort(SortType.PRICE);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showServices_ShouldReturn200AndList_WhenAllCorrectAndRequestsUserWithoutParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        when(serviceService.getServicesWithSort(SortType.PRICE)).thenReturn(List.of(service));
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);

        mockMvc.perform(get("/hotel/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("s1"))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(serviceService, times(1)).getServicesWithSort(SortType.PRICE);
    }

    @Test
    void showServices_ShouldReturn403AndList_WhenAllCorrectAndRequestsNoNameWithoutParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        when(serviceService.getServicesWithSort(SortType.PRICE)).thenReturn(List.of(service));
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);

        mockMvc.perform(get("/hotel/services"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showServices_ShouldReturn200AndList_WhenAllCorrectAndRequestsAdminWithParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        when(serviceService.getServicesWithSort(SortType.SECTION)).thenReturn(List.of(service));
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);

        mockMvc.perform(get("/hotel/services")
                        .param("sort", "SECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("s1"))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(serviceService, times(1)).getServicesWithSort(SortType.SECTION);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showServices_ShouldReturn400AndList_WhenWrongParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);

        mockMvc.perform(get("/hotel/services")
                        .param("sort", "ABC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showCatalog_ShouldReturn200AndList_WhenAllCorrectAndRequestsAdminWithoutParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        Room room = new Room();
        room.setId("r1");
        room.setPrice(new BigDecimal(1000));

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        List<Priceable> catalog = new ArrayList<>(List.of(service, room));

        when(administratorService.getPriceOfRoomsAndServicesWithSort(SortType.PRICE)).thenReturn(catalog);
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/services/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("s1"))
                .andExpect(jsonPath("$[0].price").value(500))
                .andExpect(jsonPath("$[1].id").value("r1"))
                .andExpect(jsonPath("$[1].price").value(1000));
        verify(administratorService, times(1)).getPriceOfRoomsAndServicesWithSort(SortType.PRICE);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showCatalog_ShouldReturn200AndList_WhenAllCorrectAndRequestsUserWithoutParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        Room room = new Room();
        room.setId("r1");
        room.setPrice(new BigDecimal(1000));

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        List<Priceable> catalog = new ArrayList<>(List.of(service, room));

        when(administratorService.getPriceOfRoomsAndServicesWithSort(SortType.PRICE)).thenReturn(catalog);
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/services/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("s1"))
                .andExpect(jsonPath("$[0].price").value(500))
                .andExpect(jsonPath("$[1].id").value("r1"))
                .andExpect(jsonPath("$[1].price").value(1000));
        verify(administratorService, times(1)).getPriceOfRoomsAndServicesWithSort(SortType.PRICE);
    }

    @Test
    void showCatalog_ShouldReturn403AndList_WhenAllCorrectAndRequestsNoNameWithoutParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        Room room = new Room();
        room.setId("r1");
        room.setPrice(new BigDecimal(1000));

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        List<Priceable> catalog = new ArrayList<>(List.of(service, room));

        when(administratorService.getPriceOfRoomsAndServicesWithSort(SortType.PRICE)).thenReturn(catalog);
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/services/catalog"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showCatalog_ShouldReturn200AndList_WhenAllCorrectAndRequestsAdminWithParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        Room room = new Room();
        room.setId("r1");
        room.setPrice(new BigDecimal(1000));

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        List<Priceable> catalog = new ArrayList<>(List.of(service, room));

        when(administratorService.getPriceOfRoomsAndServicesWithSort(SortType.SECTION)).thenReturn(catalog);
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/services/catalog")
                        .param("sort", "SECTION"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value("s1"))
                .andExpect(jsonPath("$[0].price").value(500))
                .andExpect(jsonPath("$[1].id").value("r1"))
                .andExpect(jsonPath("$[1].price").value(1000));
        verify(administratorService, times(1)).getPriceOfRoomsAndServicesWithSort(SortType.SECTION);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showCatalog_ShouldReturn400AndList_WhenWrongParam() throws Exception {
        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId("s1");
        serviceDto.setPrice(new BigDecimal(500));

        Room room = new Room();
        room.setId("r1");
        room.setPrice(new BigDecimal(1000));

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        List<Priceable> catalog = new ArrayList<>(List.of(service, room));

        when(administratorService.getPriceOfRoomsAndServicesWithSort(SortType.PRICE)).thenReturn(catalog);
        when(dtoMapper.toServiceDto(any(Service.class))).thenReturn(serviceDto);
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/services/catalog")
                        .param("sort", "ABC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showServicesUsedByGuest_ShouldReturn200AndList_WhenRequestsAdminWithoutParams() throws Exception {
        Guest guest = new Guest();
        guest.setId("g1");

        UsedService us1 = new UsedService();
        us1.setPrice(new BigDecimal(500));

        UsedServiceDto usd1 = new UsedServiceDto();
        usd1.setPrice(new BigDecimal(500));

        when(usedServiceService.getUsedServices("g1")).thenReturn(List.of(us1));
        when(usedServiceService.getUsedServicesByGuestWithSort(List.of(us1), SortType.DATE)).thenReturn(List.of(us1));
        when(dtoMapper.toUsedServiceDto(any(UsedService.class))).thenReturn(usd1);
        when(administratorService.isUserOwnerOfGuest("user", "g1")).thenReturn(true);

        mockMvc.perform(get("/hotel/services/used/{guestId}", "g1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(usedServiceService, times(1)).getUsedServices("g1");
    }

    @Test
    @WithMockUser(username = "otherUser", authorities = "ROLE_USER")
    void showServicesUsedByGuest_ShouldReturn403_WhenUserRequestsAnotherGuestsServices() throws Exception {
        when(administratorService.isUserOwnerOfGuest("otherUser", "g1")).thenReturn(false);

        mockMvc.perform(get("/hotel/services/used/{guestId}", "g1"))
                .andExpect(status().isForbidden());

        verify(administratorService).isUserOwnerOfGuest("otherUser", "g1");
    }

    @Test
    @WithMockUser(username = "testUser", authorities = "ROLE_USER")
    void showServicesUsedByGuest_ShouldReturn200AndList_WhenRequestsUserWithoutParams() throws Exception {
        Guest guest = new Guest();
        guest.setId("g1");

        UsedService us1 = new UsedService();
        us1.setPrice(new BigDecimal(500));

        UsedServiceDto usd1 = new UsedServiceDto();
        usd1.setPrice(new BigDecimal(500));

        when(usedServiceService.getUsedServices("g1")).thenReturn(List.of(us1));
        when(usedServiceService.getUsedServicesByGuestWithSort(List.of(us1), SortType.DATE)).thenReturn(List.of(us1));
        when(dtoMapper.toUsedServiceDto(any(UsedService.class))).thenReturn(usd1);
        when(administratorService.isUserOwnerOfGuest("testUser", "g1")).thenReturn(true);

        mockMvc.perform(get("/hotel/services/used/{guestId}", "g1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(usedServiceService, times(1)).getUsedServices("g1");
    }

    @Test
    void showServicesUsedByGuest_ShouldReturn200AndList_WhenRequestsNoNameWithoutParams() throws Exception {
        mockMvc.perform(get("/hotel/services/used/{guestId}", "g1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showServicesUsedByGuest_ShouldReturn200AndList_WhenRequestsAdminWithParams() throws Exception {
        Guest guest = new Guest();
        guest.setId("g1");

        UsedService us1 = new UsedService();
        us1.setPrice(new BigDecimal(500));

        UsedServiceDto usd1 = new UsedServiceDto();
        usd1.setPrice(new BigDecimal(500));

        when(usedServiceService.getUsedServices("g1")).thenReturn(List.of(us1));
        when(usedServiceService.getUsedServicesByGuestWithSort(List.of(us1), SortType.PRICE)).thenReturn(List.of(us1));
        when(dtoMapper.toUsedServiceDto(any(UsedService.class))).thenReturn(usd1);

        mockMvc.perform(get("/hotel/services/used/{guestId}", "g1")
                        .param("sort", "PRICE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(usedServiceService, times(1)).getUsedServices("g1");
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showServicesUsedByGuest_ShouldReturn409AndList_WhenRequestsAdminWithWrongParam() throws Exception {
        mockMvc.perform(get("/hotel/services/used/{guestId}", "g1")
                        .param("sort", "ABC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importServiceData_ShouldReturn200AndString_WhenAllCorrectAndRequestsAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "services.csv", "text/csv", "id,name,price\ns1,DINNER,500".getBytes());

        when(serviceService.importServices(any())).thenReturn("Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1");

        mockMvc.perform(multipart("/hotel/services/import")
                .file(file)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1"));
        verify(serviceService, times(1)).importServices(file);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void importServiceData_ShouldReturn200AndString_WhenAllCorrectAndRequestsUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "services.csv", "text/csv", "id,name,price\ns1,DINNER,500".getBytes());

        when(serviceService.importServices(any())).thenReturn("Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1");

        mockMvc.perform(multipart("/hotel/services/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importServiceData_ShouldReturn200AndString_WhenFileWrongAndRequestsAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "services.csv", "text/csv", "id,name,price\ns1,500".getBytes());

        when(serviceService.importServices(any())).thenReturn("Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0");

        mockMvc.perform(multipart("/hotel/services/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0"));
        verify(serviceService, times(1)).importServices(file);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void importServiceData_ShouldReturn200AndString_WhenFileWrongAndRequestsUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "services.csv", "text/csv", "id,name,price\ns1,500".getBytes());

        when(serviceService.importServices(any())).thenReturn("Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0");

        mockMvc.perform(multipart("/hotel/services/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importServiceData_ShouldReturn400_AndString_WhenFileEmpty() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "services.csv", "text/csv", "".getBytes());

        mockMvc.perform(multipart("/hotel/services/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Файл пуст."));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void exportServiceData_ShouldReturn200AndFile_WhenAdminRequests() throws Exception {
        String csv = "id,name,price\ns1,DINNER,500\ns2,BREAKFAST,500";
        when(serviceService.exportServices()).thenReturn(csv);

        mockMvc.perform(get("/hotel/services/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=service_export.csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andExpect(content().string(csv));

        verify(serviceService, times(1)).exportServices();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void exportServiceData_ShouldReturn403AndFile_WhenUserRequests() throws Exception {
        String csv = "id,name,price\ns1,DINNER,500\ns2,BREAKFAST,500";
        when(serviceService.exportServices()).thenReturn(csv);

        mockMvc.perform(get("/hotel/services/export"))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportServiceData_ShouldReturn401AndFile_WhenNoNameRequests() throws Exception {
        String csv = "id,name,price\ns1,DINNER,500\ns2,BREAKFAST,500";
        when(serviceService.exportServices()).thenReturn(csv);

        mockMvc.perform(get("/hotel/services/export"))
                .andExpect(status().isUnauthorized());
    }
}
