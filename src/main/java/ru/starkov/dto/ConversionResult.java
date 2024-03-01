package ru.starkov.dto;

public record ConversionResult(
    CurrencyRequestDto from,
    CurrencyRequestDto to,
    String rate,
    String amount,
    String convertedAmount
) {

}
