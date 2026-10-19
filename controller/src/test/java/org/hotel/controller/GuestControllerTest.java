package org.hotel.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hotel.service.AdministratorService;
import org.hotel.service.GuestService;
import org.hotel.service.dto.GuestDto;
import org.hotel.model.entities.Guest;
import org.hotel.service.mapper.DtoMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@WebMvcTest(GuestController.class)
public class GuestControllerTest {

    @MockBean
    private AdministratorService administratorService;

    @MockBean
    private GuestService guestService;

    @MockBean
    private DtoMapper dtoMapper;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void showGuests_ShouldReturn200AndList_WhenAdminRequests() throws Exception {
        Guest guest = new Guest();
        guest.setId("g1");

        GuestDto guestDto = new GuestDto();
        guestDto.setId("g1");

        when(guestService.getActualGuests()).thenReturn(List.of(guest));
        when(dtoMapper.toGuestDto(any(Guest.class))).thenReturn(guestDto);

        mockMvc.perform(get("/hotel/guests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value("g1"));

        verify(guestService, times(1)).getActualGuests();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void showGuests_ShouldReturn403_WhenUserRequests() throws Exception {
        mockMvc.perform(get("/hotel/guests"))
                .andExpect(status().isForbidden());
    }

    @Test
    void showGuests_ShouldReturn401_WhenNoNameRequests() throws Exception {
        mockMvc.perform(get("/hotel/guests"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void getGuestsCount_ShouldReturn200AndCount_WhenAdminRequests() throws Exception {
        when(guestService.getGuestsCount()).thenReturn(5);

        mockMvc.perform(get("/hotel/guests/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(guestService, times(1)).getGuestsCount();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void getGuestsCount_ShouldReturn403_WhenUserRequests() throws Exception {
        mockMvc.perform(get("/hotel/guests/count"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getGuestsCount_ShouldReturn401_WhenNoNameRequests() throws Exception {
        mockMvc.perform(get("/hotel/guests/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importGuestData_ShouldReturn200_WhenFileIsValidAndAdminRequests() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "guests.csv", "text/csv", "id,name\ng1,Ivan".getBytes());

        when(guestService.importGuests(any())).thenReturn("Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1");

        mockMvc.perform(multipart("/hotel/guests/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Импорт завершен. Количество ошибок: 0, количество успешно считанных строк: 1"));

        verify(guestService, times(1)).importGuests(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importGuestData_ShouldReturn200_WhenFileIsNotValidAndAdminRequests() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "guests.csv", "text/csv", "id,name\ng1".getBytes());

        when(guestService.importGuests(any())).thenReturn("Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0");

        mockMvc.perform(multipart("/hotel/guests/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Импорт завершен. Количество ошибок: 1, количество успешно считанных строк: 0"));

        verify(guestService, times(1)).importGuests(any());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void importGuestData_ShouldReturn400_WhenFileIsEmpty() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "guests.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/hotel/guests/import")
                        .file(emptyFile)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Файл пуст."));

    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void importGuestData_ShouldReturn403_WhenUserRequests() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "guests.csv", "text/csv", "id,name\ng1,Ivan".getBytes());

        mockMvc.perform(multipart("/hotel/guests/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void importGuestData_ShouldReturn401_WhenNoNameRequests() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "guests.csv", "text/csv", "id,name\ng1,Ivan".getBytes());

        mockMvc.perform(multipart("/hotel/guests/import")
                        .file(file)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void exportGuestData_ShouldReturn200AndFile_WhenAdminRequests() throws Exception {
        String csv = "id,name\ng1,Ivan\ng2,Petr";
        when(guestService.exportGuests()).thenReturn(csv);

        mockMvc.perform(get("/hotel/guests/export"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=guest_export.csv"))
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8"))
                .andExpect(content().string(csv));

        verify(guestService, times(1)).exportGuests();
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void exportGuestData_ShouldReturn403_WhenUserRequests() throws Exception {
        mockMvc.perform(get("/hotel/guests/export"))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportGuestData_ShouldReturn401_WhenNoNameRequests() throws Exception {
        mockMvc.perform(get("/hotel/guests/export"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void getTotalCost_ShouldReturn200_WhenAdminRequests() throws Exception {
        String rentRoomId = "r1";
        BigDecimal cost = new BigDecimal(5000);

        when(guestService.getTotalCost(rentRoomId)).thenReturn(cost);

        mockMvc.perform(get("/hotel/guests/{rent-room-id}/total-cost", rentRoomId))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));

        verify(guestService, times(1)).getTotalCost(rentRoomId);
    }

    @Test
    @WithMockUser(username = "ownerUser", authorities = "ROLE_USER")
    void getTotalCost_ShouldReturn200_WhenUserIsOwner() throws Exception {
        String rentRoomId = "r1";
        BigDecimal cost = new BigDecimal(5000);

        when(guestService.isRoomBelongsToUser("ownerUser", rentRoomId)).thenReturn(true);
        when(guestService.getTotalCost(rentRoomId)).thenReturn(cost);

        mockMvc.perform(get("/hotel/guests/{rent-room-id}/total-cost", rentRoomId))
                .andExpect(status().isOk())
                .andExpect(content().string("5000"));

        verify(guestService, times(1)).getTotalCost(rentRoomId);
    }

    @Test
    @WithMockUser(username = "otherUser", authorities = "ROLE_USER")
    void getTotalCost_ShouldReturn403_WhenUserIsNotOwner() throws Exception {
        String rentRoomId = "r1";

        when(guestService.isRoomBelongsToUser("otherUser", rentRoomId)).thenReturn(false);

        mockMvc.perform(get("/hotel/guests/{rent-room-id}/total-cost", rentRoomId))
                .andExpect(status().isForbidden());
    }

    @Test
    void getTotalCost_ShouldReturn401_WhenNoNameRequests() throws Exception {
        mockMvc.perform(get("/hotel/guests/{rent-room-id}/total-cost", "r1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void useService_ShouldReturn200_WhenAdminRequests() throws Exception {
        String gId = "g1";
        String sId = "s1";

        mockMvc.perform(post("/hotel/guests/{gId}/use-service/{sId}", gId, sId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Процесс использования услуги гостем успешен"));

        verify(administratorService, times(1)).useServiceByGuest(gId, sId);
    }

    @Test
    @WithMockUser(username = "ownerUser", authorities = "ROLE_USER")
    void useService_ShouldReturn200_WhenUserIsOwner() throws Exception {
        String gId = "g1";
        String sId = "s1";

        when(administratorService.isUserOwnerOfGuest("ownerUser", gId)).thenReturn(true);

        mockMvc.perform(post("/hotel/guests/{gId}/use-service/{sId}", gId, sId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Процесс использования услуги гостем успешен"));

        verify(administratorService, times(1)).useServiceByGuest(gId, sId);
    }

    @Test
    @WithMockUser(username = "otherUser", authorities = "ROLE_USER")
    void useService_ShouldReturn403_WhenUserIsNotOwner() throws Exception {
        String gId = "g1";
        String sId = "s1";

        when(administratorService.isUserOwnerOfGuest("otherUser", gId)).thenReturn(false);

        mockMvc.perform(post("/hotel/guests/{gId}/use-service/{sId}", gId, sId)
                        .with(csrf()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Вы не можете управлять чужими услугами."));
    }

    @Test
    void useService_ShouldReturn401_WhenNoNameRequests() throws Exception {
        mockMvc.perform(post("/hotel/guests/{gId}/use-service/{sId}", "g1", "s1")
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}