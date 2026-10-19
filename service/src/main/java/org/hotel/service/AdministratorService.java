package org.hotel.service;

import jakarta.transaction.Transactional;
import org.hotel.model.constants.TimeConstants;
import org.hotel.model.interfaces.Priceable;
import org.hotel.model.entities.Guest;
import org.hotel.model.entities.Service;
import org.hotel.model.entities.Room;
import org.hotel.model.entities.UsedService;
import org.hotel.model.entities.User;
import org.hotel.model.enums.GuestStatus;
import org.hotel.model.enums.Role;
import org.hotel.model.enums.SortType;
import org.hotel.model.exceptions.GuestNotFoundException;
import org.hotel.model.exceptions.ServiceNotFoundException;
import org.hotel.model.exceptions.UserNotFoundException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.dao.repository.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


@org.springframework.stereotype.Service
@Transactional
public class AdministratorService {
    /**
     * Класс управления гостями.
     */
    private final GuestService guestService;

    /**
     * Класс управления комнатами.
     */
    private final RoomService roomService;

    /**
     * Класс управления услугами.
     */
    private final ServiceService serviceService;

    /**
     * Класс управления использованными услугами.
     */
    private final UsedServiceService usedServiceService;

    /**
     * Класс управления пользователями.
     */
    private final UserRepository userRepository;

    public AdministratorService(final GuestService guestServiceP, final RoomService roomServiceP, final ServiceService serviceServiceP, final UsedServiceService usedServiceServiceP, UserRepository userRepositoryP) {
        this.guestService = guestServiceP;
        this.roomService = roomServiceP;
        this.serviceService = serviceServiceP;
        this.usedServiceService = usedServiceServiceP;
        this.userRepository = userRepositoryP;
    }

    public void settle(String roomId, List<Guest> guests, int daysCount, String username) {
        if (daysCount <= 0) {
            throw new IllegalArgumentException("Количество дней проживания должно быть больше нуля");
        }

        if (guests == null || guests.isEmpty()) {
            throw new IllegalArgumentException("Для заселения необходимо указать хотя бы одного гостя");
        }

        Room room = roomService.getRoomForUpdate(roomId);
        if (guests.size() > room.getCapacity()) {
            throw new IllegalArgumentException("Количество гостей превышает вместимость номера");
        }

        User currentUser = userRepository.findByUsername(username).orElseThrow(UserNotFoundException::new);

        Date arriveDate = new Date();
        Date departureDate = new Date(arriveDate.getTime() + daysCount * TimeConstants.MSEC_IN_DAY);

        roomService.setOccupiedToSettle(roomId, daysCount);

        for (Guest guest : guests) {
            guest.setRentRoomId(roomId);
            guest.setArriveDate(arriveDate);
            guest.setDepartureDate(departureDate);
            guest.setPricePerDay(room.getPrice());
            guest.setStatus(GuestStatus.SETTLED);
            if (currentUser.getRole() == Role.ROLE_USER) {
                guest.setUser(currentUser);
            }
            guestService.addGuest(guest);
        }
    }

    public void evict(final String roomIdP) {
        roomService.setAvailableToEvict(roomIdP);
        Room room = roomService.getRoom(roomIdP);
        List<Guest> guests = roomService.getCurrentGuests(room);
        for (Guest guest : guests) {
            guestService.setEvicted(guest.getId());
        }
    }

    public boolean isUserOwnerOfGuest(String username, String guestId) {
        return userRepository.isOwner(username, guestId);
    }

    public void useServiceByGuest(final String guestId, final String serviceId) {
        if (!guestService.isThereGuest(guestId)) {
            throw new GuestNotFoundException();
        }
        if (!serviceService.isThereService(serviceId)) {
            throw new ServiceNotFoundException();
        }
        UsedService usedService = new UsedService(serviceId, guestId, serviceService.getServicePrice(serviceId), new Date());
        usedServiceService.addUsedService(usedService);
    }

    public List<Priceable> getPriceOfRoomsAndServicesWithSort(final SortType sortType) {
        if (sortType != SortType.PRICE && sortType != SortType.SECTION) {
            throw new WrongSortTypeException();
        }
        List<Priceable> catalog = new ArrayList<>();
        if (sortType == SortType.PRICE) {
            catalog.addAll(roomService.getRooms());
            catalog.addAll(new ArrayList<>(serviceService.getServices()));
            catalog.sort(Comparator.comparing(Priceable::getPrice));
        } else if (sortType == SortType.SECTION) {
            catalog.addAll(new ArrayList<>(roomService.getRooms()));
            List<Service> tempCatalog = new ArrayList<>(serviceService.getServices());
            catalog.sort(Comparator.comparing(Priceable::getPrice));
            tempCatalog.sort(Comparator.comparing(Service::getServiceSection));
            catalog.addAll(tempCatalog);
        }
        return catalog;
    }
}
