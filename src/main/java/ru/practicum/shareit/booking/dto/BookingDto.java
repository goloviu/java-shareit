package ru.practicum.shareit.booking.dto;

import lombok.Data;
import ru.practicum.shareit.booking.BookingStatusType;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class BookingDto {
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