package ru.starkov.repository.dao.impl;

import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;
import ru.starkov.repository.dao.ExchangeRateDao;
import ru.starkov.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExchangeRateDaoImpl implements ExchangeRateDao {

    private static final String SAVE_SQL = """
            INSERT INTO currency_exchange_app.public.exchange_rates(base_currency_id, target_currency_id, rate)
            VALUES (?, ?, ?);
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM currency_exchange_app.public.exchange_rates
            LIMIT 500;
            """;
    private static final String FIND_BY_CURRENCIES_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM currency_exchange_app.public.exchange_rates
            WHERE base_currency_id = ? AND target_currency_id = ?;
            """;

    private static final String FIND_BY_CODES_SQL = """
            SELECT id, base_currency_id, target_currency_id, rate
            FROM currency_exchange_app.public.exchange_rates
            WHERE base_currency_id = (
                    SELECT id 
                    FROM currency_exchange_app.public.currencies
                    WHERE code = ?
                    )
                AND target_currency_id = ( 
                    SELECT id
                    FROM currency_exchange_app.public.currencies
                    WHERE code = ?
                    );
            """;
    private static final String UPDATE_SQL = """
            UPDATE currency_exchange_app.public.exchange_rates
            SET base_currency_id = ?,
                target_currency_id = ?,
                rate = ?
            WHERE id = ?;
            """;

    private static volatile ExchangeRateDaoImpl instance;

    private ExchangeRateDaoImpl() {
    }

    public static synchronized ExchangeRateDaoImpl getInstance() {
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
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            int id = generatedKeys.getInt("id");
            exchangeRate.setId(id);
            return exchangeRate;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<ExchangeRate> findAll() {
        List<ExchangeRate> exchangeRates = new ArrayList<>();
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                var exchangeRate = mapResultSetToExchangeRate(resultSet);
                exchangeRates.add(exchangeRate);
            }
            return exchangeRates;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ExchangeRate> findByCurrencies(Currency baseCurrency, Currency targetCurrency) {
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(FIND_BY_CURRENCIES_SQL)) {
            preparedStatement.setInt(1, baseCurrency.getId());
            preparedStatement.setInt(2, targetCurrency.getId());
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var exchangeRate = mapResultSetToExchangeRate(resultSet);
                return Optional.of(exchangeRate);
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ExchangeRate> findByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(FIND_BY_CURRENCIES_SQL)) {
            preparedStatement.setString(1, baseCurrencyCode);
            preparedStatement.setString(2, targetCurrencyCode);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                var exchangeRate = mapResultSetToExchangeRate(resultSet);
                return Optional.of(exchangeRate);
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(ExchangeRate exchangeRate) {
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setInt(1, exchangeRate.getBaseCurrencyId());
            preparedStatement.setInt(2, exchangeRate.getTargetCurrencyId());
            preparedStatement.setBigDecimal(3, exchangeRate.getRate());
            preparedStatement.setInt(4, exchangeRate.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static ExchangeRate mapResultSetToExchangeRate(ResultSet resultSet) throws SQLException {
        var exchangeRate = new ExchangeRate();
        exchangeRate.setId(resultSet.getInt("id"));
        exchangeRate.setBaseCurrencyId(resultSet.getInt("base_currency_id"));
        exchangeRate.setTargetCurrencyId(resultSet.getInt("target_currency_id"));
        exchangeRate.setRate(resultSet.getBigDecimal("rate"));
        return exchangeRate;
    }
}
