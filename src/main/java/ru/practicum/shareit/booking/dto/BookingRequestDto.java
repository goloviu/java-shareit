package ru.practicum.shareit.booking.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class BookingRequestDto {
    @NotBlank(message = "ID предмета не указан")
    private Long itemId;
    @NotBlank(message = "Время начала бронирования не задано")
    private LocalDateTime start;
    @NotBlank(message = "Время окончания бронирования не задано")
    private LocalDateTime end;
}