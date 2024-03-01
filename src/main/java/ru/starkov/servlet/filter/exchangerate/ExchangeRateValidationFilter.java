package ru.starkov.servlet.filter.exchangerate;

import static ru.starkov.util.Constants.BASE_CURRENCY_CODE;
import static ru.starkov.util.Constants.RATE;
import static ru.starkov.util.Constants.TARGET_CURRENCY_CODE;
import static ru.starkov.util.ValidationUtils.isClientFormMatchesServerSideForm;
import static ru.starkov.util.ValidationUtils.isNullOrEmpty;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import ru.starkov.servlet.filter.AbstractValidationFilter;


/**
 * The ExchangeRateValidationFilter class is a servlet filter responsible for validating requests to
 * the ExchangeRateServlet based on specific validation criteria. It extends the
 * {@link AbstractValidationFilter} and implements the validation logic for GET and PATCH requests.
 *
 * @see ru.starkov.servlet.ExchangeRateServlet
 */
@WebFilter(servletNames = "ExchangeRateServlet")
public class ExchangeRateValidationFilter extends AbstractValidationFilter {

  private static final Set<String> PATCH_FORM_PARAMS = Set.of(RATE);

  @Override
  protected void validateRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    var requestUri = request.getRequestURI();
    var codesFromRequest = getCodesFromRequest(requestUri);
    if (codesFromRequest == null) {
      sendBadRequestError(response, ERROR_MISSING_CURRENCY_CODE);
      return;
    }
    var baseCurrencyCode = codesFromRequest.get(BASE_CURRENCY_CODE);
    if (!isCurrencyCodeValid(baseCurrencyCode)) {
      sendBadRequestError(response,
          String.format(ERROR_INVALID_CURRENCY_CODE, baseCurrencyCode));
      return;
    }
    var targetCurrencyCode = codesFromRequest.get(TARGET_CURRENCY_CODE);
    if (!isCurrencyCodeValid(targetCurrencyCode)) {
      sendBadRequestError(response,
          String.format(ERROR_INVALID_CURRENCY_CODE, targetCurrencyCode));
      return;
    }
    request.setAttribute(BASE_CURRENCY_CODE, baseCurrencyCode);
    request.setAttribute(TARGET_CURRENCY_CODE, targetCurrencyCode);
    if (isGetMethod(request)) {
      handleGetRequest(request, response, chain);
    } else if (isPatchMethod(request)) {
      handlePatchRequest(request, response, chain);
    } else {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private void handleGetRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);
  }

  private void handlePatchRequest(HttpServletRequest request,
      HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    var parameterMap = getParameterMap(request);

    if (isNullOrEmpty(parameterMap)) {
      sendBadRequestError(response, ERROR_FORM_PARAMS_IS_MISSING);
      return;
    }
    if (!isClientFormMatchesServerSideForm(parameterMap.keySet(),
        PATCH_FORM_PARAMS)) {
      sendBadRequestError(response, ERROR_FORM_PARAMS_NOT_VALID);
    }
    var rate = parameterMap.get(RATE)[0].replace(",", ".");
    if (!isANumberAndGreaterThanZero(rate)) {
      sendBadRequestError(response, String.format(ERROR_INVALID_RATE, rate));
      return;
    }
    request.setAttribute(RATE, new BigDecimal(rate));
    chain.doFilter(request, response);
  }

  private Map<String, String[]> getParameterMap(HttpServletRequest request) throws IOException {
    try (BufferedReader reader = request.getReader()) {
      return reader.lines()
          .map(parameter -> parameter.split("="))
          .collect(Collectors.groupingBy(
              split -> split[0],
              Collectors.mapping(split -> split.length > 1 ? split[1] : "", Collectors.toList())
          ))
          .entrySet().stream()
          .collect(Collectors.toMap(
              Map.Entry::getKey,
              entry -> entry.getValue().toArray(new String[0])
          ));
    }
  }

  private Map<String, String> getCodesFromRequest(String requestUri) {
    int lastSlashIndex = requestUri.lastIndexOf("/");
    if (lastSlashIndex != -1 && lastSlashIndex + 1 < requestUri.length()) {
      var codes = requestUri.substring(lastSlashIndex + 1);
      if (codes.length() != 2 * CURRENCY_CODE_LENGTH) {
        return null;
      }
      var baseCurrencyCode = codes.substring(0, CURRENCY_CODE_LENGTH);
      var targetCurrencyCode = codes.substring(CURRENCY_CODE_LENGTH);
      return Map.of(BASE_CURRENCY_CODE, baseCurrencyCode,
          TARGET_CURRENCY_CODE, targetCurrencyCode);
    }
    return null;
  }
}
