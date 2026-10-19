package org.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotel.service.AdministratorService;
import org.hotel.service.RoomService;
import org.hotel.service.dto.GuestDto;
import org.hotel.service.dto.RoomDto;
import org.hotel.service.dto.SettleRequestDto;
import org.hotel.model.entities.Guest;
import org.hotel.model.entities.Room;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.RoomNotFoundException;
import org.hotel.model.exceptions.RoomAlreadyOccupiedException;
import org.hotel.model.exceptions.RoomAlreadyExistsException;
import org.hotel.model.exceptions.RoomNotOccupiedException;
import org.hotel.model.exceptions.RoomAlreadyAvailableException;
import org.hotel.model.exceptions.RoomAlreadyInServiceException;
import org.hotel.model.exceptions.ChangeStatusBannedException;
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
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyList;
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

@WebMvcTest(RoomController.class)
public class RoomControllerTest {

    @MockBean
    private AdministratorService administratorService;

    @MockBean
    private RoomService roomService;

    @MockBean
    private DtoMapper dtoMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void settle_ShouldReturn200_WhenRequestIsCorrectAndAdmin() throws Exception {
        SettleRequestDto settleRequestDto = new SettleRequestDto();
        settleRequestDto.setRoomId("r1");
        settleRequestDto.setDaysCount(5);
        GuestDto guestDto = new GuestDto();
        guestDto.setFullName("Ivan Petrov");
        settleRequestDto.setGuests(List.of(guestDto));

        Guest mappedGuest = new Guest();
        mappedGuest.setId("g1");

        when(dtoMapper.toGuestEntity(any(GuestDto.class))).thenReturn(mappedGuest);

        String json = objectMapper.writeValueAsString(settleRequestDto);

        mockMvc.perform(post("/hotel/rooms/settle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Заселение успешно."));

        verify(administratorService, times(1)).settle(eq("r1"), anyList(), eq(5), eq("admin"));
    }

    @Test
    @WithMockUser(username = "user123", authorities = "ROLE_USER")
    void settle_ShouldReturn200_WhenUserRequests() throws Exception {
        SettleRequestDto settleRequestDto = new SettleRequestDto();
        settleRequestDto.setRoomId("r1");
        settleRequestDto.setDaysCount(5);
        GuestDto guestDto = new GuestDto();
        guestDto.setFullName("Ivan Petrov");
        settleRequestDto.setGuests(List.of(guestDto));

        Guest mappedGuest = new Guest();
        mappedGuest.setId("g1");

        when(dtoMapper.toGuestEntity(any(GuestDto.class))).thenReturn(mappedGuest);

        String json = objectMapper.writeValueAsString(settleRequestDto);

        mockMvc.perform(post("/hotel/rooms/settle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Заселение успешно."));

        verify(administratorService, times(1)).settle(eq("r1"), anyList(), eq(5), eq("user123"));
    }

    @Test
    void settle_ShouldReturn401_WhenUnauthenticated() throws Exception {
        SettleRequestDto requestDto = new SettleRequestDto();
        requestDto.setRoomId("r1");
        requestDto.setDaysCount(5);
        String json = objectMapper.writeValueAsString(requestDto);

        mockMvc.perform(post("/hotel/rooms/settle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void settle_ShouldReturnErrorStatus_WhenRoomNotFound() throws Exception {
        SettleRequestDto requestDto = new SettleRequestDto();
        requestDto.setRoomId("r1");
        requestDto.setDaysCount(5);
        requestDto.setGuests(List.of(new GuestDto()));
        String json = objectMapper.writeValueAsString(requestDto);

        doThrow(new RoomNotFoundException()).when(administratorService).settle(eq("r1"), anyList(), eq(5), eq("admin"));

        mockMvc.perform(post("/hotel/rooms/settle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void settle_ShouldReturnConflictStatus_WhenRoomOccupied() throws Exception {
        SettleRequestDto requestDto = new SettleRequestDto();
        requestDto.setRoomId("r1");
        requestDto.setDaysCount(5);
        requestDto.setGuests(List.of(new GuestDto()));
        String json = objectMapper.writeValueAsString(requestDto);

        doThrow(new RoomAlreadyOccupiedException()).when(administratorService).settle(eq("r1"), anyList(), eq(5), eq("admin"));

        mockMvc.perform(post("/hotel/rooms/settle")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void addRoom_ShouldReturn200_WhenAdminRequestsAndAllCorrect() throws Exception {
        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setNumber(101);
        roomDto.setCapacity(3);
        roomDto.setStars(3);
        roomDto.setStatus("AVAILABLE");
        roomDto.setPrice(new BigDecimal(1000));

        Room mappedRoom = new Room();
        mappedRoom.setId("r1");

        when(dtoMapper.toRoomEntity(any(RoomDto.class))).thenReturn(mappedRoom);

        String json = objectMapper.writeValueAsString(roomDto);

        mockMvc.perform(post("/hotel/rooms/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string("Добавление новой комнаты успешно"));
        verify(roomService, times(1)).addNewRoom(any(Room.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void addRoom_ShouldReturn403_WhenRequestMakeUser() throws Exception {
        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setNumber(101);
        roomDto.setCapacity(3);
        roomDto.setStars(3);
        roomDto.setStatus("AVAILABLE");
        roomDto.setPrice(new BigDecimal(1000));

        String json = objectMapper.writeValueAsString(roomDto);

        mockMvc.perform(post("/hotel/rooms/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    void addRoom_ShouldReturnUnauthorized_WhenRequestMakeNoname() throws Exception {
        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setNumber(101);
        roomDto.setCapacity(3);
        roomDto.setStars(3);
        roomDto.setStatus("AVAILABLE");
        roomDto.setPrice(new BigDecimal(1000));

        String json = objectMapper.writeValueAsString(roomDto);

        mockMvc.perform(post("/hotel/rooms/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void addRoom_ShouldReturnConflict_WhenRoomAlreadyExists() throws Exception {
        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setNumber(101);
        roomDto.setCapacity(3);
        roomDto.setStars(3);
        roomDto.setStatus("AVAILABLE");
        roomDto.setPrice(new BigDecimal(1000));

        Room mappedRoom = new Room();
        mappedRoom.setId("r1");

        when(dtoMapper.toRoomEntity(any(RoomDto.class))).thenReturn(mappedRoom);
        doThrow(new RoomAlreadyExistsException()).when(roomService).addNewRoom(any(Room.class));

        String json = objectMapper.writeValueAsString(roomDto);

        mockMvc.perform(post("/hotel/rooms/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void evict_ShouldReturn200_WhenAdminRequests() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/evict", roomId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Выселение успешно"));
        verify(administratorService, times(1)).evict(roomId);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void evict_ShouldReturn403_WhenUserRequests() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/evict", roomId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void evict_ShouldReturn401_WhenNoNameRequests() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/evict", roomId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void evict_ShouldReturn404_WhenRoomDoesNotExists() throws Exception {
        String roomId = "r1";

        doThrow(new RoomNotFoundException()).when(administratorService).evict(roomId);

        mockMvc.perform(post("/hotel/rooms/{id}/evict", roomId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void evict_ShouldReturn409_WhenRoomDoesNotOccupied() throws Exception {
        String roomId = "r1";

        doThrow(new RoomNotOccupiedException()).when(administratorService).evict(roomId);

        mockMvc.perform(post("/hotel/rooms/{id}/evict", roomId)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setAvailable_ShouldReturn200_WhenRequestsAdminAndAllCorrect() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/set-available", roomId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Установка статуса успешна"));

        verify(roomService, times(1)).setAvailable(roomId);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void setAvailable_ShouldReturn403_WhenRequestsUser() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/set-available", roomId)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void setAvailable_ShouldReturn401_WhenRequestsNoName() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/set-available", roomId)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setAvailable_ShouldReturn404_WhenRoomDoesNotExists() throws Exception {
        String roomId = "r1";

        doThrow(new RoomNotFoundException()).when(roomService).setAvailable(roomId);

        mockMvc.perform(post("/hotel/rooms/{id}/set-available", roomId)
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setAvailable_ShouldReturn409_WhenRoomAlreadyAvailable() throws Exception {
        String roomId = "r1";

        doThrow(new RoomAlreadyAvailableException()).when(roomService).setAvailable(roomId);

        mockMvc.perform(post("/hotel/rooms/{id}/set-available", roomId)
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setAvailable_ShouldReturn400_WhenChangeStatusBanned() throws Exception {
        String roomId = "r1";

        doThrow(new ChangeStatusBannedException()).when(roomService).setAvailable(roomId);

        mockMvc.perform(post("/hotel/rooms/{id}/set-available", roomId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setOccupied_ShouldReturn200_WhenRequestsAdminAndAllCorrect() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        mockMvc.perform(post("/hotel/rooms/{id}/set-occupied", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Установка статуса успешна"));

        verify(roomService, times(1)).setOccupied(roomId, daysCount);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void setOccupied_ShouldReturn403_WhenRequestsUser() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        mockMvc.perform(post("/hotel/rooms/{id}/set-occupied", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void setOccupied_ShouldReturn401_WhenRequestsNoName() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        mockMvc.perform(post("/hotel/rooms/{id}/set-occupied", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setOccupied_ShouldReturn404_WhenRoomDoesNotExists() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        doThrow(new RoomNotFoundException()).when(roomService).setOccupied(roomId, daysCount);

        mockMvc.perform(post("/hotel/rooms/{id}/set-occupied", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setOccupied_ShouldReturn409_WhenRoomAlreadyOccupied() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        doThrow(new RoomAlreadyOccupiedException()).when(roomService).setOccupied(roomId, daysCount);

        mockMvc.perform(post("/hotel/rooms/{id}/set-occupied", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setOccupied_ShouldReturn400_WhenChangeStatusBanned() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        doThrow(new ChangeStatusBannedException()).when(roomService).setOccupied(roomId, daysCount);

        mockMvc.perform(post("/hotel/rooms/{id}/set-occupied", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setOccupied_ShouldReturn400BadRequest_WhenDaysCountIsMissing() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/set-occupied", roomId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setInService_ShouldReturn200_WhenRequestsAdminAndAllCorrect() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        mockMvc.perform(post("/hotel/rooms/{id}/set-in-service", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Установка статуса успешна"));

        verify(roomService, times(1)).setInService(roomId, daysCount);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void setInService_ShouldReturn403_WhenRequestsUser() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        mockMvc.perform(post("/hotel/rooms/{id}/set-in-service", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void setInService_ShouldReturn401_WhenRequestsNoName() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        mockMvc.perform(post("/hotel/rooms/{id}/set-in-service", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setInService_ShouldReturn404_WhenRoomDoesNotExists() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        doThrow(new RoomNotFoundException()).when(roomService).setInService(roomId, daysCount);

        mockMvc.perform(post("/hotel/rooms/{id}/set-in-service", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setInService_ShouldReturn409_WhenRoomAlreadyOccupied() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        doThrow(new RoomAlreadyInServiceException()).when(roomService).setInService(roomId, daysCount);

        mockMvc.perform(post("/hotel/rooms/{id}/set-in-service", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setInService_ShouldReturn400_WhenChangeStatusBanned() throws Exception {
        String roomId = "r1";
        int daysCount = 5;

        doThrow(new ChangeStatusBannedException()).when(roomService).setInService(roomId, daysCount);

        mockMvc.perform(post("/hotel/rooms/{id}/set-in-service", roomId)
                        .param("daysCount", String.valueOf(daysCount))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void setInService_ShouldReturn400BadRequest_WhenDaysCountIsMissing() throws Exception {
        String roomId = "r1";

        mockMvc.perform(post("/hotel/rooms/{id}/set-in-service", roomId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeRoomPrice_ShouldReturn200_WhenRequestsAdminAndAllCorrect() throws Exception {
        String roomId = "r1";
        BigDecimal newPrice = new BigDecimal(2000);

        mockMvc.perform(patch("/hotel/rooms/{id}/setNewPrice", roomId)
                        .param("newPrice", String.valueOf(newPrice))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Смена цены успешна"));

        verify(roomService, times(1)).setNewRoomPrice(roomId, newPrice);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void changeRoomPrice_ShouldReturn403_WhenRequestsUser() throws Exception {
        String roomId = "r1";
        BigDecimal newPrice = new BigDecimal(2000);

        mockMvc.perform(patch("/hotel/rooms/{id}/setNewPrice", roomId)
                        .param("newPrice", String.valueOf(newPrice))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void changeRoomPrice_ShouldReturn401_WhenRequestsNoName() throws Exception {
        String roomId = "r1";
        BigDecimal newPrice = new BigDecimal(2000);

        mockMvc.perform(patch("/hotel/rooms/{id}/setNewPrice", roomId)
                        .param("newPrice", String.valueOf(newPrice))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeRoomPrice_ShouldReturn404_WhenRoomDoesNotExists() throws Exception {
        String roomId = "r1";
        BigDecimal newPrice = new BigDecimal(2000);

        doThrow(new RoomNotFoundException()).when(roomService).setNewRoomPrice(roomId, newPrice);

        mockMvc.perform(patch("/hotel/rooms/{id}/setNewPrice", roomId)
                        .param("newPrice", String.valueOf(newPrice))
                        .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeRoomPrice_ShouldReturn400_WhenPriceLessZero() throws Exception {
        String roomId = "r1";
        BigDecimal newPrice = new BigDecimal(-2000);

        doThrow(new IllegalArgumentException()).when(roomService).setNewRoomPrice(roomId, newPrice);

        mockMvc.perform(patch("/hotel/rooms/{id}/setNewPrice", roomId)
                        .param("newPrice", String.valueOf(newPrice))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void changeRoomPrice_ShouldReturn400_WhenNoPrice() throws Exception {
        String roomId = "r1";
        BigDecimal newPrice = new BigDecimal(-2000);

        doThrow(new IllegalArgumentException()).when(roomService).setNewRoomPrice(roomId, newPrice);

        mockMvc.perform(patch("/hotel/rooms/{id}/setNewPrice", roomId)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showAllRooms_ShouldReturn200AndList_WhenDefaultSortAndRequestsAdmin() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        when(roomService.getAllRoomsWithSort(SortType.PRICE)).thenReturn(List.of(room));
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("r1"))
                .andExpect(jsonPath("$[0].price").value(1000));
        verify(roomService, times(1)).getAllRoomsWithSort(SortType.PRICE);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showAllRooms_ShouldReturn200AndList_WhenDefaultSortAndRequestsUser() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        when(roomService.getAllRoomsWithSort(SortType.PRICE)).thenReturn(List.of(room));
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("r1"))
                .andExpect(jsonPath("$[0].price").value(1000));
        verify(roomService, times(1)).getAllRoomsWithSort(SortType.PRICE);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showAllRooms_ShouldReturn200_WhenSortIsStars() throws Exception {
        when(roomService.getAllRoomsWithSort(SortType.STARS)).thenReturn(List.of());

        mockMvc.perform(get("/hotel/rooms")
                        .param("sort", String.valueOf(SortType.STARS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
        verify(roomService, times(1)).getAllRoomsWithSort(SortType.STARS);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showAllRooms_ShouldThrow400_WhenSortIsInvalid() throws Exception {
        mockMvc.perform(get("/hotel/rooms")
                        .param("sort", "ABC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void showAllRooms_ShouldReturn401_WhenRequestsNoName() throws Exception {
        mockMvc.perform(get("/hotel/rooms"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showAllFreeRooms_ShouldReturn200AndList_WhenDefaultSortAndRequestsAdmin() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        when(roomService.getFreeRoomsWithSort(SortType.PRICE)).thenReturn(List.of(room));
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms/free"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("r1"))
                .andExpect(jsonPath("$[0].price").value(1000));
        verify(roomService, times(1)).getFreeRoomsWithSort(SortType.PRICE);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showAllFreeRooms_ShouldReturn200AndList_WhenDefaultSortAndRequestsUser() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(1000));

        when(roomService.getFreeRoomsWithSort(SortType.PRICE)).thenReturn(List.of(room));
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms/free"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("r1"))
                .andExpect(jsonPath("$[0].price").value(1000));
        verify(roomService, times(1)).getFreeRoomsWithSort(SortType.PRICE);
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showAllFreeRooms_ShouldReturn200_WhenSortIsStars() throws Exception {
        when(roomService.getFreeRoomsWithSort(SortType.STARS)).thenReturn(List.of());

        mockMvc.perform(get("/hotel/rooms/free")
                        .param("sort", String.valueOf(SortType.STARS)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
        verify(roomService, times(1)).getFreeRoomsWithSort(SortType.STARS);
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showAllFreeRooms_ShouldThrowException_WhenSortIsInvalid() throws Exception {
        mockMvc.perform(get("/hotel/rooms/free")
                        .param("sort", "ABC"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void showAllFreeRooms_ShouldReturn401_WhenRequestsNoName() throws Exception {
        mockMvc.perform(get("/hotel/rooms/free"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getFreeRoomsCount_ShouldReturn200_WhenRequestsAdminAndAllCorrect() throws Exception {
        when(roomService.getFreeRoomsCount()).thenReturn(1);

        mockMvc.perform(get("/hotel/rooms/available/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
        verify(roomService, times(1)).getFreeRoomsCount();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getFreeRoomsCount_ShouldReturn200_WhenRequestsUserAndAllCorrect() throws Exception {
        when(roomService.getFreeRoomsCount()).thenReturn(1);

        mockMvc.perform(get("/hotel/rooms/available/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
        verify(roomService, times(1)).getFreeRoomsCount();
    }

    @Test
    void getFreeRoomsCount_ShouldReturn401_WhenRequestsNoName() throws Exception {
        mockMvc.perform(get("/hotel/rooms/available/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showFreeRoomsByDate_ShouldReturn200AndList_WhenRequestsAdminAndAllCorrect() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(500));

        when(roomService.getFreeRoomsByDate(any(Date.class))).thenReturn(List.of(room));
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms/free-by-date")
                        .param("date", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("r1"))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(roomService, times(1)).getFreeRoomsByDate(any(Date.class));
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showFreeRoomsByDate_ShouldReturn200AndList_WhenRequestsUserAndAllCorrect() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(500));

        when(roomService.getFreeRoomsByDate(any(Date.class))).thenReturn(List.of(room));
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms/free-by-date")
                        .param("date", "2025-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("r1"))
                .andExpect(jsonPath("$[0].price").value(500));
        verify(roomService, times(1)).getFreeRoomsByDate(any(Date.class));
    }

    @Test
    void showFreeRoomsByDate_ShouldReturn200AndList_WhenRequestsNoName() throws Exception {
        mockMvc.perform(get("/hotel/rooms/free-by-date")
                        .param("date", "2025-12-31"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showFreeRoomsByDate_ShouldReturn200AndList_WhenNoDate() throws Exception {
        mockMvc.perform(get("/hotel/rooms/free-by-date"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getRoomDetails_ShouldReturn200AndRoomDto_WhenRequestsAdminAndAllCorrect() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(500));

        when(roomService.getRoom("r1")).thenReturn(room);
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms/{id}", "r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("r1"))
                .andExpect(jsonPath("$.price").value(500));
        verify(roomService, times(1)).getRoom("r1");
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getRoomDetails_ShouldReturn200AndRoomDto_WhenRequestsUserAndAllCorrect() throws Exception {
        Room room = new Room();
        room.setId("r1");

        RoomDto roomDto = new RoomDto();
        roomDto.setId("r1");
        roomDto.setPrice(new BigDecimal(500));

        when(roomService.getRoom("r1")).thenReturn(room);
        when(dtoMapper.toRoomDto(any(Room.class))).thenReturn(roomDto);

        mockMvc.perform(get("/hotel/rooms/{id}", "r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("r1"))
                .andExpect(jsonPath("$.price").value(500));
        verify(roomService, times(1)).getRoom("r1");
    }

    @Test
    void getRoomDetails_ShouldReturn401_WhenRequestsNoName() throws Exception {
        mockMvc.perform(get("/hotel/rooms/{id}", "r1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getRoomDetails_ShouldReturn404_WhenRoomDoesNotExists() throws Exception {
        doThrow(new RoomNotFoundException()).when(roomService).getRoom("r1");

        mockMvc.perform(get("/hotel/rooms/{id}", "r1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getThreePrevGuests_ShouldReturn200AndList_WhenRequestsAdminAndAllCorrect() throws Exception {
        Room room = new Room();
        room.setId("r1");

        Guest guest = new Guest();
        guest.setId("g1");

        GuestDto guestDto = new GuestDto();
        guestDto.setId("g1");

        when(roomService.getThreePrevRoomGuests("r1")).thenReturn(List.of(guest));
        when(dtoMapper.toGuestDto(any(Guest.class))).thenReturn(guestDto);

        mockMvc.perform(get("/hotel/rooms/{id}/last-guests", "r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("g1"));
        verify(roomService, times(1)).getThreePrevRoomGuests("r1");
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getThreePrevGuests_ShouldReturn403AndList_WhenRequestsUserAndAllCorrect() throws Exception {
        Room room = new Room();
        room.setId("r1");

        Guest guest = new Guest();
        guest.setId("g1");

        GuestDto guestDto = new GuestDto();
        guestDto.setId("g1");

        when(roomService.getThreePrevRoomGuests("r1")).thenReturn(List.of(guest));
        when(dtoMapper.toGuestDto(any(Guest.class))).thenReturn(guestDto);

        mockMvc.perform(get("/hotel/rooms/{id}/last-guests", "r1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getThreePrevGuests_ShouldReturn401AndList_WhenRequestsNoName() throws Exception {
        mockMvc.perform(get("/hotel/rooms/{id}/last-guests", "r1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getThreePrevGuests_ShouldReturn404_WhenRoomDoesNotExists() throws Exception {
        doThrow(new RoomNotFoundException()).when(roomService).getThreePrevRoomGuests("r1");

        mockMvc.perform(get("/hotel/rooms/{id}/last-guests", "r1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importRoomData_ShouldReturn200_WhenFileIsValidAndRequestsAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "rooms.csv", "text/csv", "id,number,price\nr1,101,1000".getBytes());

        when(roomService.importRooms(any())).thenReturn("Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1");

        mockMvc.perform(multipart("/hotel/rooms/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1"));
        verify(roomService, times(1)).importRooms(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importRoomData_ShouldReturn200_WhenFileIsNotValidAndRequestsAdmin() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "rooms.csv", "text/csv", "id,number,price\nr1,1000".getBytes());

        when(roomService.importRooms(any())).thenReturn("Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0");

        mockMvc.perform(multipart("/hotel/rooms/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0"));
        verify(roomService, times(1)).importRooms(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void importRoomData_ShouldReturn403_WhenFileIsValidAndRequestsUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "rooms.csv", "text/csv", "id,number,price\nr1,101,1000".getBytes());

        mockMvc.perform(multipart("/hotel/rooms/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void importRoomData_ShouldReturn403_WhenFileIsNotValidAndRequestsUser() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "rooms.csv", "text/csv", "id,number,price\nr1,1000".getBytes());

        mockMvc.perform(multipart("/hotel/rooms/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void importRoomData_ShouldReturn401_WhenFileIsValidAndRequestsNoName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "rooms.csv", "text/csv", "id,number,price\nr1,101,1000".getBytes());

        mockMvc.perform(multipart("/hotel/rooms/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void importRoomData_ShouldReturn401_WhenFileIsNotValidAndRequestsNoName() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "rooms.csv", "text/csv", "id,number,price\nr1,1000".getBytes());

        mockMvc.perform(multipart("/hotel/rooms/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importRoomData_ShouldReturn400_WhenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "rooms.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/hotel/rooms/import").file(emptyFile).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Файл пуст."));
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void exportRoomData_ShouldReturn200AndFile_WhenAdminRequests() throws Exception {
        String csv = "id,number,price\nr1,101,1000\nr2,102,1500";
        when(roomService.exportRooms()).thenReturn(csv);

        mockMvc.perform(get("/hotel/rooms/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=rooms_export.csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andExpect(content().string(csv));

        verify(roomService, times(1)).exportRooms();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void exportRoomData_ShouldReturn403AndFile_WhenUserRequests() throws Exception {
        String csv = "id,number,price\nr1,101,1000\nr2,102,1500";
        when(roomService.exportRooms()).thenReturn(csv);

        mockMvc.perform(get("/hotel/rooms/export"))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportRoomData_ShouldReturn401AndFile_WhenNoNameRequests() throws Exception {
        String csv = "id,number,price\nr1,101,1000\nr2,102,1500";
        when(roomService.exportRooms()).thenReturn(csv);

        mockMvc.perform(get("/hotel/rooms/export"))
                .andExpect(status().isUnauthorized());
    }
}
