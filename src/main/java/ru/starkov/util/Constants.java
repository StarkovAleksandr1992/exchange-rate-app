package ru.starkov.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * The Constants class provides constant values used throughout the application.
 * It defines various paths, HTTP methods, and attribute names used in the application.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Constants {

  public static final String WEB_APP_PATH = "/currency-exchange-app";
  public static final String CURRENCY_PATH = "/currency";
  public static final String CURRENCIES_PATH = "/currencies";
  public static final String EXCHANGE_RATE_PATH = "/exchangeRate";
  public static final String EXCHANGE_RATES_PATH = "/exchangeRates";
  public static final String EXCHANGE_PATH = "/exchange";
  public static final String HTTP_METHOD_GET = "GET";
  public static final String HTTP_METHOD_POST = "POST";
  public static final String HTTP_METHOD_PATCH = "PATCH";
  public static final String CODE = "code";
  public static final String NAME = "name";
  public static final String SIGN = "sign";
  public static final String BASE_CURRENCY_CODE = "baseCurrencyCode";
  public static final String TARGET_CURRENCY_CODE = "targetCurrencyCode";
  public static final String FROM_CURRENCY_CODE = "from";
  public static final String TO_CURRENCY_CODE = "to";
  public static final String AMOUNT = "amount";
  public static final String RATE = "rate";
  public static final String ERROR_CURRENCY_NOT_FOUND_MESSAGE =
      "Currency with following code is not found: %S";
}
