package com.example.portfolio.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record StockCreateRequest(
        @NotBlank String symbol,
        @Min(1) int quantity,
        @DecimalMin(value = "0.0", inclusive = false) BigDecimal buyPrice

) { }
