package ru.starkov.repository.dao;

import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;

import java.util.List;
import java.util.Optional;

public interface ExchangeRateDao {
    void save(ExchangeRate exchangeRate);

    List<ExchangeRate> findAll();

    Optional<ExchangeRate> findByCurrencies(Currency baseCurrency, Currency targetCurrency);

    Optional<ExchangeRate> findByCurrencyCodes(String codes);

    void update(ExchangeRate exchangeRate);

}
