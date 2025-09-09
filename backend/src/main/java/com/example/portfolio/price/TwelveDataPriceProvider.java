package com.example.portfolio.price;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

@Component
public class TwelveDataPriceProvider implements PriceProvider {
    private final RestClient client;
    private final String apiKey;

    // /quote yanıtındaki alanlar
    public record QuoteResponse(String price, String status, String message) {}

    public TwelveDataPriceProvider(@Value("${twelvedata.apiKey:}") String apiKey) {
        this.apiKey = apiKey;
        this.client = RestClient.builder().baseUrl("https://api.twelvedata.com").build();
    }

    @Override
    public BigDecimal getPrice(String symbol) throws Exception {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("TwelveData API key missing");
        }
        String bistSymbol = symbol.trim().toUpperCase() + ":BIST";

        QuoteResponse resp = client.get()
                .uri(uri -> uri.path("/quote")
                        .queryParam("symbol", bistSymbol)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .body(QuoteResponse.class);

        if (resp == null || resp.price == null) {
            throw new IllegalStateException("Invalid quote response: " + (resp != null ? resp.message : "null"));
        }
        return new BigDecimal(resp.price);
    }
}

