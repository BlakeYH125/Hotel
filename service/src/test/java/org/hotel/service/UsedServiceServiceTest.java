package org.hotel.service;

import org.hotel.dao.repository.UsedServiceRepository;
import org.hotel.model.entities.Guest;
import org.hotel.model.entities.UsedService;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.GuestNotFoundException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class UsedServiceServiceTest {

    @Mock
    private UsedServiceRepository usedServiceRepository;

    @Mock
    private GuestService guestService;

    @InjectMocks
    private UsedServiceService usedServiceService;

    @Test
    void addUsedService_SaveUsedService() {
        UsedService usedService = new UsedService();

        usedServiceService.addUsedService(usedService);

        Mockito.verify(usedServiceRepository, Mockito.times(1)).save(usedService);
    }

    @Test
    void getUsedServicesByGuestWithSort_ReturnSortedUsedServices_WhenAllCorrect() {
        UsedService us1 = new UsedService();
        us1.setPrice(new BigDecimal(500));
        UsedService us2 = new UsedService();
        us2.setPrice(new BigDecimal(450));

        List<UsedService> expected = new ArrayList<>(List.of(us2, us1));

        List<UsedService> actual = usedServiceService.getUsedServicesByGuestWithSort(new ArrayList<>(List.of(us1, us2)), SortType.PRICE);

        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getUsedServicesByGuestWithSort_ThrowWrongSortTypeException_WhenWrongSortType() {
        UsedService us1 = new UsedService();
        us1.setPrice(new BigDecimal(500));
        UsedService us2 = new UsedService();
        us2.setPrice(new BigDecimal(450));

        Assertions.assertThrows(WrongSortTypeException.class, () -> usedServiceService.getUsedServicesByGuestWithSort(new ArrayList<>(List.of(us1, us2)), SortType.ALPHABET));
    }

    @Test
    void getUsedService_ReturnUsedServices_WhenAllCorrect() {
        Guest guest = new Guest();
        guest.setId("g1");

        UsedService us1 = new UsedService();
        UsedService us2 = new UsedService();

        Mockito.when(guestService.isThereGuest("g1")).thenReturn(true);
        Mockito.when(guestService.getGuest("g1")).thenReturn(guest);
        Mockito.when(usedServiceRepository.findServicesUsedByGuest(guest)).thenReturn(new ArrayList<>(List.of(us1, us2)));

        List<UsedService> expected = new ArrayList<>(List.of(us1, us2));
        List<UsedService> actual = usedServiceService.getUsedServices("g1");

        Assertions.assertEquals(expected, actual);

        Mockito.verify(usedServiceRepository, Mockito.times(1)).findServicesUsedByGuest(guest);
        Mockito.verify(guestService, Mockito.times(1)).isThereGuest("g1");
        Mockito.verify(guestService, Mockito.times(1)).getGuest("g1");
    }

    @Test
    void getUsedService_ThrowGuestNotFoundException_WhenGuestDoesNotExists() {
        Mockito.when(guestService.isThereGuest("g1")).thenReturn(false);

        Assertions.assertThrows(GuestNotFoundException.class, () -> usedServiceService.getUsedServices("g1"));
    }
}
