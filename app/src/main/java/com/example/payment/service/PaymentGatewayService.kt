package com.example.payment.service

import com.example.payment.model.PaymentGatewayOrderSession
import com.example.payment.model.PaymentOrderRequest
import com.example.payment.model.PaymentVerificationRequest
import com.example.payment.model.PaymentVerificationResult

/**
 * Provider-independent payment service contract.
 *
 * Security Requirements:
 * 1. Architecture is agnostic to specific vendors (UPI / Cards / Net Banking / COD via Razorpay, Cashfree, PhonePe, etc.)
 * 2. Mobile client NEVER possesses gateway merchant secret keys or initiates client-authoritative writes.
 * 3. All transaction verifications must be validated by trusted server-side HMAC signature verification or webhooks.
 * 4. Idempotency keys prevent duplicate order creation or double charging.
 */
interface PaymentGatewayService {

    /**
     * Creates an order session on the payment gateway through trusted backend.
     * Uses idempotency key to prevent creating multiple payment sessions for the same checkout.
     */
    suspend fun createGatewayOrderSession(request: PaymentOrderRequest): Result<PaymentGatewayOrderSession>

    /**
     * Submits payment response payload to trusted backend for cryptographic HMAC signature verification.
     * Never trusts client-only claims of success.
     */
    suspend fun verifyPaymentOnBackend(request: PaymentVerificationRequest): Result<PaymentVerificationResult>

    /**
     * Queries the latest trusted server-side payment & bank settlement status.
     */
    suspend fun fetchPaymentStatus(orderId: String): Result<PaymentVerificationResult>
}
