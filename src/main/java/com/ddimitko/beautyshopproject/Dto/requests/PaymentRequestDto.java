package com.ddimitko.beautyshopproject.Dto.requests;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentRequestDto {

    private String method; // "cash" or "card"
    private String paymentMethodId; // Required if method is "card"

}
