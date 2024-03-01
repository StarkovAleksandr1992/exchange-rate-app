package ru.starkov.dao.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.postgresql.util.PSQLException;
import ru.starkov.dao.ExchangeRateDao;
import ru.starkov.exception.DatabaseException;
import ru.starkov.exception.ExchangeRateAlreadyExistException;
import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;
import ru.starkov.util.ConnectionManager;

/**
 * Implementation of the ExchangeRateDao interface for performing CRUD operations related to
 * exchange rates in the database.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExchangeRateDaoImpl implements ExchangeRateDao {

  private static final String SAVE_SQL = """
      INSERT INTO currency_exchange_app.public.exchange_rates (base_currency_id, target_currency_id,
                                                               rate)
      VALUES (?, ?, ?);
      """;
  private static final String FIND_ALL_SQL = """
      SELECT er.id AS er_id,
             er.rate AS er_rate,
             bc.id AS bc_id,
             bc.code AS bc_code,
             bc.full_name AS bc_full_name,
             bc.sign AS bc_sign,
             tc.id AS tc_id,
             tc.code AS tc_code,
             tc.full_name AS tc_full_name,
             tc.sign AS tc_sign
      FROM currency_exchange_app.public.exchange_rates AS er
      JOIN currency_exchange_app.public.currencies AS bc on bc.id = er.base_currency_id
      JOIN currency_exchange_app.public.currencies AS tc ON er.target_currency_id = tc.id
      ORDER BY er.id
      LIMIT 500;
      """;

  private static final String FIND_BY_CURRENCY_IDS_SQL = """
        SELECT er.id        AS er_id,
               er.rate      AS er_rate,
               bc.id        AS bc_id,
               bc.code      AS bc_code,
               bc.full_name AS bc_full_name,
               bc.sign      AS bc_sign,
               tc.id        AS tc_id,
               tc.code      AS tc_code,
               tc.full_name AS tc_full_name,
               tc.sign      AS tc_sign
      FROM currency_exchange_app.public.exchange_rates AS er
               JOIN (SELECT id,
                            code,
                            full_name,
                            sign
                     FROM currency_exchange_app.public.currencies
                     WHERE id = ?) AS bc ON bc.id = er.base_currency_id
               JOIN (SELECT id,
                            code,
                            full_name,
                            sign
                     FROM currency_exchange_app.public.currencies
                     WHERE id = ?) AS tc ON tc.id = er.target_currency_id
      """;

  private static final String FIND_BY_CODES_SQL = """
      SELECT er.id        AS er_id,
             er.rate      AS er_rate,
             bc.id        AS bc_id,
             bc.code      AS bc_code,
             bc.full_name AS bc_full_name,
             bc.sign      AS bc_sign,
             tc.id        AS tc_id,
             tc.code      AS tc_code,
             tc.full_name AS tc_full_name,
             tc.sign      AS tc_sign
      FROM currency_exchange_app.public.exchange_rates AS er
               JOIN (SELECT id,
                            code,
                            full_name,
                            sign
                     FROM currency_exchange_app.public.currencies
                     WHERE LOWER(code) = LOWER(?)) AS bc ON bc.id = er.base_currency_id
               JOIN (SELECT id,
                            code,
                            full_name,
                            sign
                     FROM currency_exchange_app.public.currencies
                     WHERE LOWER(code) = LOWER(?)) AS tc ON tc.id = er.target_currency_id
      """;

  private static final String UPDATE_SQL = """
      UPDATE currency_exchange_app.public.exchange_rates
      SET base_currency_id   = ?,
          target_currency_id = ?,
          rate               = ?
      WHERE id = ?;
      """;

  private static final String FIND_ALL_BY_BASE_CURRENCY_CODE = """
      SELECT er.id AS er_id,
             er.rate AS er_rate,
             bc.id AS bc_id,
             bc.code AS bc_code,
             bc.full_name AS bc_full_name,
             bc.sign AS bc_sign,
             tc.id AS tc_id,
             tc.code AS tc_code,
             tc.full_name AS tc_full_name,
             tc.sign AS tc_sign
      FROM currency_exchange_app.public.exchange_rates AS er
      JOIN (SELECT id,
                            code,
                            full_name,
                            sign
                     FROM currency_exchange_app.public.currencies
                     WHERE LOWER(code) = LOWER(?)) AS bc ON bc.id = er.base_currency_id
      JOIN currency_exchange_app.public.currencies AS tc ON er.target_currency_id = tc.id
      LIMIT 500;
      """;

  private static final String FIND_ALL_BY_TARGET_CURRENCY_CODE = """
      SELECT er.id        AS er_id,
             er.rate      AS er_rate,
             bc.id        AS bc_id,
             bc.code      AS bc_code,
             bc.full_name AS bc_full_name,
             bc.sign      AS bc_sign,
             tc.id        AS tc_id,
             tc.code      AS tc_code,
             tc.full_name AS tc_full_name,
             tc.sign      AS tc_sign
      FROM currency_exchange_app.public.exchange_rates AS er
               JOIN currency_exchange_app.public.currencies AS bc ON bc.id = er.base_currency_id
               JOIN (SELECT id,
                            code,
                            full_name,
                            sign
                     FROM currency_exchange_app.public.currencies
                     WHERE LOWER(code) = LOWER(?)) AS tc ON er.target_currency_id = tc.id
      LIMIT 500;
      """;
  private static final String FAILED_TO_SAVE_EXCHANGE_RATE_ERROR_MESSAGE =
      "Failed to save the exchange rate";
  private static final String FAILED_TO_UPDATE_EXCHANGE_RATE_ERROR_MESSAGE =
      "Failed to update the exchange rate";
  private static final String FAILED_TO_FIND_EXCHANGE_RATE_BY_CODES =
      "Failed to find an exchange rate by currency codes";
  private static final String FAILED_TO_FIND_ALL_EXCHANGE_RATES_ERROR_MESSAGE =
      "Failed to find all exchange rates";

  private static volatile ExchangeRateDaoImpl instance;

  @Setter
  private Connection transactionalConnection;

  /**
   * Returns the singleton instance of ExchangeRateDao.
   *
   * @return the singleton instance of ExchangeRateDaoImpl
   */
  public static ExchangeRateDaoImpl getInstance() {
    if (instance == null) {
      synchronized (ExchangeRateDaoImpl.class) {
        if (instance == null) {
          instance = new ExchangeRateDaoImpl();
        }
      }
    }
    return instance;
  }

  @Override
  public ExchangeRate save(ExchangeRate exchangeRate) {
    Connection connection = null;
    try {
      connection = (transactionalConnection == null)
          ? ConnectionManager.getConnection() : transactionalConnection;
      try (var preparedStatement = connection.prepareStatement(SAVE_SQL,
          Statement.RETURN_GENERATED_KEYS)) {
        preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
        preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
        preparedStatement.setBigDecimal(3, exchangeRate.getRate());
        preparedStatement.executeUpdate();
        try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            var id = generatedKeys.getInt("id");
            exchangeRate.setId(id);
          }
        }
        return exchangeRate;
      }
    } catch (SQLException e) {
      if (e instanceof PSQLException && e.getMessage()
          .contains("duplicate key value violates unique constraint")) {
        throw new ExchangeRateAlreadyExistException(
            String.format(
                "The exchange rate for the currency pair '%s%s' already exists in the database.",
                exchangeRate.getBaseCurrency().getCode(),
                exchangeRate.getTargetCurrency().getCode()));
      } else {
        throw new DatabaseException(FAILED_TO_SAVE_EXCHANGE_RATE_ERROR_MESSAGE, e);
      }
    } finally {
      closeNotTransactionalConnection(connection, FAILED_TO_SAVE_EXCHANGE_RATE_ERROR_MESSAGE);
    }
  }

  @Override
  public List<ExchangeRate> findAll() {
    List<ExchangeRate> exchangeRates = new ArrayList<>();
    try (var connection = ConnectionManager.getConnection();
        var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
      try (var resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          var exchangeRate = mapResultSetToExchangeRate(resultSet);
          exchangeRates.add(exchangeRate);
        }
      }
      return exchangeRates;
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_FIND_ALL_EXCHANGE_RATES_ERROR_MESSAGE, e);
    }
  }

  @Override
  public Optional<ExchangeRate> findByCurrencies(Currency baseCurrency, Currency targetCurrency) {
    Connection connection = null;
    try {
      connection = (transactionalConnection == null)
          ? ConnectionManager.getConnection() : transactionalConnection;
      try (var preparedStatement = connection.prepareStatement(FIND_BY_CURRENCY_IDS_SQL)) {
        preparedStatement.setInt(1, baseCurrency.getId());
        preparedStatement.setInt(2, targetCurrency.getId());
        try (var resultSet = preparedStatement.executeQuery()) {
          if (resultSet.next()) {
            var exchangeRate = mapResultSetToExchangeRate(resultSet);
            return Optional.of(exchangeRate);
          } else {
            return Optional.empty();
          }
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_UPDATE_EXCHANGE_RATE_ERROR_MESSAGE, e);
    } finally {
      closeNotTransactionalConnection(connection, FAILED_TO_UPDATE_EXCHANGE_RATE_ERROR_MESSAGE);
    }
  }

  @Override
  public Optional<ExchangeRate> findByCurrencyCodes(String baseCurrencyCode,
      String targetCurrencyCode) {
    Connection connection = null;
    try {
      connection = (transactionalConnection == null)
          ? ConnectionManager.getConnection() : transactionalConnection;
      try (var preparedStatement = connection.prepareStatement(FIND_BY_CODES_SQL)) {
        preparedStatement.setString(1, baseCurrencyCode);
        preparedStatement.setString(2, targetCurrencyCode);
        try (var resultSet = preparedStatement.executeQuery()) {
          if (resultSet.next()) {
            return Optional.of(mapResultSetToExchangeRate(resultSet));
          } else {
            return Optional.empty();
          }
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_FIND_EXCHANGE_RATE_BY_CODES, e);
    } finally {
      closeNotTransactionalConnection(connection, FAILED_TO_FIND_EXCHANGE_RATE_BY_CODES);
    }
  }

  @Override
  public void update(ExchangeRate exchangeRate) {
    Connection connection = null;
    try {
      connection = (transactionalConnection == null)
          ? ConnectionManager.getConnection() : transactionalConnection;
      try (var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
        preparedStatement.setInt(1, exchangeRate.getBaseCurrency().getId());
        preparedStatement.setInt(2, exchangeRate.getTargetCurrency().getId());
        preparedStatement.setBigDecimal(3, exchangeRate.getRate());
        preparedStatement.setInt(4, exchangeRate.getId());
        preparedStatement.executeUpdate();
      }
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_UPDATE_EXCHANGE_RATE_ERROR_MESSAGE, e);
    } finally {
      closeNotTransactionalConnection(connection, FAILED_TO_UPDATE_EXCHANGE_RATE_ERROR_MESSAGE);
    }
  }

  @Override
  public List<ExchangeRate> findAllByBaseCurrencyCode(String baseCurrencyCode) {
    List<ExchangeRate> exchangeRates = new ArrayList<>();
    Connection connection = null;
    try {
      connection = (transactionalConnection == null)
          ? ConnectionManager.getConnection() : transactionalConnection;
      try (var preparedStatement = connection.prepareStatement(FIND_ALL_BY_BASE_CURRENCY_CODE)) {
        preparedStatement.setString(1, baseCurrencyCode);
        try (var resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            exchangeRates.add(mapResultSetToExchangeRate(resultSet));
          }
          return exchangeRates;
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_FIND_EXCHANGE_RATE_BY_CODES, e);
    } finally {
      closeNotTransactionalConnection(connection, FAILED_TO_FIND_EXCHANGE_RATE_BY_CODES);
    }
  }

  @Override
  public List<ExchangeRate> findAllByTargetCurrencyCode(String targetCurrencyCode) {
    List<ExchangeRate> exchangeRates = new ArrayList<>();
    Connection connection = null;
    try {
      connection = (transactionalConnection == null)
          ? ConnectionManager.getConnection() : transactionalConnection;
      try (var preparedStatement = connection.prepareStatement(FIND_ALL_BY_TARGET_CURRENCY_CODE)) {
        preparedStatement.setString(1, targetCurrencyCode);
        try (var resultSet = preparedStatement.executeQuery()) {
          while (resultSet.next()) {
            exchangeRates.add(mapResultSetToExchangeRate(resultSet));
          }
          return exchangeRates;
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_FIND_EXCHANGE_RATE_BY_CODES, e);
    } finally {
      closeNotTransactionalConnection(connection, FAILED_TO_FIND_EXCHANGE_RATE_BY_CODES);
    }
  }

  private ExchangeRate mapResultSetToExchangeRate(ResultSet resultSet) throws SQLException {
    var baseCurrency = Currency.builder()
        .id(resultSet.getInt("bc_id"))
        .code(resultSet.getString("bc_code"))
        .fullName(resultSet.getString("bc_full_name"))
        .sign(resultSet.getString("bc_sign"))
        .build();

    var targetCurrency = Currency.builder()
        .id(resultSet.getInt("tc_id"))
        .code(resultSet.getString("tc_code"))
        .fullName(resultSet.getString("tc_full_name"))
        .sign(resultSet.getString("tc_sign"))
        .build();

    return ExchangeRate.builder()
        .id(resultSet.getInt("er_id"))
        .baseCurrency(baseCurrency)
        .targetCurrency(targetCurrency)
        .rate(resultSet.getBigDecimal("er_rate"))
        .build();
  }

  private void closeNotTransactionalConnection(Connection connection, String errorMessage) {
    if (transactionalConnection == null && connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        throw new DatabaseException(errorMessage, e);
      }
    }
  }
}
