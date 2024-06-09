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
public class BookingOwnerDto {
    @NotBlank(message = "ID предмета не указан")
    private Long id;
    @NotBlank(message = "ID пользователя не указан")
    private User booker;
    @NotBlank(message = "Статус не указан")
    private BookingStatusType status;
    @NotBlank(message = "Предмет не указан")
    private Item item;
}