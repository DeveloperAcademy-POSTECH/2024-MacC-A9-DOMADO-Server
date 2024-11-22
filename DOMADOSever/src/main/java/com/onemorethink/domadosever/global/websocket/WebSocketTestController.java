package com.onemorethink.domadosever.global.websocket;

import com.onemorethink.domadosever.domain.rental.dto.HiBikeResponse;
import com.onemorethink.domadosever.domain.rental.entity.RentalStatus;
import com.onemorethink.domadosever.global.common.BaseResponse;
import com.onemorethink.domadosever.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/test/websocket")
@RequiredArgsConstructor
public class WebSocketTestController {
    private final RentalWebSocketHandler webSocketHandler;

    // 일반 메시지 테스트
    @PostMapping("/send/{email}")
    @Parameter(name = "email", description = "email", required = true)
    public ResponseEntity<String> sendTestMessage(
            @PathVariable("email") String email,
            @RequestBody WebSocketTestRequest request
    ) {
        try {
            WebSocketMessage<Object> message = WebSocketMessage.success(
                    request.getType(),
                    request.getPayload()
            );

            webSocketHandler.sendMessage(email, message);
            return ResponseEntity.ok("메시지 전송 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("메시지 전송 실패: " + e.getMessage());
        }
    }


    // 에러 상황 테스트
    @PostMapping("/rental/{email}/error")
    public ResponseEntity<String> testErrorScenario(
            @PathVariable String email,
            @RequestParam ErrorCode errorCode
    ) {
        try {
            WebSocketMessage<Void> errorMessage = WebSocketMessage.error(
                    WebSocketMessageType.ERROR,
                    errorCode
            );

            webSocketHandler.sendMessage(email, errorMessage);
            return ResponseEntity.ok("에러 메시지 전송 성공");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("전송 실패: " + e.getMessage());
        }
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class WebSocketTestRequest {
    private String type;
    private Object payload;
}