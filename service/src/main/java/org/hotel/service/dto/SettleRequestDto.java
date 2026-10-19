package org.hotel.service.dto;

import java.util.List;

public class SettleRequestDto {
    /**
     * Уникальный id комнаты.
     */
    private String roomId;

    /**
     * Количество дней для заселения.
     */
    private int daysCount;

    /**
     * Список гостей.
     */
    private List<GuestDto> guests;

    public SettleRequestDto() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomIdP) {
        this.roomId = roomIdP;
    }

    public int getDaysCount() {
        return daysCount;
    }

    public void setDaysCount(int daysCountP) {
        this.daysCount = daysCountP;
    }

    public List<GuestDto> getGuests() {
        return guests;
    }

    public void setGuests(List<GuestDto> guestsP) {
        this.guests = guestsP;
    }
}
