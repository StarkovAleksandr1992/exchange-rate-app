package ru.starkov.servlet.filter.currency;

import static ru.starkov.util.Constants.CODE;
import static ru.starkov.util.Constants.NAME;
import static ru.starkov.util.Constants.SIGN;
import static ru.starkov.util.ValidationUtils.isClientFormMatchesServerSideForm;
import static ru.starkov.util.ValidationUtils.isNullOrBlank;
import static ru.starkov.util.ValidationUtils.isNullOrEmpty;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import ru.starkov.servlet.filter.AbstractValidationFilter;


/**
 * The CurrenciesValidationFilter class is a servlet filter responsible for validating requests to
 * the CurrenciesServlet based on specific validation criteria. It extends the
 * {@link AbstractValidationFilter} and implements the validation logic for POST and GET requests.
 *
 * @see ru.starkov.servlet.CurrenciesServlet
 */
@WebFilter(servletNames = "CurrenciesServlet")
public class CurrenciesValidationFilter extends AbstractValidationFilter {

  private static final Set<String> FORM_PARAMS = Set.of(CODE, NAME, SIGN);

  @Override
  protected void validateRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    if (isPostMethod(request)) {
      handlePostRequest(request, response, chain);
    } else if (isGetMethod(request)) {
      chain.doFilter(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private void handlePostRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    var parameterMap = request.getParameterMap();
    if (isNullOrEmpty(parameterMap)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_FORM_PARAMS_IS_MISSING);
      return;
    }
    if (!isClientFormMatchesServerSideForm(parameterMap.keySet(), FORM_PARAMS)) {
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, ERROR_FORM_PARAMS_NOT_VALID);
      return;
    }
    var code = parameterMap.get(CODE)[0];
    if (!isCurrencyCodeValid(code)) {
      sendBadRequestError(response,
          String.format(ERROR_INVALID_CURRENCY_CODE, code));
      return;
    }
    var fullName = parameterMap.get(NAME)[0];
    if (isNullOrBlank(fullName)) {
      sendBadRequestError(response,
          String.format(ERROR_FORM_PARAM_IS_NULL_OR_BLANK, fullName));
      return;
    }
    var sign = parameterMap.get(SIGN)[0];
    if (isNullOrBlank(sign)) {
      sendBadRequestError(response,
          String.format(ERROR_FORM_PARAM_IS_NULL_OR_BLANK, sign));
      return;
    }
    request.setAttribute(CODE, code);
    request.setAttribute(NAME, fullName);
    request.setAttribute(SIGN, sign);
    chain.doFilter(request, response);
  }
}
