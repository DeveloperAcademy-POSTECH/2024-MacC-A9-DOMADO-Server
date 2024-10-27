package com.onemorethink.domadosever.domain.bike.entity;

import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.station.entity.Hub;
import com.onemorethink.domadosever.domain.station.entity.Station;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bikes")
@Getter
@NoArgsConstructor
public class Bike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String qrCode;

    @Column(nullable = false)
    private Integer batteryLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BikeStatus status = BikeStatus.LOCKED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_hub_id", nullable = false)
    private Hub homeHub;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_station_id")
    private Station currentStation;

    @Column(nullable = false)
    private Double currentLatitude;
    @Column(nullable = false)
    private Double currentLongitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_user_id")
    private User currentUser;

    @OneToMany(mappedBy = "bike")
    private List<Rental> rentals = new ArrayList<>();
}
