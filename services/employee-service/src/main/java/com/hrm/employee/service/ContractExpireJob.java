package com.hrm.employee.service;

import com.hrm.employee.entity.Contract;
import com.hrm.employee.repository.ContractRepository;
import com.hrm.employee.util.constant.ContractStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContractExpireJob {

    private final ContractRepository contractRepository;

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Ho_Chi_Minh")
    public void expireContracts() {

        int updated = contractRepository.bulkExpire(
                ContractStatus.ACTIVE,
                ContractStatus.EXPIRED,
                LocalDate.now()
        );

        log.info("Expired {} contracts", updated);
    }
}
