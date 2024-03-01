package ru.starkov.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ru.starkov.dao.CurrencyDao;
import ru.starkov.dao.ExchangeRateDao;
import ru.starkov.dao.impl.CurrencyDaoImpl;
import ru.starkov.dao.impl.ExchangeRateDaoImpl;
import ru.starkov.dto.ExchangeRateInfo;
import ru.starkov.exception.CurrencyNotFoundException;
import ru.starkov.exception.DatabaseException;
import ru.starkov.exception.ExchangeRateNotFoundException;
import ru.starkov.model.ExchangeRate;
import ru.starkov.util.ConnectionManager;
import ru.starkov.util.ValidationUtils;

/**
 * The ExchangeRateService class provides services related to exchange rates, such as finding all
 * exchange rates, finding exchange rates by currency codes, updating exchange rates, and saving new
 * exchange rates. It interacts with the ExchangeRateDao and CurrencyDao to perform database
 * operations.
 */
@RequiredArgsConstructor
public final class ExchangeRateService {

  private final ExchangeRateDao exchangeRateDao;
  private final CurrencyDao currencyDao;

  private static final String EXCHANGE_RATES_NOT_FOUND_MSG = "Exchange rates not found";
  private static final String NULL_OR_BLANK_CURRENCY_CODES_MSG =
      "Base and target currency codes cannot be null or blank.";
  private static final String NULL_EXCHANGE_RATE_INFO_MSG = "Exchange rate info cannot be null.";
  private static final String FAILED_TO_UPDATE_CURRENCY_NOT_FOUND_MSG =
      "Failed to update an exchange rate, one or both currencies is not found in database.";
  private static final String FAILED_TO_UPDATE_EXCHANGE_RATE_NOT_FOUND_MSG =
      "Failed to update an exchange rate, it's not found in database.";
  private static final String UNKNOWN_DAO_MSG = "Unknown dao.";
  private static final String FAILED_TO_SAVE_EXCHANGE_RATE_MSG = "Failed to save exchange rate.";

  /**
   * Retrieves all exchange rates from the database.
   *
   * @return a list of all exchange rates
   * @throws ExchangeRateNotFoundException if no exchange rates are found in the database
   */
  public List<ExchangeRate> findAll() throws ExchangeRateNotFoundException {
    List<ExchangeRate> all = exchangeRateDao.findAll();
    if (all.isEmpty()) {
      throw new ExchangeRateNotFoundException(EXCHANGE_RATES_NOT_FOUND_MSG);
    }
    return all;
  }

  /**
   * Retrieves an exchange rate from the database by the given base and target currency codes.
   *
   * @param baseCurrencyCode   the code of the base currency
   * @param targetCurrencyCode the code of the target currency
   * @return the exchange rate corresponding to the given currency codes
   * @throws ExchangeRateNotFoundException if the exchange rate is not found in the database
   * @throws IllegalArgumentException      if either baseCurrencyCode or targetCurrencyCode is null
   *                                       or blank
   */
  public ExchangeRate findByCurrencyCodes(String baseCurrencyCode,
      String targetCurrencyCode) throws ExchangeRateNotFoundException {
    if (ValidationUtils.isNullOrBlank(baseCurrencyCode)
        || ValidationUtils.isNullOrBlank(targetCurrencyCode)) {
      throw new IllegalArgumentException(NULL_OR_BLANK_CURRENCY_CODES_MSG);
    }
    return exchangeRateDao.findByCurrencyCodes(baseCurrencyCode, targetCurrencyCode)
        .orElseThrow(() -> new ExchangeRateNotFoundException(String.format(
            "An exchange rate for the following currency code pair was not found: %s %s",
            baseCurrencyCode,
            targetCurrencyCode)));
  }

  /**
   * Updates an existing exchange rate in the database with the information provided in the
   * ExchangeRateInfo object.
   *
   * @param exchangeRateInfo the ExchangeRateInfo containing the information to update the exchange
   *                         rate
   * @throws DatabaseException             if a database error occurs during the update operation
   * @throws CurrencyNotFoundException     if either the base or target currency is not found in the
   *                                       database
   * @throws ExchangeRateNotFoundException if the exchange rate to be updated is not found in the
   *                                       database
   */
  public void update(ExchangeRateInfo exchangeRateInfo) {
    Objects.requireNonNull(exchangeRateInfo, NULL_EXCHANGE_RATE_INFO_MSG);
    try (var connection = ConnectionManager.getConnection()) {
      checkDaoTypes();
      final CurrencyDaoImpl currencyDaoImpl = (CurrencyDaoImpl) currencyDao;
      final ExchangeRateDaoImpl exchangeRateDaoImpl = (ExchangeRateDaoImpl) exchangeRateDao;
      connection.setAutoCommit(false);
      connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      exchangeRateDaoImpl.setTransactionalConnection(connection);
      currencyDaoImpl.setTransactionalConnection(connection);
      var rate = exchangeRateInfo.rate();
      var baseCurrencyCode = exchangeRateInfo.baseCurrencyCode();
      var targetCurrencyCode = exchangeRateInfo.targetCurrencyCode();
      var baseCurrencyOptional = currencyDaoImpl.findByCode(baseCurrencyCode);
      var targetCurrencyOptional = currencyDaoImpl.findByCode(targetCurrencyCode);
      if (baseCurrencyOptional.isEmpty() || targetCurrencyOptional.isEmpty()) {
        throw new CurrencyNotFoundException(FAILED_TO_UPDATE_CURRENCY_NOT_FOUND_MSG);
      }
      var exchangeRate = exchangeRateDaoImpl.findByCurrencies(baseCurrencyOptional.get(),
              targetCurrencyOptional.get())
          .orElseThrow(() -> new ExchangeRateNotFoundException(
              FAILED_TO_UPDATE_EXCHANGE_RATE_NOT_FOUND_MSG));
      exchangeRate.setRate(rate);
      exchangeRate.setBaseCurrency(baseCurrencyOptional.get());
      exchangeRate.setTargetCurrency(targetCurrencyOptional.get());
      exchangeRateDaoImpl.update(exchangeRate);
      exchangeRateDaoImpl.setTransactionalConnection(null);
      currencyDaoImpl.setTransactionalConnection(null);
      connection.commit();
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_SAVE_EXCHANGE_RATE_MSG, e);
    }

  }

  /**
   * Saves a new exchange rate to the database with the information provided in the ExchangeRateInfo
   * object.
   *
   * @param exchangeRateInfo the ExchangeRateInfo containing the information to save the exchange
   *                         rate
   * @return the saved exchange rate
   * @throws DatabaseException         if a database error occurs during the save operation
   * @throws CurrencyNotFoundException if either the base or target currency is not found in the
   *                                   database
   */
  public ExchangeRate save(ExchangeRateInfo exchangeRateInfo)
      throws DatabaseException, CurrencyNotFoundException {
    Objects.requireNonNull(exchangeRateInfo, NULL_EXCHANGE_RATE_INFO_MSG);
    try (var connection = ConnectionManager.getConnection()) {
      checkDaoTypes();
      final CurrencyDaoImpl currencyDaoImpl = (CurrencyDaoImpl) currencyDao;
      final ExchangeRateDaoImpl exchangeRateDaoImpl = (ExchangeRateDaoImpl) exchangeRateDao;
      connection.setAutoCommit(false);
      connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      exchangeRateDaoImpl.setTransactionalConnection(connection);
      currencyDaoImpl.setTransactionalConnection(connection);
      var rate = exchangeRateInfo.rate();
      var baseCurrencyCode = exchangeRateInfo.baseCurrencyCode();
      var targetCurrencyCode = exchangeRateInfo.targetCurrencyCode();
      var baseCurrencyOptional = currencyDaoImpl.findByCode(baseCurrencyCode);
      var targetCurrencyOptional = currencyDaoImpl.findByCode(targetCurrencyCode);
      if (baseCurrencyOptional.isEmpty() || targetCurrencyOptional.isEmpty()) {
        throw new CurrencyNotFoundException(FAILED_TO_UPDATE_CURRENCY_NOT_FOUND_MSG);
      }
      var exchangeRate = ExchangeRate.builder()
          .rate(rate)
          .baseCurrency(baseCurrencyOptional.get())
          .targetCurrency(targetCurrencyOptional.get())
          .build();
      final var save = exchangeRateDaoImpl.save(exchangeRate);
      currencyDaoImpl.setTransactionalConnection(null);
      exchangeRateDaoImpl.setTransactionalConnection(null);
      connection.commit();
      return save;
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_SAVE_EXCHANGE_RATE_MSG, e);
    }
  }

  private void checkDaoTypes() {
    if (!(exchangeRateDao instanceof ExchangeRateDaoImpl)
        || !(currencyDao instanceof CurrencyDaoImpl)) {
      throw new RuntimeException(UNKNOWN_DAO_MSG);
    }
  }
}
