package com.example.portfolio.price;

import java.math.BigDecimal;

public interface PriceProvider {
    BigDecimal getPrice(String symbol) throws Exception;
}
