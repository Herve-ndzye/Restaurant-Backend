package com.mavic.backend.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mavic.backend.model.enums.Category;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "menuitem", schema = "restaurant")
public class Menuitem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant")
    private Restaurant restaurant;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private Category category;

    @ColumnDefault("1")
    @Column(name = "isAvailable")
    private Boolean isAvailable;

    @JsonIgnore
    @OneToMany(mappedBy = "menuItem")
    private Set<Orderitem> orderitems = new LinkedHashSet<>();

}