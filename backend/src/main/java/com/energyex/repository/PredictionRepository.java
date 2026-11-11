package com.energyex.repository;

import com.energyex.entity.Prediction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    List<Prediction> findByForecastDateGreaterThanEqualOrderByForecastDateAsc(LocalDate startDate);
    List<Prediction> findTop14ByOrderByForecastDateDesc();
}

