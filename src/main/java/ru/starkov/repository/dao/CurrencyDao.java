package ru.starkov.repository.dao;

import ru.starkov.model.Currency;

import java.util.List;
import java.util.Optional;

public interface CurrencyDao {
    Currency save(Currency currency);

    List<Currency> findAll();

    Optional<Currency> findByCode(String code);

    void update(Currency currency);

    boolean exists(String code);


}
