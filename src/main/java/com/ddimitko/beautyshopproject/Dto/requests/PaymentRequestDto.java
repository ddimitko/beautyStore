package com.ddimitko.beautyshopproject.Dto.requests;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

    private String sessionToken;
    private Long shopId;
    private Long customerId;
    private String customerName;
    private String customerEmail;
    private Integer serviceId;

}
