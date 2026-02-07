package com.lollito.fm.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.lollito.fm.model.PaymentStatus;
import com.lollito.fm.model.PaymentType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SponsorshipPaymentDTO implements Serializable {
    private Long id;
    private PaymentType paymentType;
    private BigDecimal amount;
    private LocalDate dueDate;
    private LocalDate paidDate;
    private PaymentStatus status;
    private String description;
    private String reference;
}
