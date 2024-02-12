package ru.starkov.repository.dao;

import ru.starkov.model.Currency;

public interface CurrencyDao {
    void save(Currency currency);

    void findAll();

    void findByCode(String code);

    void findByName(String fullName);

    void update(Currency currency);

    boolean exists(String code);


}
