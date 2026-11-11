package com.energyex.service;

import com.energyex.dto.BillEstimationDTO;
import com.energyex.entity.Tariff;
import com.energyex.repository.TariffRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class EnergyCalculationService {

    private final TariffRepository tariffRepository;

    public EnergyCalculationService(TariffRepository tariffRepository) {
        this.tariffRepository = tariffRepository;
    }

    public Tariff getActiveTariff() {
        return tariffRepository.findFirstByIsActiveTrue()
                .orElseGet(() -> {
                    Tariff defaultTariff = new Tariff();
                    defaultTariff.setRatePerKwh(BigDecimal.valueOf(7.50));
                    defaultTariff.setName("Default LT Domestic");
                    defaultTariff.setCurrency("₹");
                    return defaultTariff;
                });
    }

    /**
     * Calculates energy in kWh: Energy (kWh) = Power (W) * Time (hours) / 1000
     */
    public BigDecimal calculateEnergyIncrementKwh(BigDecimal powerWatts, double durationSeconds) {
        if (powerWatts == null || powerWatts.compareTo(BigDecimal.ZERO) <= 0 || durationSeconds <= 0) {
            return BigDecimal.ZERO;
        }
        double hours = durationSeconds / 3600.0;
        double kwh = (powerWatts.doubleValue() * hours) / 1000.0;
        return BigDecimal.valueOf(kwh).setScale(6, RoundingMode.HALF_UP);
    }

    /**
     * Calculates flat cost: Cost (₹) = Energy (kWh) * Tariff rate (₹/kWh)
     */
    public BigDecimal calculateCost(BigDecimal energyKwh, BigDecimal ratePerKwh) {
        if (energyKwh == null || ratePerKwh == null) {
            return BigDecimal.ZERO;
        }
        return energyKwh.multiply(ratePerKwh).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Produces a full detailed bill estimation with tariff slabs and end-of-month projection
     */
    public BillEstimationDTO calculateDetailedBill(BigDecimal totalKwh) {
        Tariff tariff = getActiveTariff();
        BigDecimal kwh = (totalKwh != null) ? totalKwh : BigDecimal.ZERO;

        BillEstimationDTO dto = new BillEstimationDTO();
        dto.setCurrentEnergyKwh(kwh.setScale(2, RoundingMode.HALF_UP));
        dto.setDefaultTariffRate(tariff.getRatePerKwh());
        dto.setFixedCharge(tariff.getFixedCharge());

        // Simple flat base cost
        BigDecimal flatCost = kwh.multiply(tariff.getRatePerKwh()).setScale(2, RoundingMode.HALF_UP);
        dto.setCalculatedBaseCost(flatCost);

        // Slab-based calculation
        List<BillEstimationDTO.SlabDetailDTO> slabDetails = new ArrayList<>();
        BigDecimal remainingUnits = kwh;
        BigDecimal totalSlabCost = BigDecimal.ZERO;

        // Slab 1: 0 - Tier 1 (e.g. 100 kWh)
        BigDecimal tier1Limit = tariff.getSlabTier1Limit();
        BigDecimal unitsSlab1 = remainingUnits.min(tier1Limit);
        if (unitsSlab1.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costSlab1 = unitsSlab1.multiply(tariff.getSlabTier1Rate()).setScale(2, RoundingMode.HALF_UP);
            totalSlabCost = totalSlabCost.add(costSlab1);
            slabDetails.add(new BillEstimationDTO.SlabDetailDTO("Slab 1 (0-" + tier1Limit + " kWh)", unitsSlab1, tariff.getSlabTier1Rate(), costSlab1));
            remainingUnits = remainingUnits.subtract(unitsSlab1);
        } else {
            slabDetails.add(new BillEstimationDTO.SlabDetailDTO("Slab 1 (0-" + tier1Limit + " kWh)", BigDecimal.ZERO, tariff.getSlabTier1Rate(), BigDecimal.ZERO));
        }

        // Slab 2: Tier 1 to Tier 2 (e.g. 101 - 300 kWh)
        BigDecimal tier2Span = tariff.getSlabTier2Limit().subtract(tier1Limit);
        BigDecimal unitsSlab2 = remainingUnits.min(tier2Span);
        if (unitsSlab2.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costSlab2 = unitsSlab2.multiply(tariff.getSlabTier2Rate()).setScale(2, RoundingMode.HALF_UP);
            totalSlabCost = totalSlabCost.add(costSlab2);
            slabDetails.add(new BillEstimationDTO.SlabDetailDTO("Slab 2 (" + tier1Limit + "-" + tariff.getSlabTier2Limit() + " kWh)", unitsSlab2, tariff.getSlabTier2Rate(), costSlab2));
            remainingUnits = remainingUnits.subtract(unitsSlab2);
        } else {
            slabDetails.add(new BillEstimationDTO.SlabDetailDTO("Slab 2 (" + tier1Limit + "-" + tariff.getSlabTier2Limit() + " kWh)", BigDecimal.ZERO, tariff.getSlabTier2Rate(), BigDecimal.ZERO));
        }

        // Slab 3: Above Tier 2 (> 300 kWh)
        if (remainingUnits.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costSlab3 = remainingUnits.multiply(tariff.getSlabTier3Rate()).setScale(2, RoundingMode.HALF_UP);
            totalSlabCost = totalSlabCost.add(costSlab3);
            slabDetails.add(new BillEstimationDTO.SlabDetailDTO("Slab 3 (Above " + tariff.getSlabTier2Limit() + " kWh)", remainingUnits, tariff.getSlabTier3Rate(), costSlab3));
        }

        BigDecimal totalBillWithFixed = totalSlabCost.add(tariff.getFixedCharge()).setScale(2, RoundingMode.HALF_UP);
        dto.setCalculatedSlabCost(totalBillWithFixed);
        dto.setSlabBreakdown(slabDetails);

        // Projected monthly consumption (scale based on current day of month)
        LocalDate today = LocalDate.now();
        int dayOfMonth = Math.max(today.getDayOfMonth(), 1);
        int totalDaysInMonth = today.lengthOfMonth();
        double dailyAvgKwh = kwh.doubleValue() / dayOfMonth;
        double projectedMonthKwh = dailyAvgKwh * totalDaysInMonth;

        dto.setProjectedMonthlyKwh(BigDecimal.valueOf(projectedMonthKwh).setScale(2, RoundingMode.HALF_UP));
        BigDecimal projectedBill = BigDecimal.valueOf(projectedMonthKwh).multiply(tariff.getRatePerKwh())
                .add(tariff.getFixedCharge()).setScale(2, RoundingMode.HALF_UP);
        dto.setProjectedMonthlyBillInr(projectedBill);

        return dto;
    }
}

