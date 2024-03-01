package ru.starkov.dao;

import java.util.List;
import java.util.Optional;
import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;

/**
 * An interface for accessing and managing exchange rate entities in the data store. Extends the
 * {@link CrudRepository} interface for basic CRUD operations.
 *
 * @see ExchangeRate
 * @see CrudRepository
 */
public interface ExchangeRateDao extends CrudRepository<ExchangeRate> {

  Optional<ExchangeRate> findByCurrencies(Currency baseCurrency, Currency targetCurrency);

  Optional<ExchangeRate> findByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode);

  List<ExchangeRate> findAllByBaseCurrencyCode(String baseCurrencyCode);

  List<ExchangeRate> findAllByTargetCurrencyCode(String targetCurrencyCode);

}