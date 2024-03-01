package ru.starkov.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * This class represents an exchange rate model. It contains the ID, links to the base and target
 * currencies, and the rate.
 *
 * @see Currency
 */
@Data
@Builder
public final class ExchangeRate {

  private Integer id;
  private Currency baseCurrency;
  private Currency targetCurrency;
  private BigDecimal rate;
}
