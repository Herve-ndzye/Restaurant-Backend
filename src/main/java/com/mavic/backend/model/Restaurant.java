package com.mavic.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Setter
@Entity
@Table(name = "restaurant", schema = "restaurant")
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 100)
    private String name;

    @Column(name = "cuisine", length = 50)
    private String cuisine;

    @Lob
    @Column(name = "address")
    private String address;

    @ColumnDefault("1")
    @Column(name = "isOpen")
    private Boolean isOpen;

}