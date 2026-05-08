package com.mavic.backend.model;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@JsonPropertyOrder({"id", "name", "phone", "address", "createdAt"})
@Getter
@Setter
@Entity
@Table(name = "customer", schema = "restaurant")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name",length = 100)
    private String name;

    @Column(name = "phone",length = 20)
    private String phone;

    @Lob
    @Column(name = "address")
    private String address;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false,updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "customer")
    private Set<Order> orders = new LinkedHashSet<>();

}