package ru.starkov.service;

import static ru.starkov.util.Constants.ERROR_CURRENCY_NOT_FOUND_MESSAGE;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import ru.starkov.dao.CurrencyDao;
import ru.starkov.dao.ExchangeRateDao;
import ru.starkov.dao.impl.CurrencyDaoImpl;
import ru.starkov.dao.impl.ExchangeRateDaoImpl;
import ru.starkov.dto.ConversionResult;
import ru.starkov.dto.mapper.CurrencyMapper;
import ru.starkov.exception.CurrencyNotFoundException;
import ru.starkov.exception.ExchangeRateNotFoundException;
import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;
import ru.starkov.util.ConnectionManager;

@RequiredArgsConstructor
public class ExchangeService {

  private final ExchangeRateDao exchangeRateDao;
  private final CurrencyDao currencyDao;
  private final CurrencyMapper currencyMapper;

  public ConversionResult convert(String fromCurrencyCode, String toCurrencyCode,
      BigDecimal amount) {
    try (var connection = ConnectionManager.getConnection()) {
      checkDaoTypes();
      final var currencyDaoImpl = (CurrencyDaoImpl) currencyDao;
      final var exchangeRateDaoImpl = (ExchangeRateDaoImpl) exchangeRateDao;
      connection.setAutoCommit(false);
      connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
      exchangeRateDaoImpl.setTransactionalConnection(connection);
      currencyDaoImpl.setTransactionalConnection(connection);

      var fromCurrency = findCurrencyByCode(currencyDaoImpl, fromCurrencyCode);
      var toCurrency = findCurrencyByCode(currencyDaoImpl, toCurrencyCode);

      var rate = determineExchangeRate(exchangeRateDaoImpl, fromCurrencyCode,
          toCurrencyCode);
      exchangeRateDaoImpl.setTransactionalConnection(null);
      currencyDaoImpl.setTransactionalConnection(null);
      return createConversionResult(fromCurrency, toCurrency, rate, amount);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private void checkDaoTypes() {
    if (!(exchangeRateDao instanceof ExchangeRateDaoImpl)
        || !(currencyDao instanceof CurrencyDaoImpl)) {
      throw new RuntimeException("Unknown dao.");
    }
  }

  private Currency findCurrencyByCode(CurrencyDaoImpl currencyDaoImpl, String currencyCode) {
    return currencyDaoImpl.findByCode(currencyCode)
        .orElseThrow(() -> new CurrencyNotFoundException(
            String.format(ERROR_CURRENCY_NOT_FOUND_MESSAGE, currencyCode)));
  }

  private BigDecimal determineExchangeRate(ExchangeRateDaoImpl exchangeRateDaoImpl,
      String fromCurrencyCode, String toCurrencyCode) {
    return exchangeRateDaoImpl.findByCurrencyCodes(fromCurrencyCode, toCurrencyCode)
        .map(ExchangeRate::getRate)
        .orElseGet(
            () -> findIndirectExchangeRate(exchangeRateDaoImpl, fromCurrencyCode, toCurrencyCode));
  }


  private BigDecimal findIndirectExchangeRate(ExchangeRateDaoImpl exchangeRateDaoImpl,
      String fromCurrencyCode, String toCurrencyCode) {
    return exchangeRateDaoImpl.findByCurrencyCodes(toCurrencyCode, fromCurrencyCode)
        .map(exchangeRate -> BigDecimal.ONE.divide(exchangeRate.getRate(), 6, RoundingMode.HALF_UP))
        .orElseGet(() -> calculateIndirectCrossExchangeRate(exchangeRateDaoImpl, fromCurrencyCode,
            toCurrencyCode));
  }

  private BigDecimal calculateIndirectCrossExchangeRate(ExchangeRateDaoImpl exchangeRateDaoImpl,
      String fromCurrencyCode, String toCurrencyCode) {
    var allByFrom = exchangeRateDaoImpl.findAllByBaseCurrencyCode(fromCurrencyCode);
    var allByTo = exchangeRateDaoImpl.findAllByBaseCurrencyCode(toCurrencyCode);

    var fromTargetCurrencyCodes = allByFrom.stream()
        .flatMap(rate -> Stream.of(rate.getTargetCurrency().getCode()))
        .toList();
    var byTargetCurrencyCodes = allByTo.stream()
        .flatMap(rate -> Stream.of(rate.getTargetCurrency().getCode()))
        .toList();

    List<String> sharedCurrencyCodes = new ArrayList<>(fromTargetCurrencyCodes);
    sharedCurrencyCodes.retainAll(byTargetCurrencyCodes);
    var sharedCurrencyCode = sharedCurrencyCodes.stream()
        .findAny();
    if (sharedCurrencyCode.isEmpty()) {
      return calculateDirectCrossExchangeRate(exchangeRateDaoImpl,
          fromCurrencyCode, toCurrencyCode);
    }
    var from = allByFrom.stream()
        .filter(
            rate -> rate.getTargetCurrency().getCode().equalsIgnoreCase(sharedCurrencyCode.get()))
        .findAny();
    var to = allByTo.stream()
        .filter(
            rate -> rate.getTargetCurrency().getCode().equalsIgnoreCase(sharedCurrencyCode.get()))
        .findAny();
    if (from.isEmpty() || to.isEmpty()) {
      return calculateDirectCrossExchangeRate(exchangeRateDaoImpl,
          fromCurrencyCode, toCurrencyCode);
    }

    return from.get().getRate().divide(to.get().getRate(), 6, RoundingMode.HALF_UP);
  }

  private BigDecimal calculateDirectCrossExchangeRate(ExchangeRateDaoImpl exchangeRateDaoImpl,
      String fromCurrencyCode, String toCurrencyCode) {
    var allByFrom = exchangeRateDaoImpl.findAllByTargetCurrencyCode(fromCurrencyCode);
    var allByTo = exchangeRateDaoImpl.findAllByTargetCurrencyCode(toCurrencyCode);

    var fromTargetCurrencyCodes = allByFrom.stream()
        .flatMap(rate -> Stream.of(rate.getBaseCurrency().getCode()))
        .toList();
    var byTargetCurrencyCodes = allByTo.stream()
        .flatMap(rate -> Stream.of(rate.getBaseCurrency().getCode()))
        .toList();

    List<String> sharedCurrencyCodes = new ArrayList<>(fromTargetCurrencyCodes);
    sharedCurrencyCodes.retainAll(byTargetCurrencyCodes);
    var sharedCurrencyCode = sharedCurrencyCodes.stream()
        .findAny()
        .orElseThrow(() -> new ExchangeRateNotFoundException("Exchange rate not found."));

    var from = allByFrom.stream()
        .filter(rate -> rate.getBaseCurrency().getCode().equalsIgnoreCase(sharedCurrencyCode))
        .findAny();
    var to = allByTo.stream()
        .filter(rate -> rate.getBaseCurrency().getCode().equalsIgnoreCase(sharedCurrencyCode))
        .findAny();
    if (from.isEmpty() || to.isEmpty()) {
      throw new ExchangeRateNotFoundException("Exchange rate not found.");
    }

    return to.get().getRate().divide(from.get().getRate(), 6, RoundingMode.HALF_UP);
  }

  private ConversionResult createConversionResult(Currency fromCurrency, Currency toCurrency,
      BigDecimal rate, BigDecimal amount) {
    return new ConversionResult(
        currencyMapper.toDto(fromCurrency),
        currencyMapper.toDto(toCurrency),
        rate.setScale(6, RoundingMode.HALF_UP).toPlainString(),
        amount.setScale(2, RoundingMode.HALF_UP).toPlainString(),
        rate.multiply(amount).setScale(2, RoundingMode.HALF_UP).toPlainString()
    );
  }
}
