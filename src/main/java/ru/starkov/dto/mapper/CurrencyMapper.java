package ru.starkov.dto.mapper;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.starkov.dto.CurrencyRequestDto;
import ru.starkov.model.Currency;

/**
 * An interface for mapping between currency DTOs (Data Transfer Objects) and currency entities.
 * Uses MapStruct for automatic mapping implementation.
 *
 * @see CurrencyRequestDto
 * @see Currency
 */
@Mapper
public interface CurrencyMapper {

  CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);

  @Mapping(source = "code", target = "code")
  @Mapping(source = "fullName", target = "name")
  @Mapping(source = "sign", target = "sign")
  CurrencyRequestDto toDto(Currency currency);

  List<CurrencyRequestDto> collectionToDto(List<Currency> currencies);

  @Mapping(target = "id", ignore = true)
  @Mapping(source = "code", target = "code")
  @Mapping(source = "name", target = "fullName")
  @Mapping(source = "sign", target = "sign")
  Currency toModel(CurrencyRequestDto currencyRequestDto);
}
