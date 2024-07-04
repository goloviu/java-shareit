package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class BookingRequestDto {
    @Positive(message = "ID предмета не указан")
    private Long itemId;
    @NotNull(message = "Время начала бронирования не задано")
    private LocalDateTime start;
    @NotNull(message = "Время окончания бронирования не задано")
    private LocalDateTime end;
}