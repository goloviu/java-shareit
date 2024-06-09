package ru.practicum.shareit.request;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name = "requests")
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "requestor_id")
    private Long requestorId;
    @Column(name = "description")
    private String description;
}