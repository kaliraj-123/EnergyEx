package com.energyex.dto;

import java.math.BigDecimal;
import java.util.List;

public class BillEstimationDTO {

    private BigDecimal currentEnergyKwh;
    private BigDecimal defaultTariffRate;
    private BigDecimal fixedCharge;
    private BigDecimal calculatedBaseCost;
    private BigDecimal calculatedSlabCost;
    private BigDecimal projectedMonthlyKwh;
    private BigDecimal projectedMonthlyBillInr;
    private String disclaimer = "Estimated bill. Actual utility bills may include municipal taxes, fuel surcharges, and regulatory duties.";
    private List<SlabDetailDTO> slabBreakdown;

    public BillEstimationDTO() {}

    public static class SlabDetailDTO {
        private String slabName;
        private BigDecimal unitsInSlab;
        private BigDecimal ratePerKwh;
        private BigDecimal amountInr;

        public SlabDetailDTO() {}

        public SlabDetailDTO(String slabName, BigDecimal unitsInSlab, BigDecimal ratePerKwh, BigDecimal amountInr) {
            this.slabName = slabName;
            this.unitsInSlab = unitsInSlab;
            this.ratePerKwh = ratePerKwh;
            this.amountInr = amountInr;
        }

        public String getSlabName() { return slabName; }
        public void setSlabName(String slabName) { this.slabName = slabName; }

        public BigDecimal getUnitsInSlab() { return unitsInSlab; }
        public void setUnitsInSlab(BigDecimal unitsInSlab) { this.unitsInSlab = unitsInSlab; }

        public BigDecimal getRatePerKwh() { return ratePerKwh; }
        public void setRatePerKwh(BigDecimal ratePerKwh) { this.ratePerKwh = ratePerKwh; }

        public BigDecimal getAmountInr() { return amountInr; }
        public void setAmountInr(BigDecimal amountInr) { this.amountInr = amountInr; }
    }

    public BigDecimal getCurrentEnergyKwh() { return currentEnergyKwh; }
    public void setCurrentEnergyKwh(BigDecimal currentEnergyKwh) { this.currentEnergyKwh = currentEnergyKwh; }

    public BigDecimal getDefaultTariffRate() { return defaultTariffRate; }
    public void setDefaultTariffRate(BigDecimal defaultTariffRate) { this.defaultTariffRate = defaultTariffRate; }

    public BigDecimal getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }

    public BigDecimal getCalculatedBaseCost() { return calculatedBaseCost; }
    public void setCalculatedBaseCost(BigDecimal calculatedBaseCost) { this.calculatedBaseCost = calculatedBaseCost; }

    public BigDecimal getCalculatedSlabCost() { return calculatedSlabCost; }
    public void setCalculatedSlabCost(BigDecimal calculatedSlabCost) { this.calculatedSlabCost = calculatedSlabCost; }

    public BigDecimal getProjectedMonthlyKwh() { return projectedMonthlyKwh; }
    public void setProjectedMonthlyKwh(BigDecimal projectedMonthlyKwh) { this.projectedMonthlyKwh = projectedMonthlyKwh; }

    public BigDecimal getProjectedMonthlyBillInr() { return projectedMonthlyBillInr; }
    public void setProjectedMonthlyBillInr(BigDecimal projectedMonthlyBillInr) { this.projectedMonthlyBillInr = projectedMonthlyBillInr; }

    public String getDisclaimer() { return disclaimer; }
    public void setDisclaimer(String disclaimer) { this.disclaimer = disclaimer; }

    public List<SlabDetailDTO> getSlabBreakdown() { return slabBreakdown; }
    public void setSlabBreakdown(List<SlabDetailDTO> slabBreakdown) { this.slabBreakdown = slabBreakdown; }
}

