package ru.practicum.shareit.booking;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "start_date")
    private LocalDateTime start;
    @Column(name = "end_date")
    private LocalDateTime end;
    @Column(name = "item_id")
    private Integer itemId;
    @Column(name = "booker_id")
    private Integer bookerId;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private BookingStatusType status;
}
