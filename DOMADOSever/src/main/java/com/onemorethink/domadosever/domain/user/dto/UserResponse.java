package com.onemorethink.domadosever.domain.user.dto;

import com.onemorethink.domadosever.domain.user.entity.User;
import com.onemorethink.domadosever.domain.user.entity.UserStatus;
import com.onemorethink.domadosever.domain.user.entity.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 정보 응답")
public class UserResponse {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;
    @Schema(description = "이메일 주소", example = "user@example.com")
    private String email;
    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;
    @Schema(description = "계정 상태",
            example = "ACTIVE",
            allowableValues = {"ACTIVE", "LOCKED", "SUSPENDED", "BLOCKED", "WITHDRAWN"})
    private UserStatus status;
    @Schema(description = "사용자 권한 목록",
            example = "[\"USER\", \"ADMIN\"]")
    private Set<String> roles;
    @Schema(description = "계정 생성일시",
            example = "2024-03-15T09:30:00",
            type = "string",
            format = "date-time")
    private LocalDateTime createdAt;
    @Schema(description = "계정 수정일시",
            example = "2024-03-15T09:30:00",
            type = "string",
            format = "date-time")
    private LocalDateTime updatedAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .status(user.getStatus())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}