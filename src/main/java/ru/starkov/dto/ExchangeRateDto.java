package ru.starkov.dto;

import java.math.BigDecimal;

/**
 * The ExchangeRateDto class represents a data transfer object (DTO) for exchange rates between two
 * currencies. It encapsulates information about the base currency, target currency, and the
 * exchange rate value.
 */
public record ExchangeRateDto(
    CurrencyRequestDto baseCurrency,
    CurrencyRequestDto targetCurrency,
    BigDecimal rate
) {

}
