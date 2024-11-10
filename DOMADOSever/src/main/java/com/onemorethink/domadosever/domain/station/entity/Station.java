package com.onemorethink.domadosever.domain.station.entity;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stations")
@Getter @Setter
@NoArgsConstructor
public class Station extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @PositiveOrZero
    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hub_id", nullable = false)
    private Hub hub;

    @OneToMany(mappedBy = "currentStation")
    private List<Bike> parkedBikes = new ArrayList<>();
    
}
