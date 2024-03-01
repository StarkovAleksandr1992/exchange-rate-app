package ru.starkov.dao;

import java.util.Optional;
import ru.starkov.model.Currency;

/**
 * An interface for accessing and managing currency entities in the data store.
 * Extends the {@link CrudRepository} interface for basic CRUD operations.
 *
 * @see Currency
 * @see CrudRepository
 */
public interface CurrencyDao extends CrudRepository<Currency> {

  Optional<Currency> findByCode(String code);
}