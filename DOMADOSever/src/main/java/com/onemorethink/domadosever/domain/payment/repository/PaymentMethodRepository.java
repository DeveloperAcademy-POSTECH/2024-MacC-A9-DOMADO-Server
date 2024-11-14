package com.onemorethink.domadosever.domain.payment.repository;

import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethod;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.PaymentMethodStatus;
import com.onemorethink.domadosever.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long> {

    // 사용자가 활성화된 결제 수단이 하나라도 있는지 확인
    boolean existsByUserAndStatus(User user, PaymentMethodStatus status);

    List<PaymentMethod> findByUser_Id(Long userId);

    Optional<PaymentMethod> findByIdAndUser_Email(Long id, String email);
}
