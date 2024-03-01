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
import ru.starkov.dao.CurrencyDao;
import ru.starkov.exception.CurrencyAlreadyExistException;
import ru.starkov.exception.DatabaseException;
import ru.starkov.model.Currency;
import ru.starkov.util.ConnectionManager;

/**
 * Implementation of the CurrencyDao interface for performing CRUD operations related to currencies
 * in the database.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CurrencyDaoImpl implements CurrencyDao {

  private static final String SAVE_SQL = """
      INSERT INTO currency_exchange_app.public.currencies (code, full_name, sign)
      VALUES (?, ?, ?);
      """;
  private static final String FIND_ALL_SQL = """
      SELECT id, code, full_name, sign
      FROM currency_exchange_app.public.currencies
      ORDER BY id
      LIMIT 500;
      """;

  private static final String FIND_BY_CODE_SQL = """
      SELECT id, code, full_name, sign
      FROM currency_exchange_app.public.currencies
      WHERE LOWER(code) = LOWER(?);
      """;

  private static final String UPDATE_SQL = """
      UPDATE currency_exchange_app.public.currencies
      SET id = ?,
      code = ?,
      full_name = ?,
      sign = ?
      WHERE id = ?;
      """;
  private static final String FAILED_TO_FIND_CURRENCY_BY_CODE_ERROR_MESSAGE =
      "Failed to find currency by code";

  private static volatile CurrencyDaoImpl instance;

  @Setter
  private Connection transactionalConnection;

  /**
   * Returns the singleton instance of CurrencyDaoImpl.
   *
   * @return the singleton instance of CurrencyDaoImpl
   */
  public static CurrencyDaoImpl getInstance() {
    if (instance == null) {
      synchronized (CurrencyDaoImpl.class) {
        if (instance == null) {
          instance = new CurrencyDaoImpl();
        }
      }
    }
    return instance;
  }

  @Override
  public Currency save(Currency currency) {
    try (var connection = ConnectionManager.getConnection();
        var preparedStatement = connection.prepareStatement(SAVE_SQL,
            Statement.RETURN_GENERATED_KEYS)) {
      preparedStatement.setString(1, currency.getCode());
      preparedStatement.setString(2, currency.getFullName());
      preparedStatement.setString(3, currency.getSign());
      preparedStatement.executeUpdate();
      try (var generatedKeys = preparedStatement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          int id = generatedKeys.getInt(1);
          currency.setId(id);
        }
      }
      return currency;
    } catch (SQLException e) {
      if (e instanceof PSQLException && e.getMessage()
          .contains("duplicate key value violates unique constraint")) {
        throw new CurrencyAlreadyExistException(
            String.format("The currency with the code '%s' already exists in the database.",
                currency.getCode()));
      } else {
        throw new DatabaseException("Failed to save currency", e);
      }
    }
  }

  @Override
  public List<Currency> findAll() {
    List<Currency> currencyList = new ArrayList<>();
    try (var connection = ConnectionManager.getConnection();
        var preparedStatement = connection.prepareStatement(FIND_ALL_SQL)) {
      try (var resultSet = preparedStatement.executeQuery()) {
        while (resultSet.next()) {
          currencyList.add(mapResultSetToCurrency(resultSet));
        }
      }
      return currencyList;
    } catch (SQLException e) {
      throw new DatabaseException("Failed to find currencies", e);
    }
  }

  @Override
  public Optional<Currency> findByCode(String code) {
    Connection connection = null;
    try {
      connection = (transactionalConnection == null)
          ? ConnectionManager.getConnection() : transactionalConnection;
      try (var preparedStatement = connection.prepareStatement(FIND_BY_CODE_SQL)) {
        preparedStatement.setString(1, code);
        try (var resultSet = preparedStatement.executeQuery()) {
          if (resultSet.next()) {
            return Optional.of(mapResultSetToCurrency(resultSet));
          } else {
            return Optional.empty();
          }
        }
      }
    } catch (SQLException e) {
      throw new DatabaseException(FAILED_TO_FIND_CURRENCY_BY_CODE_ERROR_MESSAGE, e);
    } finally {
      closeNotTransactionalConnection(connection);
    }
  }

  @Override
  public void update(Currency currency) {
    try (var connection = ConnectionManager.getConnection();
        var preparedStatement = connection.prepareStatement(UPDATE_SQL)) {
      preparedStatement.setString(1, currency.getCode());
      preparedStatement.setString(2, currency.getFullName());
      preparedStatement.setString(3, currency.getSign());
      preparedStatement.setInt(4, currency.getId());
      preparedStatement.executeUpdate();
    } catch (SQLException e) {
      throw new DatabaseException("Failed to update currency", e);
    }
  }

  private Currency mapResultSetToCurrency(ResultSet resultSet) throws SQLException {
    return Currency.builder()
        .id(resultSet.getInt("id"))
        .code(resultSet.getString("code"))
        .fullName(resultSet.getString("full_name"))
        .sign(resultSet.getString("sign"))
        .build();
  }

  private void closeNotTransactionalConnection(Connection connection) {
    if (transactionalConnection == null && connection != null) {
      try {
        connection.close();
      } catch (SQLException e) {
        throw new DatabaseException(FAILED_TO_FIND_CURRENCY_BY_CODE_ERROR_MESSAGE, e);
      }
    }
  }
}
