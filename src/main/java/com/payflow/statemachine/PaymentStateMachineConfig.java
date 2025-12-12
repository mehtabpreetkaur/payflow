package com.payflow.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import java.util.EnumSet;

@Slf4j
@Configuration
@EnableStateMachineFactory
public class PaymentStateMachineConfig extends StateMachineConfigurerAdapter<PaymentState, PaymentEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentState, PaymentEvent> states) throws Exception {
        states
                .withStates()
                .initial(PaymentState.PENDING)
                .states(EnumSet.allOf(PaymentState.class))
                .end(PaymentState.FAILED)      // FAILED is terminal - can't recover
                .end(PaymentState.REFUNDED);   // REFUNDED is terminal - process complete
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentState, PaymentEvent> transitions) throws Exception {
        transitions
                // PENDING -> PROCESSING
                .withExternal()
                .source(PaymentState.PENDING)
                .target(PaymentState.PROCESSING)
                .event(PaymentEvent.PROCESS)
                .and()

                // PROCESSING -> COMPLETED
                .withExternal()
                .source(PaymentState.PROCESSING)
                .target(PaymentState.COMPLETED)
                .event(PaymentEvent.COMPLETE)
                .and()

                // PROCESSING -> FAILED
                .withExternal()
                .source(PaymentState.PROCESSING)
                .target(PaymentState.FAILED)
                .event(PaymentEvent.FAIL)
                .and()

                // COMPLETED -> REFUNDED (full refund directly)
                .withExternal()
                .source(PaymentState.COMPLETED)
                .target(PaymentState.REFUNDED)
                .event(PaymentEvent.REFUND)
                .and()

                // COMPLETED -> PARTIALLY_REFUNDED (first partial refund)
                .withExternal()
                .source(PaymentState.COMPLETED)
                .target(PaymentState.PARTIALLY_REFUNDED)
                .event(PaymentEvent.PARTIAL_REFUND)
                .and()

                // PARTIALLY_REFUNDED -> PARTIALLY_REFUNDED (multiple partial refunds)
                .withInternal()
                .source(PaymentState.PARTIALLY_REFUNDED)
                .event(PaymentEvent.PARTIAL_REFUND)
                .and()

                // PARTIALLY_REFUNDED -> REFUNDED (complete the refund)
                .withExternal()
                .source(PaymentState.PARTIALLY_REFUNDED)
                .target(PaymentState.REFUNDED)
                .event(PaymentEvent.REFUND);
    }
}