package com.energyex.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tariffs")
public class Tariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name = "Domestic LT-1 Tariff";

    @Column(name = "rate_per_kwh", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerKwh = BigDecimal.valueOf(7.50);

    @Column(length = 10)
    private String currency = "₹";

    @Column(name = "slab_tier1_limit", precision = 10, scale = 2)
    private BigDecimal slabTier1Limit = BigDecimal.valueOf(100.0);

    @Column(name = "slab_tier1_rate", precision = 10, scale = 2)
    private BigDecimal slabTier1Rate = BigDecimal.valueOf(4.50);

    @Column(name = "slab_tier2_limit", precision = 10, scale = 2)
    private BigDecimal slabTier2Limit = BigDecimal.valueOf(300.0);

    @Column(name = "slab_tier2_rate", precision = 10, scale = 2)
    private BigDecimal slabTier2Rate = BigDecimal.valueOf(7.50);

    @Column(name = "slab_tier3_rate", precision = 10, scale = 2)
    private BigDecimal slabTier3Rate = BigDecimal.valueOf(10.00);

    @Column(name = "fixed_charge", precision = 10, scale = 2)
    private BigDecimal fixedCharge = BigDecimal.valueOf(50.00);

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Tariff() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getRatePerKwh() { return ratePerKwh; }
    public void setRatePerKwh(BigDecimal ratePerKwh) { this.ratePerKwh = ratePerKwh; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getSlabTier1Limit() { return slabTier1Limit; }
    public void setSlabTier1Limit(BigDecimal slabTier1Limit) { this.slabTier1Limit = slabTier1Limit; }

    public BigDecimal getSlabTier1Rate() { return slabTier1Rate; }
    public void setSlabTier1Rate(BigDecimal slabTier1Rate) { this.slabTier1Rate = slabTier1Rate; }

    public BigDecimal getSlabTier2Limit() { return slabTier2Limit; }
    public void setSlabTier2Limit(BigDecimal slabTier2Limit) { this.slabTier2Limit = slabTier2Limit; }

    public BigDecimal getSlabTier2Rate() { return slabTier2Rate; }
    public void setSlabTier2Rate(BigDecimal slabTier2Rate) { this.slabTier2Rate = slabTier2Rate; }

    public BigDecimal getSlabTier3Rate() { return slabTier3Rate; }
    public void setSlabTier3Rate(BigDecimal slabTier3Rate) { this.slabTier3Rate = slabTier3Rate; }

    public BigDecimal getFixedCharge() { return fixedCharge; }
    public void setFixedCharge(BigDecimal fixedCharge) { this.fixedCharge = fixedCharge; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

