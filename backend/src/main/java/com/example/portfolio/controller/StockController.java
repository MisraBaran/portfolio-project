package com.example.portfolio.controller;

import com.example.portfolio.dto.StockCreateRequest;
import com.example.portfolio.model.Stock;
import com.example.portfolio.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class StockController {

    private final StockService stockService;

    @GetMapping
    public List<Stock> list() {
        return stockService.list();
    }

    @PostMapping
    public ResponseEntity<Stock> create(@Valid @RequestBody StockCreateRequest req) {
        return ResponseEntity.ok(stockService.create(req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        stockService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<Stock> updatePrice(@PathVariable Long id, @RequestBody UpdatePriceRequest body) {
        if (body == null || body.price == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price required");
        }
        return ResponseEntity.ok(stockService.updatePrice(id, body.price));
    }

    public static class UpdatePriceRequest {
        public BigDecimal price;
    }
}
