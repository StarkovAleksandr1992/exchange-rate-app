package ru.starkov.dto.mapper;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.starkov.dto.ExchangeRateDto;
import ru.starkov.model.Currency;
import ru.starkov.model.ExchangeRate;

/**
 * An interface for mapping between exchange rate entities and DTOs (Data Transfer Objects). Uses
 * MapStruct for automatic mapping implementation.
 *
 * @see ExchangeRateDto
 * @see ExchangeRate
 */
@Mapper
public interface ExchangeRateMapper {

  ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

  /**
   * Converts an exchange rate entity to its corresponding DTO.
   *
   * @param model the exchange rate entity to convert
   * @return the corresponding DTO
   */
  default ExchangeRateDto toDto(ExchangeRate model) {
    CurrencyMapper currencyMapper = CurrencyMapper.INSTANCE;
    Currency baseCurrency = model.getBaseCurrency();
    Currency targetCurrency = model.getTargetCurrency();
    BigDecimal rate = model.getRate();
    return new ExchangeRateDto(currencyMapper.toDto(baseCurrency),
        currencyMapper.toDto(targetCurrency), rate);
  }

  List<ExchangeRateDto> collectionToListDto(Collection<ExchangeRate> exchangeRates);

}
