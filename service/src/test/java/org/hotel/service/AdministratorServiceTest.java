package org.hotel.service;

import org.hotel.model.interfaces.Priceable;
import org.hotel.model.entities.User;
import org.hotel.model.entities.Room;
import org.hotel.model.entities.Service;
import org.hotel.model.entities.UsedService;
import org.hotel.model.entities.Guest;
import org.hotel.model.enums.GuestStatus;
import org.hotel.model.enums.Role;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.GuestNotFoundException;
import org.hotel.model.exceptions.ServiceNotFoundException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.dao.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AdministratorServiceTest {

    @Mock
    private GuestService guestService;

    @Mock
    private RoomService roomService;

    @Mock
    private ServiceService serviceService;

    @Mock
    private UsedServiceService usedServiceService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdministratorService administratorService;

    @Test
    void settle_ShouldSetsUserToGuest_WhenDoingNormalUser() {
        String roomId = "r1";
        String username = "testUser";
        int daysCount = 3;
        List<Guest> guests = new ArrayList<>(List.of(new Guest("g1", "Ivanov Ivan", 25)));

        User normalUser = new User();
        normalUser.setUsername(username);
        normalUser.setRole(Role.ROLE_USER);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(normalUser));

        Room room = new Room();
        room.setCapacity(1);

        Mockito.when(roomService.getRoomForUpdate(roomId)).thenReturn(room);

        administratorService.settle(roomId, guests, daysCount, username);

        Mockito.verify(roomService).setOccupiedToSettle(roomId, daysCount);
        Mockito.verify(guestService, Mockito.times(1)).addGuest(ArgumentMatchers.any(Guest.class));

        Assertions.assertEquals(normalUser, guests.get(0).getUser());
        Assertions.assertEquals(GuestStatus.SETTLED, guests.get(0).getStatus());
        Assertions.assertEquals(roomId, guests.get(0).getRentRoomId());
    }

    @Test
    void settle_ShouldNotSetsUserToGuest_WhenDoingAdmin() {
        String roomId = "r1";
        String username = "adminUser";
        int daysCount = 3;
        List<Guest> guests = new ArrayList<>(List.of(new Guest("g1", "Ivanov Ivan", 25)));

        User adminUser = new User();
        adminUser.setUsername(username);
        adminUser.setRole(Role.ROLE_ADMIN);

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));

        Room room = new Room();
        room.setCapacity(1);

        Mockito.when(roomService.getRoomForUpdate(roomId)).thenReturn(room);

        administratorService.settle(roomId, guests, daysCount, username);

        Assertions.assertNull(guests.get(0).getUser());
        Mockito.verify(roomService).setOccupiedToSettle(roomId, daysCount);
        Mockito.verify(guestService, Mockito.times(1)).addGuest(ArgumentMatchers.any(Guest.class));
    }

    @Test
    void settle_ShouldThrow_WhenDaysCountIsNotPositive() {
        List<Guest> guests = List.of(new Guest("g1", "Ivanov Ivan", 25));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> administratorService.settle("r1", guests, -1, "testUser"));

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> administratorService.settle("r1", guests, 0, "testUser"));

        Mockito.verifyNoInteractions(roomService, guestService, userRepository);
    }

    @Test
    void settle_ShouldThrow_WhenGuestsCountExceedsRoomCapacity() {
        String roomId = "r1";
        Room room = new Room();
        room.setCapacity(1);

        List<Guest> guests = List.of(
                new Guest("g1", "Ivanov Ivan", 25),
                new Guest("g2", "Petrov Petr", 30)
        );

        Mockito.when(roomService.getRoomForUpdate(roomId)).thenReturn(room);

        Assertions.assertThrows(IllegalArgumentException.class,
                () -> administratorService.settle(roomId, guests, 3, "testUser"));

        Mockito.verify(roomService, Mockito.never()).setOccupiedToSettle(roomId, 3);
        Mockito.verifyNoInteractions(guestService, userRepository);
    }

    @Test
    void isUserOwnerOfGuest_ShouldReturnTrue_WhenUserIsOwner() {
        String username = "testUser";
        String guestId = "g1";

        Mockito.when(userRepository.isOwner(username, guestId)).thenReturn(true);

        boolean actual = administratorService.isUserOwnerOfGuest(username, guestId);

        Assertions.assertTrue(actual);
        Mockito.verify(userRepository, Mockito.times(1)).isOwner(username, guestId);
    }

    @Test
    void evict_ShouldUpdateRoomAndEvictGuests_WhenAllCorrect() {
        String roomId = "r1";
        Room room = new Room();
        room.setId(roomId);

        Guest guest1 = new Guest();
        guest1.setId("g1");
        Guest guest2 = new Guest();
        guest2.setId("g2");
        List<Guest> guests = new ArrayList<>(List.of(guest1, guest2));

        Mockito.when(roomService.getRoom(roomId)).thenReturn(room);
        Mockito.when(roomService.getCurrentGuests(room)).thenReturn(guests);

        administratorService.evict(roomId);

        Mockito.verify(roomService, Mockito.times(1)).setAvailableToEvict(roomId);

        Mockito.verify(roomService, Mockito.times(1)).getRoom(roomId);
        Mockito.verify(roomService, Mockito.times(1)).getCurrentGuests(room);

        Mockito.verify(guestService, Mockito.times(1)).setEvicted("g1");
        Mockito.verify(guestService, Mockito.times(1)).setEvicted("g2");
    }

    @Test
    void useServiceByGuest_ShouldSaveUsedService_WhenAllCorrect() {
        Mockito.when(guestService.isThereGuest("g1")).thenReturn(true);
        Mockito.when(serviceService.isThereService("s1")).thenReturn(true);

        administratorService.useServiceByGuest("g1", "s1");

        Mockito.verify(usedServiceService, Mockito.times(1)).addUsedService(ArgumentMatchers.any(UsedService.class));
    }

    @Test
    void useServiceByGuest_ShouldThrowGuestNotFoundException_WhenGuestDoesNotExists() {
       Mockito.when(guestService.isThereGuest("g1")).thenReturn(false);

        Assertions.assertThrows(GuestNotFoundException.class, () -> administratorService.useServiceByGuest("g1", "s1"));
    }

    @Test
    void useServiceByGuest_ShouldThrowServiceNotFoundException_WhenServiceDoesNotExists() {
        Mockito.when(guestService.isThereGuest("g1")).thenReturn(true);
        Mockito.when(serviceService.isThereService("s1")).thenReturn(false);

        Assertions.assertThrows(ServiceNotFoundException.class, () -> administratorService.useServiceByGuest("g1", "s1"));
    }

    @Test
    void getPriceOfRoomsAndServicesWithSort_ShouldReturnCatalog_WhenAllCorrect() {
        Room room = new Room();
        room.setId("r1");
        room.setPrice(new BigDecimal(2000));

        Service service = new Service();
        service.setId("s1");
        service.setPrice(new BigDecimal(500));

        Mockito.when(roomService.getRooms()).thenReturn(new ArrayList<>(List.of(room)));
        Mockito.when(serviceService.getServices()).thenReturn(new ArrayList<>(List.of(service)));

        List<Priceable> expected = new ArrayList<>(List.of(service, room));

        List<Priceable> actual = administratorService.getPriceOfRoomsAndServicesWithSort(SortType.PRICE);

        Assertions.assertEquals(expected, actual);

        Mockito.verify(roomService, Mockito.times(1)).getRooms();
        Mockito.verify(serviceService, Mockito.times(1)).getServices();
    }

    @Test
    void getPriceOfRoomsAndServicesWithSort_ShouldThrowWrongSortTypeException_WhenWrongSortType() {
        Assertions.assertThrows(WrongSortTypeException.class, () -> administratorService.getPriceOfRoomsAndServicesWithSort(SortType.ALPHABET));
    }
}
