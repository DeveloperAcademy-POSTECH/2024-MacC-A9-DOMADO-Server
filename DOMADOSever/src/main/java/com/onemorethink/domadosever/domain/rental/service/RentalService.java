package com.onemorethink.domadosever.domain.rental.service;


import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import com.onemorethink.domadosever.domain.bike.repository.BikeRepository;
import com.onemorethink.domadosever.domain.payment.entity.PaymentMethodStatus;
import com.onemorethink.domadosever.domain.payment.repository.PaymentMethodRepository;
import com.onemorethink.domadosever.domain.rental.dto.RentalResponse;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.rental.entity.RentalStatus;
import com.onemorethink.domadosever.domain.rental.repository.RentalRepository;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.repository.UserRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class RentalService {
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final RentalRepository  rentalRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    // TODO: MQTTService 구현 및 필드 추가


    public RentalResponse rentBikeByEmail(String email, String qrCode) {
        // 1-1. 사용자 조회 - 해당 사용자가 등록되어 있는지 확인

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return rentBike(user, qrCode);
    }

    private RentalResponse rentBike(User user, String qrCode) {

        // 1-2. 사용자 상태 검증 - 해당 사용자가 대여 서비스를 이용 가능한지 확인
        validateUserStatus(user);

        // 2-1. 자전거 조회 - 해당 자전가 등록되어 있는지 확인
        Bike bike = bikeRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));

        // 2-2. 자전거 상태 검증
        validateBikeStatus(bike);

        // 3. 사용자가 유효한 결제 수단이 있는지 확인
        validatePaymentMethod(user);

        // 4. 사용자가 현재 진행중인 대여가 있는지 확인
        validateNoActiveRental(user);

        // 5. 대여 생성
        Rental rental = createRental(user, bike);

        // 6. 자전거 상태 업데이트
        updateBikeStatus(bike);

        // TODO: MQTT 프로토콜을 통한 자전거 잠금 해제 명령 전송

        return RentalResponse.builder()
                .rentalId(rental.getId())
                .bikeId(bike.getId())
                .startTime(rental.getStartTime())
                .message("대여가 성공적으로 완료되었습니다")
                .build();
    }

    private void validateUserStatus(User user) {
        switch (user.getStatus()) {
            case ACTIVE:
                return;
            case LOCKED:
                throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
            case SUSPENDED:
                throw new BusinessException(ErrorCode.ACCOUNT_SUSPENDED);
            case BLOCKED:
                throw new BusinessException(ErrorCode.ACCOUNT_BLOCKED);
            case WITHDRAWN:
                throw new BusinessException(ErrorCode.ACCOUNT_WITHDRAWN);
        }
    }

    private void validateBikeStatus(Bike bike) {
        BikeStatus bikeStatus = bike.getStatus();
        HiBikeStatus hiBikeStatus = bike.getHiBikeStatus();

        // 일반 자전거는 PARKED 상태일 때, HiBike는 AVAILABLE_FOR_RENT이면서 TEMPORARY_LOCKED 상태일 때 대여 가능
        boolean isAvailable = bikeStatus == BikeStatus.PARKED ||
                (hiBikeStatus == HiBikeStatus.AVAILABLE_FOR_RENT &&
                        bikeStatus == BikeStatus.TEMPORARY_LOCKED);

        if (!isAvailable) {
            throw new BusinessException(
                    ErrorCode.BIKE_NOT_AVAILABLE,
                    "현재 대여 불가능한 자전거입니다. 상태: " + bikeStatus.getDescription()
            );
        }

        if (bike.getBatteryLevel() < 20) {
            throw new BusinessException(ErrorCode.LOW_BATTERY);
        }
    }

    private void validatePaymentMethod(User user) {
        boolean hasValidPaymentMethod = paymentMethodRepository
                .existsByUserAndStatus(user, PaymentMethodStatus.ACTIVE);

        if (!hasValidPaymentMethod) {
            throw new BusinessException(ErrorCode.NO_PAYMENT_METHOD);
        }
    }

    private void validateNoActiveRental(User user){

        boolean hasUncompletedRental = rentalRepository
                .existsByUserAndStatusNot(user, RentalStatus.COMPLETED);

        if (!hasUncompletedRental) {
            throw new BusinessException(ErrorCode.ACTIVE_RENTAL_EXISTS);
        }
    }


    private Rental createRental(User user, Bike bike) {
        Rental rental = Rental.builder()
                .user(user)
                .bike(bike)
                .startTime(LocalDateTime.now())
                .status(RentalStatus.IN_PROGRESS)
                .build();

        return rentalRepository.save(rental);
    }

    private void updateBikeStatus(Bike bike){
        bike.setStatus(BikeStatus.IN_USE);
        bikeRepository.save(bike);
    }

}
