package ru.starkov.dto;

import java.math.BigDecimal;

/**
 * The ExchangeRateInfo class represents a data transfer object (DTO) for exchange rate information
 * between two currencies. It encapsulates the currency codes of the base and target currencies,
 * along with the exchange rate value.
 */
public record ExchangeRateInfo(String baseCurrencyCode, String targetCurrencyCode,
                               BigDecimal rate) {

}
