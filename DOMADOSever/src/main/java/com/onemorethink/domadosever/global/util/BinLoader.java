package com.onemorethink.domadosever.global.util;

import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.BinInfo;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.CardBrand;
import com.onemorethink.domadosever.domain.payment.entity.paymentMethod.CardType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class BinLoader {
    private final Map<String, String> issuerCodeMap = new HashMap<>();
    private final Map<String, BinInfo> binInfoMap = new HashMap<>();

    @PostConstruct
    public void initialize() {
        try {
            initializeIssuerCodes();
            initializeBinInfo();

            // printLoadedBins(); 로드된 BIN 콘솔에 출력
            
        } catch (IOException e) {
            log.error("Failed to initialize BIN loader", e);
            throw new RuntimeException("Failed to initialize BIN loader", e);
        }
    }

    private void initializeIssuerCodes() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("outline.csv").getInputStream(),
                        StandardCharsets.UTF_8
                ))) {

            // 헤더 건너뛰기
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2 && !parts[0].trim().isEmpty() && !parts[1].trim().isEmpty()) {
                    issuerCodeMap.put(parts[0].trim(), parts[1].trim());
                }
            }
        }
        log.info("Loaded {} issuer codes", issuerCodeMap.size());
    }

    private void initializeBinInfo() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        new ClassPathResource("detail.csv").getInputStream(),
                        StandardCharsets.UTF_8
                ))) {

            // 헤더 건너뛰기
            reader.readLine();

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    processBinLine(line);
                } catch (Exception e) {
                    log.error("Failed to process BIN line: {}", line, e);
                }
            }
        }
        log.info("Loaded {} BIN entries", binInfoMap.size());
    }

    private void processBinLine(String line) {
        String[] parts = line.split(",");
        if (parts.length < 8) {
            return;
        }

        String bin = normalizeBin(parts[2].trim());
        String issuerCode = parts[1].trim();
        String issuerName = issuerCodeMap.getOrDefault(issuerCode, issuerCode);
        String personalType = parts[4].trim();
        String brandCode = parts[5].trim();
        String cardTypeCode = parts[6].trim();
        String note = parts.length > 8 ? parts[8].trim() : "";

        CardBrand brand = CardBrand.fromCode(brandCode);
        CardType type = CardType.fromCode(cardTypeCode);
        boolean isPersonal = "개인".equals(personalType);

        BinInfo binInfo = new BinInfo(issuerName, brand, type, isPersonal, note);
        binInfoMap.put(bin, binInfo);

        // BIN 번호가 8자리인 경우 6자리 버전도 저장
        if (bin.length() == 8) {
            String sixDigitBin = bin.substring(0, 6);
            if (!binInfoMap.containsKey(sixDigitBin)) {
                binInfoMap.put(sixDigitBin, binInfo);
            }
        }
    }

    private String normalizeBin(String bin) {
        // 숫자만 추출
        bin = bin.replaceAll("[^0-9]", "");

        // 6자리 미만인 경우 앞에 0 채우기
        while (bin.length() < 6) {
            bin = "0" + bin;
        }

        return bin;
    }

    public BinInfo getBinInfo(String bin) {
        bin = normalizeBin(bin);
        BinInfo info = binInfoMap.get(bin);

        // 6자리로 찾지 못한 경우 8자리로 시도
        if (info == null && bin.length() == 6) {
            for (String key : binInfoMap.keySet()) {
                if (key.length() == 8 && key.startsWith(bin)) {
                    return binInfoMap.get(key);
                }
            }
        }

        return info;
    }

    public void printLoadedBins() {
        log.info("Loaded BINs:");
        binInfoMap.forEach((bin, info) ->
                log.info("BIN: {}, Issuer: {}, Brand: {}, Type: {}",
                        bin, info.getIssuer(), info.getBrand(), info.getType()));
    }
}