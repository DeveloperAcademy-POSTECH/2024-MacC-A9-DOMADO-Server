package com.onemorethink.domadosever.domain.rental.repository;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.rental.entity.RentalStatus;
import com.onemorethink.domadosever.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Integer> {

    // COMPLETED가 아닌 모든 상태 체크
    boolean existsByUserAndStatusNot(User user, RentalStatus status);
    Optional<Rental> findByBikeAndStatus(Bike bike, RentalStatus status);
}
