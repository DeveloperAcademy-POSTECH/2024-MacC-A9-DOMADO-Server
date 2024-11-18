package com.onemorethink.domadosever.domain.rental.entity;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.payment.entity.Payment;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.global.common.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_id", nullable = false)
    private Bike bike;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @PositiveOrZero
    private Integer usageMinutes;

    @PositiveOrZero
    private Integer pauseMinutes = 0;
    private LocalDateTime lastPauseStartTime;  // 마지막 일시정지 시작 시간

    @OneToOne(mappedBy = "rental", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalStatus status = RentalStatus.IN_PROGRESS;

    @Column(nullable = false)
    private boolean couponApplied = false;  // 쿠폰 적용 여부

}
