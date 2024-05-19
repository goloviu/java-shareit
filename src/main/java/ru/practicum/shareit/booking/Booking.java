package ru.practicum.shareit.booking;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class Booking {
    private Integer id;
    @NotBlank(message = "Время начала бронирования не задано")
    private LocalDateTime start;
    @NotBlank(message = "Время окончания бронирования не задано")
    private LocalDateTime end;
    @NotBlank(message = "ID предмета не указан")
    private Integer itemId;
    @NotBlank(message = "ID пользователя не указан")
    private Integer bookerId;
    private BookingStatusType status;
}
