package ru.practicum.shareit.item.dto;

import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;

public class ItemMapper {

    public static ItemDto itemToItemDto(final Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .build();
    }

    public static ItemWithBookingDto itemToItemWithBookingDto(final Item item, final Booking lastBooking,
                                                              final Booking nextBooking) {
        return ItemWithBookingDto.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .lastBooking(new BookingShortDto(lastBooking.getId(), lastBooking.getBooker().getId()))
                .nextBooking(new BookingShortDto(nextBooking.getId(), nextBooking.getBooker().getId()))
                .build();
    }

    public static ItemWithBookingDto itemToItemWithNextBookingDto(final Item item, final Booking nextBooking) {
        return ItemWithBookingDto.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .nextBooking(new BookingShortDto(nextBooking.getId(), nextBooking.getBooker().getId()))
                .build();
    }

    public static ItemWithBookingDto itemToItemWithLastBookingDto(final Item item, final Booking lastBooking) {
        return ItemWithBookingDto.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .lastBooking(new BookingShortDto(lastBooking.getId(), lastBooking.getBooker().getId()))
                .build();
    }

    public static ItemWithBookingDto itemToItemWithBookingDto(final Item item) {
        return ItemWithBookingDto.builder()
                .id(item.getId())
                .owner(item.getOwner())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .request(item.getRequest())
                .build();
    }

    public static Item itemRegisterDtoToItem(final ItemRegisterDto itemRegisterDto) {
        return Item.builder()
                .owner(itemRegisterDto.getOwner())
                .name(itemRegisterDto.getName())
                .description(itemRegisterDto.getDescription())
                .available(itemRegisterDto.getAvailable())
                .request(itemRegisterDto.getRequest())
                .build();
    }

    public static CommentDto commentToCommentDto(final Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(comment.getCreated())
                .build();
    }
}
