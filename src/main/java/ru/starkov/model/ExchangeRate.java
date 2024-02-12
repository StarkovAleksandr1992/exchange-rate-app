package ru.starkov.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRate {
    private Integer id;
    private Integer baseCurrencyId;
    private Integer targetCurrencyId;
    private BigDecimal rate;
}
