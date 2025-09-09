package com.example.portfolio.service;

import com.example.portfolio.price.PriceProvider;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PriceService {

    private static final Logger log = LoggerFactory.getLogger(PriceService.class);
    private final PriceProvider provider;

    /** Önce API'den dener; hata olursa simüle eder. */
    public BigDecimal fetchOrSimulate(String symbol, BigDecimal current) {
        try {
            BigDecimal p = provider.getPrice(symbol);
            if (p != null && p.compareTo(BigDecimal.ZERO) > 0) {
                log.info("Price from API {} -> {}", symbol, p);
                return p.setScale(2, RoundingMode.HALF_UP);
            }
        } catch (Exception e) {
            log.warn("API failed for {}: {} (using simulation)", symbol, e.getMessage());
        }

        // Fallback: simülasyon (±%1)
        if (current == null || current.compareTo(BigDecimal.ZERO) <= 0) {
            current = BigDecimal.valueOf(100);
        }
        double drift = ThreadLocalRandom.current().nextDouble(-0.01, 0.01);
        return current.multiply(BigDecimal.valueOf(1.0 + drift))
                .setScale(2, RoundingMode.HALF_UP);
    }

    // (İsteğe bağlı) Eski çağrılar için köprü metotlar:
    public BigDecimal nextPrice(BigDecimal current) {
        return fetchOrSimulate("UNKNOWN", current);
    }
    public BigDecimal nextPrice(String symbol, BigDecimal current) {
        return fetchOrSimulate(symbol, current);
    }
}
