package com.onemorethink.domadosever.domain.payment.entity.paymentMethod;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class BinInfo {
    private final String issuer;
    private final CardBrand brand;
    private final CardType type;
    private final boolean isPersonal;
    private final String note;

    public BinInfo(String issuer, CardBrand brand, CardType type, boolean isPersonal, String note) {
        this.issuer = issuer;
        this.brand = brand;
        this.type = type;
        this.isPersonal = isPersonal;
        this.note = note;
    }

    public boolean isValid() {
        return note == null || note.isEmpty() ||
                (!note.contains("삭제") && !note.contains("변경"));
    }
}