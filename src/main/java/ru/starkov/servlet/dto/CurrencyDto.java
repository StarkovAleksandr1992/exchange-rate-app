package ru.starkov.servlet.dto;

import lombok.Data;

@Data
public class CurrencyDto {
    private String code;
    private String fullname;
    private String sign;
}
