package com.onemorethink.domadosever.domain.station.entity;

import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hubs")
@Getter @Setter
@NoArgsConstructor

public class Hub extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "hub")
    private List<Station> stations = new ArrayList<>();
}
