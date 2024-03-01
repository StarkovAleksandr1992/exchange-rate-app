package ru.starkov.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.regex.Pattern;
import ru.starkov.util.Constants;

/**
 * The AbstractValidationFilter class provides common functionality for validation filters used in
 * servlets. It implements the Filter interface and defines methods for filtering requests based on
 * validation logic.
 */
public abstract class AbstractValidationFilter implements Filter {

  protected static final String ERROR_MISSING_CURRENCY_CODE =
      "The currency code is missing in the URI";
  protected static final String ERROR_INVALID_CURRENCY_CODE =
      "This is not a valid currency code: '%s'";
  protected static final String ERROR_INVALID_RATE =
      "This is not a valid currency exchange rate: '%s'";
  protected static final String ERROR_FORM_PARAMS_IS_MISSING =
      "Request form parameters are empty, please fill the form and try again";
  protected static final String ERROR_FORM_PARAMS_NOT_VALID =
      "Request form doesn't match server-side form";
  protected static final String ERROR_FORM_PARAM_IS_NULL_OR_BLANK =
      "Currency %s cannot be null or blank";

  protected static final int CURRENCY_CODE_LENGTH = 3;
  private static final Pattern CURRENCY_CODE_PATTERN = Pattern.compile("^[a-zA-Z]{3}$");


  protected abstract void validateRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException;

  /**
   * Filters the request and response. If the request is an instance of HttpServletRequest and the
   * response is an instance of HttpServletResponse, the filter logic is applied to validate the
   * request.
   *
   * @param request  the ServletRequest to be filtered
   * @param response the ServletResponse to be filtered
   * @param chain    the FilterChain to invoke the next filter in the chain
   * @throws IOException      if an I/O error occurs during the filtering process
   * @throws ServletException if a servlet error occurs during the filtering process
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (request instanceof HttpServletRequest httpServletRequest
        && response instanceof HttpServletResponse httpServletResponse) {
      FilterLogic filterLogic = this::validateRequest;
      filterLogic.apply(httpServletRequest, httpServletResponse, chain);
    } else {
      chain.doFilter(request, response);
    }
  }

  protected void sendBadRequestError(HttpServletResponse httpServletResponse, String errorMessage)
      throws IOException {
    httpServletResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, errorMessage);
  }

  @SuppressWarnings("all")
  protected boolean isCurrencyCodeValid(String currencyCode) {
    var codeMatcher = CURRENCY_CODE_PATTERN.matcher(currencyCode);
    return codeMatcher.matches();
  }

  protected boolean isGetMethod(HttpServletRequest request) {
    return request.getMethod().equalsIgnoreCase(Constants.HTTP_METHOD_GET);
  }

  protected boolean isPostMethod(HttpServletRequest request) {
    return request.getMethod().equalsIgnoreCase(Constants.HTTP_METHOD_POST);
  }

  protected boolean isPatchMethod(HttpServletRequest request) {
    return request.getMethod().equalsIgnoreCase(Constants.HTTP_METHOD_PATCH);
  }

  @SuppressWarnings("all")
  protected boolean isANumberAndGreaterThanZero(String value) {
    try {
      var bigDecimal = new BigDecimal(value);
      return bigDecimal.compareTo(BigDecimal.ZERO) > 0;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}
