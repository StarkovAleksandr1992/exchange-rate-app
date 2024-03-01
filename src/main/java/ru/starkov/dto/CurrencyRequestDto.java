package ru.starkov.dto;


/**
 * A DTO (Data Transfer Object) representing currency information for requests. This record contains
 * the code, full name, and sign of a currency.
 */
public record CurrencyRequestDto(String code, String name, String sign) {

}
