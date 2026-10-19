package org.hotel.service;

import jakarta.transaction.Transactional;
import org.hotel.model.enums.SortType;
import org.hotel.model.entities.UsedService;
import org.hotel.model.exceptions.GuestNotFoundException;
import org.hotel.model.exceptions.WrongSortTypeException;
import org.hotel.dao.repository.UsedServiceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class UsedServiceService {
    /**
     * Репозиторий для работы с использованными услугами в БД.
     */
    private UsedServiceRepository usedServiceRepository;

    /**
     * Сервис управления гостями.
     */
    private GuestService guestService;

    public UsedServiceService(final UsedServiceRepository usedServiceRepositoryP, final GuestService guestServiceP) {
        this.usedServiceRepository = usedServiceRepositoryP;
        this.guestService = guestServiceP;
    }

    public void addUsedService(final UsedService usedService) {
        usedServiceRepository.save(usedService);
    }

    public List<UsedService> getUsedServicesByGuestWithSort(final List<UsedService> usedServices, final SortType sortType) {
        List<UsedService> sortedList = new ArrayList<>(usedServices);
        if (sortType == SortType.PRICE) {
            sortedList.sort(Comparator.comparing(UsedService::getPrice));
        } else if (sortType == SortType.DATE) {
            sortedList.sort(Comparator.comparing(UsedService::getDate));
        } else {
            throw new WrongSortTypeException();
        }
        return sortedList;
    }

    public List<UsedService> getUsedServices(final String id) {
        if (!guestService.isThereGuest(id)) {
            throw new GuestNotFoundException();
        }
        return usedServiceRepository.findServicesUsedByGuest(guestService.getGuest(id));
    }
}
