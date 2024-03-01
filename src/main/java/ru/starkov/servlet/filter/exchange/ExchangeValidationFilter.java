package ru.starkov.servlet.filter.exchange;

import static ru.starkov.util.Constants.AMOUNT;
import static ru.starkov.util.Constants.FROM_CURRENCY_CODE;
import static ru.starkov.util.Constants.TO_CURRENCY_CODE;
import static ru.starkov.util.ValidationUtils.isClientFormMatchesServerSideForm;
import static ru.starkov.util.ValidationUtils.isNullOrEmpty;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import ru.starkov.servlet.filter.AbstractValidationFilter;


/**
 * The ExchangeValidationFilter class is a servlet filter responsible for validating requests to the
 * ExchangeServlet based on specific validation criteria. It extends the
 * {@link AbstractValidationFilter} and implements the validation logic for GET requests.
 *
 * @see ru.starkov.servlet.ExchangeServlet
 */
@WebFilter(servletNames = "ExchangeServlet")
public class ExchangeValidationFilter extends AbstractValidationFilter {


  private static final String ERROR_AMOUNT_NOT_VALID =
      "This is not a valid amount: '%s'";
  private static final Set<String> GET_URL_QUERY_PARAMS = Set.of(
      FROM_CURRENCY_CODE,
      TO_CURRENCY_CODE,
      AMOUNT);

  @Override
  protected void validateRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    if (isGetMethod(request)) {
      var parameterMap = request.getParameterMap();
      if (isNullOrEmpty(parameterMap)) {
        sendBadRequestError(response, ERROR_FORM_PARAMS_IS_MISSING);
        return;
      }
      if (!isClientFormMatchesServerSideForm(parameterMap.keySet(), GET_URL_QUERY_PARAMS)) {
        sendBadRequestError(response, ERROR_FORM_PARAMS_NOT_VALID);
        return;
      }
      var fromCurrencyCode = parameterMap.get(FROM_CURRENCY_CODE)[0];
      if (!isCurrencyCodeValid(fromCurrencyCode)) {
        sendBadRequestError(response, String.format(ERROR_INVALID_CURRENCY_CODE, fromCurrencyCode));
        return;
      }
      var toCurrencyCode = parameterMap.get(TO_CURRENCY_CODE)[0];
      if (!isCurrencyCodeValid(toCurrencyCode)) {
        sendBadRequestError(response, String.format(ERROR_INVALID_CURRENCY_CODE, toCurrencyCode));
        return;
      }
      var amount = parameterMap.get(AMOUNT)[0].replace(",", ".");
      if (!isANumberAndGreaterThanZero(amount)) {
        sendBadRequestError(response, String.format(ERROR_AMOUNT_NOT_VALID, amount));
        return;
      }
      request.setAttribute(FROM_CURRENCY_CODE, fromCurrencyCode);
      request.setAttribute(TO_CURRENCY_CODE, toCurrencyCode);
      request.setAttribute(AMOUNT, new BigDecimal(amount));
      chain.doFilter(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }
}
