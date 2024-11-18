package com.onemorethink.domadosever.domain.rental.service;


import com.onemorethink.domadosever.domain.bike.entity.Bike;
import com.onemorethink.domadosever.domain.bike.entity.BikeStatus;
import com.onemorethink.domadosever.domain.bike.entity.HiBikeStatus;
import com.onemorethink.domadosever.domain.bike.repository.BikeRepository;
import com.onemorethink.domadosever.domain.coupon.entity.Stamp;
import com.onemorethink.domadosever.domain.coupon.service.StampService;
import com.onemorethink.domadosever.domain.payment.entity.Payment;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethodStatus;
import com.onemorethink.domadosever.domain.payment.repository.PaymentMethodRepository;
import com.onemorethink.domadosever.domain.payment.service.PaymentService;
import com.onemorethink.domadosever.domain.rental.dto.*;
import com.onemorethink.domadosever.domain.rental.entity.Rental;
import com.onemorethink.domadosever.domain.rental.entity.RentalStatus;
import com.onemorethink.domadosever.domain.rental.repository.RentalRepository;
import com.onemorethink.domadosever.domain.station.entity.Station;
import com.onemorethink.domadosever.domain.station.repository.StationRepository;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.repository.UserRepository;
import com.onemorethink.domadosever.global.error.ErrorCode;
import com.onemorethink.domadosever.global.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RentalService {
    private final BikeRepository bikeRepository;
    private final UserRepository userRepository;
    private final RentalRepository rentalRepository;
    private final StationRepository stationRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentService paymentService;
    private final StampService stampService;
    // private final MQTTService mqttService;

    // 대여 관련 메서드들...
    public RentalResponse rentBike(String email, String qrCode, boolean useCoupon) {
        // 1-1. 사용자 조회 및 검증
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateUserStatus(user);

        // 1-2. 자전거 조회 및 검증
        Bike bike = bikeRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new BusinessException(ErrorCode.BIKE_NOT_FOUND));
        validateBikeStatus(bike);

        // 2. 결제수단 및 활성 대여 검증
        validatePaymentMethod(user);
        validateNoActiveRental(user);

        // 3. HiBike 자전거인 경우 이전 사용자 반납 처리
        if (bike.getHiBikeStatus() == HiBikeStatus.AVAILABLE_FOR_RENT) {
            handleHiBikeTransfer(bike);
        }

        // 4. 대여 생성 및 상태 업데이트
        Rental rental = createRental(user, bike, useCoupon);
        updateBikeStatus(bike);

        // TODO: MQTT 프로토콜을 통한 자전거 잠금 해제 명령 전송

        return RentalResponse.builder()
                .rentalId(rental.getId())
                .bikeId(bike.getId())
                .startTime(rental.getStartTime())
                .bikeStatus(bike.getStatus())
                .hiBikeStatus(bike.getHiBikeStatus())
                .message("대여가 성공적으로 완료되었습니다")
                .build();
    }

    // dock 에 자전거가 주차된이후 사용자에게 dock 정차 메시지가 뜨면 해당 메시지를 받고 반납하기 버튼을 누르면
    public RentalReturnResponse returnBike(String email, Integer rentalId, RentalReturnRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RENTAL_NOT_FOUND));

        validateRentalOwnership(rental, email);
        validateReturnAvailable(rental);

        return returnBike(rental, false, request);
    }

    // HiBike 대여시 이전 사용자가 대여 반납 처리
    private void handleHiBikeTransfer(Bike bike) {
        Rental previousRental = rentalRepository.findByBikeAndStatus(bike, RentalStatus.IN_PROGRESS)
                .orElseThrow(() -> new BusinessException(ErrorCode.RENTAL_NOT_FOUND));

        returnBike(previousRental, true);
        bike.setHiBikeStatus(HiBikeStatus.TRANSFERRED);
        bikeRepository.save(bike);
    }

    // HiBike 대여시, 자전거 반납 절차
    private RentalReturnResponse returnBike(Rental rental, boolean isHiBikeTransfer) {
        return returnBike(rental, isHiBikeTransfer, null);
    }

    private RentalReturnResponse returnBike(Rental rental, boolean isHiBikeTransfer, RentalReturnRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Bike bike = rental.getBike();
        StampIssuanceInfo stampInfo = null;

        try {
            // 1. 일시정지 시간 계산 및 업데이트
            if (rental.getLastPauseStartTime() != null) {
                updatePauseTime(rental, now);
            }

            // 2. 대여 정보 업데이트
            rental.setEndTime(now);
            rental.setUsageMinutes(calculateTotalMinutes(rental));
            rental.setStatus(RentalStatus.COMPLETED);

            // 3. 자전거 상태 및 위치 업데이트
            if (!isHiBikeTransfer) {
                validateReturnLocation(bike, request.getStationId());
                updateBikeForNormalReturn(bike, request);
            }

            // 4. 결제 처리
            Payment payment = paymentService.processRentalPayment(rental);

            // 5.일반반납 &  HiBike 이용 후 반납시 스탬프 발급
            if (!isHiBikeTransfer && bike.getHiBikeStatus() == HiBikeStatus.TRANSFERRED) {
                stampInfo = issueStampForHiBikeUse(rental);
            }

            return createReturnResponse(rental, payment, stampInfo);

        } catch (Exception e) {
            log.error("자전거 반납 처리 중 오류 발생. rentalId: {}, error: {}", rental.getId(), e.getMessage());
            throw new BusinessException(ErrorCode.RETURN_PROCESSING_FAILED, e.getMessage());
        }
    }

    // 일시 잠금
    public RentalPauseResponse pauseBike(String userEmail, Integer rentalId, RentalPauseRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RENTAL_NOT_FOUND));

        validateRentalOwnership(rental, userEmail);
        validatePauseAvailable(rental);

        // TODO: MQTT로 잠금 명령 전송
        // sendLockCommand(rental.getBike());

        Bike bike = rental.getBike();
        updateBikeLocation(bike, request.getLatitude(), request.getLongitude());
        bike.setStatus(BikeStatus.TEMPORARY_LOCKED);
        bikeRepository.save(bike);

        rental.setLastPauseStartTime(LocalDateTime.now());
        rentalRepository.save(rental);

        return RentalPauseResponse.builder()
                .rentalId(rental.getId())
                .bikeStatus(bike.getStatus())
                .pauseTime(LocalDateTime.now())
                .totalPauseMinutes(rental.getPauseMinutes())
                .message("자전거가 성공적으로 일시 정지되었습니다.")
                .build();
    }

    // 일시 잠금 해제
    public RentalResumeResponse resumeBike(String userEmail, Integer rentalId, RentalResumeRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RENTAL_NOT_FOUND));

        validateRentalOwnership(rental, userEmail);
        validateResumeAvailable(rental);

        LocalDateTime now = LocalDateTime.now();
        int pauseDuration = calculatePauseDuration(rental, now);
        updatePauseTime(rental, now);

        // TODO: MQTT로 잠금 해제 명령 전송
        // sendUnlockCommand(rental.getBike());

        Bike bike = rental.getBike();
        updateBikeLocation(bike, request.getLatitude(), request.getLongitude());
        bike.setStatus(BikeStatus.IN_USE);
        bikeRepository.save(bike);

        return RentalResumeResponse.builder()
                .rentalId(rental.getId())
                .bikeStatus(bike.getStatus())
                .resumeTime(now)
                .pauseMinutes(pauseDuration)
                .message("자전거 잠금이 해제되었습니다.")
                .build();
    }

    //  Dock에서 반납인 경우 , 스테이션 정보 확인하고 자전거 PARKED 상태로
    private void updateBikeForNormalReturn(Bike bike, RentalReturnRequest request) {
        Station station = stationRepository.findById(request.getStationId())
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));

        updateBikeLocation(bike, request.getLatitude(), request.getLongitude());
        bike.setStatus(BikeStatus.PARKED);
        bike.setCurrentStation(station);
        bike.setCurrentDockId(request.getDockId());
        bikeRepository.save(bike);
    }


    private StampIssuanceInfo issueStampForHiBikeUse(Rental rental) {
        Stamp stamp = stampService.createStamp(rental.getUser(), rental);
        int unusedStamps = stampService.countUnusedStamps(rental.getUser());
        boolean couponIssued = unusedStamps % 5 == 0;
        Long couponId = couponIssued ? stampService.getLastIssuedCouponId(rental.getUser()) : null;

        return StampIssuanceInfo.builder()
                .isIssued(true)
                .stampId(stamp.getId())
                .totalUnusedStamps(unusedStamps)
                .couponIssued(couponIssued)
                .issuedCouponId(couponId)
                .build();
    }

    // 검증 관련 Helper 메서드들
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

    private void validateNoActiveRental(User user) {
        boolean hasUncompletedRental = rentalRepository
                .existsByUserAndStatusNot(user, RentalStatus.COMPLETED);

        if (hasUncompletedRental) {
            throw new BusinessException(ErrorCode.ACTIVE_RENTAL_EXISTS);
        }
    }

    private void validateRentalOwnership(Rental rental, String userEmail) {
        if (!rental.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException(ErrorCode.RENTAL_NOT_OWNED);
        }
    }

    private void validateReturnAvailable(Rental rental) {
        if (rental.getStatus() != RentalStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.RENTAL_NOT_IN_PROGRESS);
        }
    }

    private void validatePauseAvailable(Rental rental) {
        if (rental.getStatus() != RentalStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.RENTAL_NOT_IN_PROGRESS);
        }

        if (rental.getBike().getStatus() == BikeStatus.TEMPORARY_LOCKED) {
            throw new BusinessException(ErrorCode.BIKE_ALREADY_LOCKED);
        }
    }

    private void validateResumeAvailable(Rental rental) {
        if (rental.getStatus() != RentalStatus.IN_PROGRESS) {
            throw new BusinessException(ErrorCode.RENTAL_NOT_IN_PROGRESS);
        }

        if (rental.getBike().getStatus() != BikeStatus.TEMPORARY_LOCKED) {
            throw new BusinessException(ErrorCode.BIKE_NOT_LOCKED);
        }

        if (rental.getLastPauseStartTime() == null) {
            throw new BusinessException(ErrorCode.BIKE_NOT_IN_PAUSE);
        }
    }

    // Dock에서 반납인 경우 Homehub인지 확인
    private void validateReturnLocation(Bike bike, Long stationId) {
        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STATION_NOT_FOUND));

        if (!station.getHub().getId().equals(bike.getHomeHub().getId())) {
            throw new BusinessException(
                    ErrorCode.INVALID_RETURN_HUB,
                    String.format("이 자전거는 %s 허브에서만 반납이 가능합니다.",
                            bike.getHomeHub().getName())
            );
        }
    }

    // 생성 관련 Helper 메서드들
    private Rental createRental(User user, Bike bike, boolean useCoupon) {
        Rental rental = Rental.builder()
                .user(user)
                .bike(bike)
                .startTime(LocalDateTime.now())
                .status(RentalStatus.IN_PROGRESS)
                .couponApplied(useCoupon)
                .build();

        return rentalRepository.save(rental);
    }

    // 상태 업데이트 관련 Helper 메서드들
    private void updateBikeStatus(Bike bike) {
        bike.setStatus(BikeStatus.IN_USE);
        bike.setCurrentDockId(null);
        bikeRepository.save(bike);
    }

    private void updateBikeLocation(Bike bike, Double latitude, Double longitude) {
        bike.setCurrentLatitude(latitude);
        bike.setCurrentLongitude(longitude);
    }

    // 시간 계산 관련 Helper 메서드들
    private int calculateTotalMinutes(Rental rental) {
        LocalDateTime endTime = rental.getEndTime() != null ?
                rental.getEndTime() : LocalDateTime.now();

        return (int) Duration.between(rental.getStartTime(), endTime).toMinutes();
    }

    private void updatePauseTime(Rental rental, LocalDateTime endTime) {
        LocalDateTime startTime = rental.getLastPauseStartTime();
        if (startTime != null) {
            int currentPauseMinutes = rental.getPauseMinutes() != null ? rental.getPauseMinutes() : 0;
            int additionalMinutes = (int) Duration.between(startTime, endTime).toMinutes();
            rental.setPauseMinutes(currentPauseMinutes + additionalMinutes);
            rental.setLastPauseStartTime(null);
        }
    }

    private int calculatePauseDuration(Rental rental, LocalDateTime endTime) {
        LocalDateTime startTime = rental.getLastPauseStartTime();
        if (startTime == null) {
            return 0;
        }
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    // Response 생성 관련 Helper 메서드들
    private RentalReturnResponse createReturnResponse(Rental rental, Payment payment,
                                                      StampIssuanceInfo stampInfo) {
        Bike bike = rental.getBike();

        return RentalReturnResponse.builder()
                .rentalId(rental.getId())
                .bikeId(bike.getId())
                .endTime(rental.getEndTime())
                .usageMinutes(rental.getUsageMinutes())
                .pauseMinutes(rental.getPauseMinutes())
                .paymentAmount(payment.getAmount())
                .bikeStatus(bike.getStatus())
                .hiBikeStatus(bike.getHiBikeStatus())
                .stationId(bike.getCurrentStation() != null ? bike.getCurrentStation().getId() : null)
                .stampInfo(stampInfo)
                .message(createReturnMessage(rental, stampInfo))
                .build();
    }

    private String createReturnMessage(Rental rental, StampIssuanceInfo stampInfo) {
        StringBuilder message = new StringBuilder("자전거가 성공적으로 반납되었습니다.");

        if (stampInfo != null && stampInfo.isIssued()) {
            message.append("\nHiBike 이용에 대한 스탬프가 발급되었습니다.");

            if (stampInfo.isCouponIssued()) {
                message.append("\n축하합니다! 스탬프 5개가 모여 무료이용 쿠폰이 발급되었습니다.");
            }
        }

        return message.toString();
    }

}