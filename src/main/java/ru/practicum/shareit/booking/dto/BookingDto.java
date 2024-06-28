package ru.practicum.shareit.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.BookingStatusType;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    @NotBlank(message = "ID бронирования не указан")
    private Long id;
    @NotBlank(message = "Время начала бронирования не задано")
    private String start;
    @NotBlank(message = "Время окончания бронирования не задано")
    private String end;
    @NotBlank(message = "ID пользователя не указан")
    private User booker;
    @NotBlank(message = "Статус не указан")
    private BookingStatusType status;
    @NotBlank(message = "Предмет не указан")
    private Item item;
}