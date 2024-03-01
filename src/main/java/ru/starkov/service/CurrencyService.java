package ru.starkov.service;

import static ru.starkov.util.Constants.ERROR_CURRENCY_NOT_FOUND_MESSAGE;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import ru.starkov.dao.CurrencyDao;
import ru.starkov.dto.CurrencyRequestDto;
import ru.starkov.dto.mapper.CurrencyMapper;
import ru.starkov.exception.CurrencyNotFoundException;
import ru.starkov.model.Currency;

@RequiredArgsConstructor
public final class CurrencyService {

  private final CurrencyDao currencyDao;


  public List<Currency> findAll() {
    return currencyDao.findAll();
  }

  public Currency findByCode(String code) throws CurrencyNotFoundException {
    Objects.requireNonNull(code, "Currency code cannot ve null");
    return currencyDao.findByCode(code).orElseThrow(() -> new CurrencyNotFoundException(
        String.format(ERROR_CURRENCY_NOT_FOUND_MESSAGE, code)));
  }

  public Currency save(CurrencyRequestDto currencyRequestDto) {
    Objects.requireNonNull(currencyRequestDto, "Currency cannot be null");
    return currencyDao.save(CurrencyMapper.INSTANCE.toModel(currencyRequestDto));
  }
}
