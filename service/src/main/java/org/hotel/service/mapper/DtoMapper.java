package org.hotel.service.mapper;

import org.hotel.service.dto.GuestDto;
import org.hotel.service.dto.RoomDto;
import org.hotel.service.dto.ServiceDto;
import org.hotel.service.dto.UsedServiceDto;
import org.hotel.model.entities.Guest;
import org.hotel.model.entities.Room;
import org.hotel.model.entities.Service;
import org.hotel.model.entities.UsedService;
import org.hotel.model.enums.RoomStatus;
import org.hotel.model.enums.ServiceSection;
import org.springframework.stereotype.Component;

@Component
public class DtoMapper {
    public RoomDto toRoomDto(final Room roomP) {
        if (roomP == null) {
            return null;
        }
        RoomDto roomDto = new RoomDto();
        roomDto.setId(roomP.getId());
        roomDto.setNumber(roomP.getNumber());
        roomDto.setPrice(roomP.getPrice());
        roomDto.setCapacity(roomP.getCapacity());
        roomDto.setStars(roomP.getStars());
        roomDto.setStatus(roomP.getStatus().name());
        roomDto.setReleasedIn(roomP.getReleasedIn());
        return roomDto;
    }

    public GuestDto toGuestDto(final Guest guestP) {
        if (guestP == null) {
            return null;
        }
        GuestDto guestDto = new GuestDto();
        guestDto.setId(guestP.getId());
        guestDto.setFullName(guestP.getFullName());
        guestDto.setAge(guestP.getAge());
        guestDto.setRentRoomId(guestP.getRentRoomId());
        guestDto.setArriveDate(guestP.getArriveDate());
        guestDto.setDepartureDate(guestP.getDepartureDate());
        guestDto.setStatus(guestP.getStatus().name());
        return guestDto;
    }

    public ServiceDto toServiceDto(final Service serviceP) {
        if (serviceP == null) {
            return null;
        }
        ServiceDto serviceDto = new ServiceDto();
        serviceDto.setId(serviceP.getId());
        serviceDto.setName(serviceP.getName());
        serviceDto.setPrice(serviceP.getPrice());
        serviceDto.setServiceSection(serviceP.getServiceSection() != null ? serviceP.getServiceSection().name() : null);
        return serviceDto;
    }

    public UsedServiceDto toUsedServiceDto(final UsedService usedServiceP) {
        if (usedServiceP == null) {
            return null;
        }
        UsedServiceDto usedServiceDto = new UsedServiceDto();
        usedServiceDto.setUsedServiceId(usedServiceP.getUsedServiceId());
        usedServiceDto.setGuestId(usedServiceP.getGuestId());
        usedServiceDto.setServiceId(usedServiceP.getServiceId());
        usedServiceDto.setPrice(usedServiceP.getPrice());
        usedServiceDto.setDate(usedServiceP.getDate());
        return usedServiceDto;
    }

    public Guest toGuestEntity(GuestDto guestDto) {
        if (guestDto == null) {
            return null;
        }
        return new Guest(guestDto.getId(), guestDto.getFullName(), guestDto.getAge());
    }

    public Room toRoomEntity(RoomDto roomDto) {
        if (roomDto == null) {
            return null;
        }
        return new Room(roomDto.getId(), roomDto.getNumber(), roomDto.getPrice(), RoomStatus.AVAILABLE, roomDto.getCapacity(), roomDto.getStars());
    }

    public Service toServiceEntity(ServiceDto serviceDto) {
        if (serviceDto == null) {
            return null;
        }
        return new Service(serviceDto.getId(), serviceDto.getName(), serviceDto.getPrice(), ServiceSection.valueOf(serviceDto.getServiceSection()));
    }
}
