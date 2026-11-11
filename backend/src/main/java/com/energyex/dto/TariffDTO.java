package com.energyex.dto;

import java.math.BigDecimal;

public class TariffDTO {
    private Long id;
    private String name;
    private BigDecimal ratePerKwh;
    private String currency;
    private BigDecimal fixedCharge;

    public TariffDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getRatePerKwh() { return ratePerKwh; }
    public void setRatePerKwh(BigDecimal ratePerKwh) { this.ratePerKwh = ratePerKwh; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }
}

