package com.energyex.repository;

import com.energyex.entity.EnergyReading;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnergyReadingRepository extends JpaRepository<EnergyReading, Long> {

    List<EnergyReading> findTop50ByOrderByTimestampDesc();

    List<EnergyReading> findByApplianceIdOrderByTimestampDesc(Long applianceId, Pageable pageable);

    List<EnergyReading> findByTimestampBetweenOrderByTimestampAsc(LocalDateTime start, LocalDateTime end);

    @Query("SELECT r FROM EnergyReading r WHERE r.timestamp >= :since ORDER BY r.timestamp ASC")
    List<EnergyReading> findReadingsSince(@Param("since") LocalDateTime since);

    Optional<EnergyReading> findFirstByOrderByTimestampDesc();
}

