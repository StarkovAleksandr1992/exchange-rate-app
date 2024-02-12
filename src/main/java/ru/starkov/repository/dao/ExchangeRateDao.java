package ru.starkov.repository.dao;

import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;

public interface ExchangeRateDao {
    void save(ExchangeRate exchangeRate);

    void findAll();
    void findByCurrencies(Currency baseCurrency, Currency targetCurrency);

    void findByCurrencyCodes(String codes);

    void update(ExchangeRate exchangeRate);

}
