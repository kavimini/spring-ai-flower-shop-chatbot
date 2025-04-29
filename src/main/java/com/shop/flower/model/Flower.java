package com.shop.flower.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "flower")
@Data
public class Flower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String type;

    private double price;

}
