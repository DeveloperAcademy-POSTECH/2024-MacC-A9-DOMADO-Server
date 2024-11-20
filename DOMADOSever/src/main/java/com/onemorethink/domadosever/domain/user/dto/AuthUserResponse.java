package com.onemorethink.domadosever.domain.user.dto;

import com.onemorethink.domadosever.domain.user.entity.Role;
import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.entity.UserStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class AuthUserResponse {
    private Long id;
    private String email;
    private String name;
    private String phone;
    private UserStatus status;
    private List<String> roles;
    private boolean hasRegisteredPayments;
    private Long currentRentalId;  // 현재 대여 정보는 ID만 제공
    private int stampCount;        // 스탬프는 개수만 제공
    private int couponCount;       // 쿠폰도 개수만 제공
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AuthUserResponse from(User user) {
        return AuthUserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toList()))
                .hasRegisteredPayments(user.isHasRegisteredPayments())
                .currentRentalId(user.getCurrentRental() != null ?
                        user.getCurrentRental().getId() : null)
                .stampCount(user.getStamps().size())
                .couponCount(user.getCoupons().size())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}