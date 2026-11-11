package com.energyex.repository;

import com.energyex.entity.Appliance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, Long> {
    Optional<Appliance> findByRelayChannel(Integer relayChannel);
    List<Appliance> findAllByOrderByIdAsc();
    List<Appliance> findByIsOnTrue();
}

