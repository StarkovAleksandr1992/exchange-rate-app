package ru.starkov.model;

import lombok.Data;

@Data
public class Currency {
    private Integer id;
    private String code;
    private String fullname;
    private String sign;
}
