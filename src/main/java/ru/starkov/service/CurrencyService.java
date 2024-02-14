package ru.starkov.service;

import ru.starkov.model.Currency;
import ru.starkov.repository.dao.CurrencyDao;
import ru.starkov.repository.dao.impl.CurrencyDaoImpl;
import ru.starkov.servlet.CurrencyServlet;

import java.util.List;
import java.util.Optional;

public class CurrencyService {
    private final CurrencyDao currencyDao;

    public CurrencyService(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public List<Currency> findAll() {
        return currencyDao.findAll();
    }

    public Optional<Currency> findByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code cannot be null or blank");
        }
        return currencyDao.findByCode(code);
    }

    public Currency save(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        if (currencyDao.exists(currency.getCode())) {
            throw new IllegalArgumentException("Currency already exist");
        }
        return currencyDao.save(currency);
    }

    public void update(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        currencyDao.update(currency);
    }
}
