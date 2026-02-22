package com.hospital.hms.payment.dto;

/**
 * Response for create-order. Client uses orderId for Razorpay/Stripe checkout.
 */
public class PaymentOrderResponseDto {

    private String orderId;
    private String keyId;
    private Long amountPaise;
    private String currency;

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getKeyId() { return keyId; }
    public void setKeyId(String keyId) { this.keyId = keyId; }
    public Long getAmountPaise() { return amountPaise; }
    public void setAmountPaise(Long amountPaise) { this.amountPaise = amountPaise; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
