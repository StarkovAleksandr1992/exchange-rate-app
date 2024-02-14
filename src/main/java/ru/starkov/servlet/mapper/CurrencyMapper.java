package ru.starkov.servlet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.starkov.model.Currency;
import ru.starkov.servlet.dto.CurrencyDto;

import java.util.List;

@Mapper
public interface CurrencyMapper {

    CurrencyMapper INSTANCE = Mappers.getMapper(CurrencyMapper.class);
    CurrencyDto toDto(Currency currency);
    List<CurrencyDto> collectionToDto(List<Currency> currencies);
}
