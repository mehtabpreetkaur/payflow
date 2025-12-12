package com.payflow.repository;

import com.payflow.entity.Payment;
import com.payflow.statemachine.PaymentState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByMerchantId(Long merchantId);
    List<Payment> findByStatus(PaymentState status);
    List<Payment> findByMerchantIdAndStatus(Long merchantId, PaymentState status);
}