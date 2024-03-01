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
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;
import ru.starkov.servlet.filter.AbstractValidationFilter;


/**
 * The ExchangeRatesValidationFilter class is a servlet filter responsible for validating requests
 * to the ExchangeRatesServlet based on specific validation criteria. It extends the
 * {@link AbstractValidationFilter} and implements the validation logic for GET and POST requests.
 *
 * @see ru.starkov.servlet.ExchangeRatesServlet
 */
@WebFilter(servletNames = "ExchangeRatesServlet")
public class ExchangeRatesValidationFilter extends AbstractValidationFilter {

  private static final Set<String> POST_FORM_PARAMS =
      Set.of(BASE_CURRENCY_CODE, TARGET_CURRENCY_CODE, RATE);

  @Override
  protected void validateRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    if (isGetMethod(request)) {
      handeGetMethod(request, response, chain);
    } else if (isPostMethod(request)) {
      handlePostMethod(request, response, chain);
    } else {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private void handeGetMethod(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    chain.doFilter(request, response);
  }

  private void handlePostMethod(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    var parameterMap = request.getParameterMap();
    if (isNullOrEmpty(parameterMap)) {
      sendBadRequestError(response, ERROR_FORM_PARAMS_IS_MISSING);
      return;
    }
    if (!isClientFormMatchesServerSideForm(parameterMap.keySet(), POST_FORM_PARAMS)) {
      sendBadRequestError(response, ERROR_FORM_PARAMS_NOT_VALID);
      return;
    }
    var baseCurrencyCode = parameterMap.get(BASE_CURRENCY_CODE)[0];
    if (!isCurrencyCodeValid(baseCurrencyCode)) {
      sendBadRequestError(response,
          String.format(ERROR_INVALID_CURRENCY_CODE, baseCurrencyCode));
      return;
    }
    var targetCurrencyCode = parameterMap.get(TARGET_CURRENCY_CODE)[0];
    if (!isCurrencyCodeValid(targetCurrencyCode)) {
      sendBadRequestError(response,
          String.format(ERROR_INVALID_CURRENCY_CODE, targetCurrencyCode));
      return;
    }
    var rate = parameterMap.get(RATE)[0].replace(",", ".");
    if (!isANumberAndGreaterThanZero(rate)) {
      sendBadRequestError(response, String.format(ERROR_INVALID_RATE, rate));
      return;
    }
    request.setAttribute(BASE_CURRENCY_CODE, baseCurrencyCode);
    request.setAttribute(TARGET_CURRENCY_CODE, targetCurrencyCode);
    request.setAttribute(RATE, new BigDecimal(rate));
    chain.doFilter(request, response);
  }
}
