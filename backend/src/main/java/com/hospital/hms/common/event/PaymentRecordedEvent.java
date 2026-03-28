package com.hospital.hms.common.event;

import java.math.BigDecimal;

public class PaymentRecordedEvent extends DomainEvent {

    private final Long billingAccountId;
    private final Long paymentId;
    private final BigDecimal amount;
    private final String paymentMode;

    public PaymentRecordedEvent(String triggeredBy, Long billingAccountId,
                                Long paymentId, BigDecimal amount, String paymentMode) {
        super(triggeredBy);
        this.billingAccountId = billingAccountId;
        this.paymentId = paymentId;
        this.amount = amount;
        this.paymentMode = paymentMode;
    }

    public Long getBillingAccountId() { return billingAccountId; }
    public Long getPaymentId() { return paymentId; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMode() { return paymentMode; }
}
