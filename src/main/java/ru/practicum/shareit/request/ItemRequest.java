package ru.practicum.shareit.request;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

@Data
@Entity
public class ItemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "requestor_id")
    private Integer requestorId;
    @Column(name = "description")
    private String description;
    private String created;
}