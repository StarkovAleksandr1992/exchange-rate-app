package ru.starkov.service;

import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;
import ru.starkov.repository.dao.ExchangeRateDao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class ExchangeRateService {

    private final ExchangeRateDao exchangeRateDao;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao) {
        this.exchangeRateDao = exchangeRateDao;
    }

    public List<ExchangeRate> findAll() {
        return exchangeRateDao.findAll();
    }
    public Optional<ExchangeRate> findByCurrencies(Currency baseCurrency, Currency targetCurrency) {
        if (baseCurrency == null || targetCurrency == null) {
            throw new IllegalArgumentException("Base and target currencies cannot be null.");
        }
        return exchangeRateDao.findByCurrencies(baseCurrency, targetCurrency);
    }
    public Optional<ExchangeRate> findByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        if (baseCurrencyCode == null || targetCurrencyCode == null || baseCurrencyCode.isBlank() || targetCurrencyCode.isBlank()) {
            throw new IllegalArgumentException("Base and target currency codes cannot be null or blank.");
        }
        return exchangeRateDao.findByCurrencyCodes(baseCurrencyCode, targetCurrencyCode);
    }

    public void update(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            throw new IllegalArgumentException("ExchangeRate cannot be null.");
        } else if (exchangeRate.getRate().compareTo(BigDecimal.ZERO) < 0 || exchangeRate.getRate().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Exchange rate cannot be zero or negative.");
        }
        exchangeRateDao.update(exchangeRate);
    }

    public ExchangeRate save(ExchangeRate exchangeRate) {
        if (exchangeRate == null) {
            throw new IllegalArgumentException("ExchangeRate cannot be null.");
        } else if (exchangeRate.getRate().compareTo(BigDecimal.ZERO) < 0 || exchangeRate.getRate().compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Exchange rate cannot be zero or negative.");
        }
        return exchangeRateDao.save(exchangeRate);
    }
}
