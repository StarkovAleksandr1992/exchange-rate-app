package ru.starkov.model;

import lombok.Builder;
import lombok.Data;

/**
 * This class represents a currency model. It contains the ID, code, full name and the sign of a
 * currency.
 */
@Data
@Builder
public final class Currency {

  private Integer id;
  private String code;
  private String fullName;
  private String sign;

}
