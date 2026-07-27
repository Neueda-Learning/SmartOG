package org.example.payment.service;

import org.example.payment.exception.BusinessException;
import org.example.payment.exception.ErrorCode;
import org.example.payment.model.PaymentStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class PaymentStateMachine {

    private final Map<PaymentStatus, Set<PaymentStatus>> transitions = new EnumMap<>(PaymentStatus.class);

    public PaymentStateMachine() {
        transitions.put(PaymentStatus.CREATED, EnumSet.of(PaymentStatus.VALIDATED, PaymentStatus.FAILED));
        transitions.put(PaymentStatus.VALIDATED, EnumSet.of(PaymentStatus.SENT, PaymentStatus.FAILED));
        transitions.put(PaymentStatus.SENT, EnumSet.of(PaymentStatus.COMPLETED, PaymentStatus.FAILED));
        transitions.put(PaymentStatus.COMPLETED, EnumSet.noneOf(PaymentStatus.class));
        transitions.put(PaymentStatus.FAILED, EnumSet.noneOf(PaymentStatus.class));
    }

    public void assertCanTransition(PaymentStatus from, PaymentStatus to) {
        Set<PaymentStatus> allowed = transitions.getOrDefault(from, EnumSet.noneOf(PaymentStatus.class));
        if (!allowed.contains(to)) {
            throw new BusinessException(
                    ErrorCode.INVALID_STATUS_TRANSITION,
                    "Invalid status transition: " + from + " -> " + to
            );
        }
    }
}

