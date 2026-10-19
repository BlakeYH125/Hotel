package org.hotel.dao.repository;


import org.hotel.model.entities.Guest;
import org.hotel.model.entities.UsedService;

import java.util.List;

public interface UsedServiceRepository {
    void save(UsedService usedService);
    List<UsedService> findServicesUsedByGuest(Guest guest);
}
