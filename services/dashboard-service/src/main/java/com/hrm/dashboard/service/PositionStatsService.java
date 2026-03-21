package com.hrm.dashboard.service;

import com.hrm.dashboard.entity.PositionStats;
import com.hrm.dashboard.repository.PositionStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionStatsService {

    private final PositionStatsRepository repository;

    @Transactional
    public PositionStats handlePositionCreated() {

        Optional<PositionStats> optionalStats = repository.findTopByOrderByCreatedAtDesc();

        PositionStats stats;
        if (optionalStats.isPresent()) {
            stats = optionalStats.get();
            stats.setTotalPosition(stats.getTotalPosition() + 1);
            stats.setCreatedAt(LocalDateTime.now());
        } else {
            stats = PositionStats.builder()
                    .totalPosition(1)
                    .createdAt(LocalDateTime.now())
                    .build();
        }

        return repository.save(stats);
    }

    @Transactional
    public PositionStats handlePositionDeleted() {

        Optional<PositionStats> optionalStats = repository.findTopByOrderByCreatedAtDesc();

        if (optionalStats.isEmpty()) return null;

        PositionStats stats = optionalStats.get();

        int current = stats.getTotalPosition() == null ? 0 : stats.getTotalPosition();

        stats.setTotalPosition(Math.max(0, current - 1));
        stats.setCreatedAt(LocalDateTime.now());

        return repository.save(stats);
    }

    // ===================== GET TOTAL =====================
    @Transactional(readOnly = true)
    public int getTotalPositions() {
        return repository.findTopByOrderByCreatedAtDesc()
                .map(PositionStats::getTotalPosition)
                .orElse(0);
    }
}
