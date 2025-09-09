package com.example.portfolio.config;

import com.example.portfolio.model.Stock;
import com.example.portfolio.repository.StockRepository;
import com.example.portfolio.service.PriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PriceScheduler {

    private final StockRepository repo;
    private final PriceService priceService;

    @Scheduled(fixedRate = 10_000) // 10 sn
    @Transactional
    public void updatePrices() {
        List<Stock> all = repo.findAll();
        for (Stock s : all) {
            s.setCurrentPrice(
                    priceService.fetchOrSimulate(s.getSymbol(), s.getCurrentPrice())
            );
        }
        repo.saveAll(all);
    }
}
