package com.onemorethink.domadosever.domain.rental.service;

import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import com.onemorethink.domadosever.domain.bike.repository.BikeRepository;
import com.onemorethink.domadosever.domain.rental.dto.HiBikeRequest;
import com.onemorethink.domadosever.domain.rental.dto.HiBikeResponse;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.rental.entity.RentalStatus;
import com.onemorethink.domadosever.domain.rental.repository.RentalRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class HiBikeService {
    private final RentalRepository rentalRepository;
    private final BikeRepository bikeRepository;

    public HiBikeResponse makeHiBike(String userEmail, Integer rentalId, HiBikeRequest request) {
        // 1. 대여 정보 조회 및 검증
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RENTAL_NOT_FOUND));

        // 2. 사용자 검증
        validateRentalOwnership(rental, userEmail);

        // 3. HiBike 생성 가능 상태 검증
        validateHiBikeAvailable(rental);

        // 4. 자전거 상태 업데이트
        Bike bike = rental.getBike();
        updateBikeForHiBike(bike, request.getLatitude(), request.getLongitude());
        bikeRepository.save(bike);

        return HiBikeResponse.builder()
                .rentalId(rental.getId())
                .bikeId(bike.getId())
                .bikeStatus(bike.getStatus())
                .hiBikeStatus(bike.getHiBikeStatus())
                .message("자전거가 성공적으로 HiBike로 전환되었습니다.")
                .build();
    }

    public HiBikeResponse cancelHiBike(String userEmail, Integer rentalId) {
        // 1. 대여 정보 조회 및 검증
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RENTAL_NOT_FOUND));

        // 2. 사용자 검증
        validateRentalOwnership(rental, userEmail);

        // 3. HiBike 취소 가능 상태 검증
        validateHiBikeCancellable(rental);

        // 4. 자전거 상태 업데이트
        Bike bike = rental.getBike();
        updateBikeForHiBikeCancel(bike);
        bikeRepository.save(bike);

        return HiBikeResponse.builder()
                .rentalId(rental.getId())
                .bikeId(bike.getId())
                .bikeStatus(bike.getStatus())
                .hiBikeStatus(bike.getHiBikeStatus())
                .message("HiBike가 성공적으로 취소되었습니다.")
                .build();
    }

    private void validateRentalOwnership(Rental rental, String userEmail) {
        if (!rental.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.RENTAL_NOT_OWNED);
        }
    }

    private void validateHiBikeAvailable(Rental rental) {
        if (rental.getStatus() != RentalStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.RENTAL_NOT_IN_PROGRESS);
        }

        Bike bike = rental.getBike();
        if (bike.getStatus() != BikeStatus.TEMPORARY_LOCKED) {
            throw new BusinessException(ErrorCode.BIKE_NOT_LOCKED,
                    "HiBike로 전환하기 위해서는 자전거가 일시잠금 상태여야 합니다.");
        }

        if (bike.getHiBikeStatus() != HiBikeStatus.NONE) {
            throw new BusinessException(ErrorCode.ALREADY_HIBIKE,
                    "이미 HiBike 상태입니다.");
        }
    }

    private void validateHiBikeCancellable(Rental rental) {
        if (rental.getStatus() != RentalStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.RENTAL_NOT_IN_PROGRESS);
        }

        Bike bike = rental.getBike();
        if (bike.getHiBikeStatus() != HiBikeStatus.AVAILABLE_FOR_RENT) {
            throw new BusinessException(ErrorCode.NOT_HIBIKE,
                    "HiBike 상태가 아닙니다.");
        }
    }

    private void updateBikeForHiBike(Bike bike, Double latitude, Double longitude) {
        bike.setCurrentLatitude(latitude);
        bike.setCurrentLongitude(longitude);
        bike.setHiBikeStatus(HiBikeStatus.AVAILABLE_FOR_RENT);
    }

    private void updateBikeForHiBikeCancel(Bike bike) {
        bike.setHiBikeStatus(HiBikeStatus.NONE);
    }
}