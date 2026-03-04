package com.hrm.employee.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

//@Component
//@RequiredArgsConstructor
//@Slf4j
//public class EmployeeEventListener {
//
//    private final LeaveClient leaveClient;
//
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    public void handleEmployeeCreated(EmployeeCreatedEvent event) {
//
//        try {
//            log.info("Calling leave-service to init leave balance for employee: {}",
//                    event.getEmployeeId());
//
//            leaveClient.initLeaveBalance(event.getEmployeeId());
//
//        } catch (Exception e) {
//
//            log.error("Failed to init leave balance for employee {}",
//                    event.getEmployeeId(), e);
//
//        }
//    }
//}
