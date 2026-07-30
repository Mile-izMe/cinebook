package com.cinebook.module.city.entity;

import com.cinebook.common.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "city")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class City extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "city_name", nullable = false)
    private String cityName;
}
