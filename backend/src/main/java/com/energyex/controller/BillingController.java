package com.energyex.controller;

import com.energyex.dto.BillEstimationDTO;
import com.energyex.dto.TariffDTO;
import com.energyex.entity.Appliance;
import com.energyex.entity.Tariff;
import com.energyex.repository.ApplianceRepository;
import com.energyex.repository.TariffRepository;
import com.energyex.service.EnergyCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/billing")
@CrossOrigin(origins = "*")
public class BillingController {

    private final EnergyCalculationService calculationService;
    private final ApplianceRepository applianceRepository;
    private final TariffRepository tariffRepository;

    public BillingController(EnergyCalculationService calculationService,
                             ApplianceRepository applianceRepository,
                             TariffRepository tariffRepository) {
        this.calculationService = calculationService;
        this.applianceRepository = applianceRepository;
        this.tariffRepository = tariffRepository;
    }

    @GetMapping("/estimate")
    public ResponseEntity<BillEstimationDTO> getBillEstimate() {
        List<Appliance> appliances = applianceRepository.findAll();
        BigDecimal totalKwh = appliances.stream()
                .map(Appliance::getCumulativeEnergyKwh)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BillEstimationDTO estimate = calculationService.calculateDetailedBill(totalKwh);
        return ResponseEntity.ok(estimate);
    }

    @GetMapping("/tariff")
    public ResponseEntity<Tariff> getActiveTariff() {
        return ResponseEntity.ok(calculationService.getActiveTariff());
    }

    @PutMapping("/tariff")
    @Transactional
    public ResponseEntity<Tariff> updateTariff(@RequestBody TariffDTO dto) {
        Tariff tariff = calculationService.getActiveTariff();
        if (dto.getRatePerKwh() != null) {
            tariff.setRatePerKwh(dto.getRatePerKwh());
        }
        if (dto.getName() != null) {
            tariff.setName(dto.getName());
        }
        if (dto.getFixedCharge() != null) {
            tariff.setFixedCharge(dto.getFixedCharge());
        }
        Tariff updated = tariffRepository.save(tariff);
        return ResponseEntity.ok(updated);
    }
}

