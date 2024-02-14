package ru.starkov.repository.dao.impl;

import ru.starkov.model.Currency;
import ru.starkov.repository.dao.CurrencyDao;
import ru.starkov.util.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CurrencyDaoImpl implements CurrencyDao {

    private static final String SAVE_SQL = """
            INSERT INTO currency_exchange_app.public.currencies (code, full_name, sign)
            VALUES (?, ?, ?);
            """;
    private static final String FIND_ALL_SQL = """
            SELECT id, code, full_name, sign
            FROM currency_exchange_app.public.currencies
            LIMIT 500;
            """;

    private static final String FIND_BY_CODE_SQL = """
            SELECT id, code, full_name, sign
            FROM currency_exchange_app.public.currencies
            WHERE (LOWER(code)) = (LOWER(?));
            """;

    private static final String UPDATE_SQL = """
            UPDATE currency_exchange_app.public.currencies
            SET id = ?,
            code = ?,
            full_name = ?,
            sign = ?
            WHERE id = ?;
            """;
    private static final String EXISTS_SQL = """
            SELECT id
            FROM currency_exchange_app.public.currencies
            WHERE code = ?;
            """;

    private static volatile CurrencyDaoImpl instance;

    private CurrencyDaoImpl() {
    }

    public static synchronized CurrencyDaoImpl getInstance() {
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
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(SAVE_SQL, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, currency.getCode());
            preparedStatement.setString(2, currency.getFullname());
            preparedStatement.setString(3, currency.getSign());
            preparedStatement.executeUpdate();
            var generatedKeys = preparedStatement.getGeneratedKeys();
            int id = generatedKeys.getInt("id");
            currency.setId(id);
            return currency;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Currency> findAll() {
        List<Currency> currencyList = new ArrayList<>();
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(FIND_ALL_SQL)) {
            var resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                currencyList.add(mapResultSetToCurrency(resultSet));
            }
            return currencyList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Currency> findByCode(String code) {
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(FIND_BY_CODE_SQL)) {
            preparedStatement.setString(1, code);
            var resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(mapResultSetToCurrency(resultSet));
            } else {
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void update(Currency currency) {
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(UPDATE_SQL)) {
            preparedStatement.setInt(1, currency.getId());
            preparedStatement.setString(2, currency.getCode());
            preparedStatement.setString(3, currency.getFullname());
            preparedStatement.setString(4, currency.getSign());
            preparedStatement.setInt(5, currency.getId());
            preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean exists(String code) {
        try (var con = ConnectionManager.getConnection();
             var preparedStatement = con.prepareStatement(EXISTS_SQL)) {
            preparedStatement.setString(1, code);
            var resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private Currency mapResultSetToCurrency(ResultSet resultSet) throws SQLException {
        var currency = new Currency();
        currency.setId(resultSet.getInt("id"));
        currency.setCode(resultSet.getString("code"));
        currency.setFullname(resultSet.getString("full_name"));
        currency.setSign(resultSet.getString("sign"));
        return currency;
    }

}