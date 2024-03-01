package ru.starkov.servlet.filter.currency;

import static ru.starkov.util.Constants.CODE;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ru.starkov.servlet.filter.AbstractValidationFilter;

/**
 * The CurrencyValidationFilter class is a servlet filter responsible for validating requests to the
 * based on specific validation criteria. It extends the {@link AbstractValidationFilter} and
 * implements the validation logic for GET requests.
 *
 * @see ru.starkov.servlet.CurrencyServlet
 */
@WebFilter(servletNames = "CurrencyServlet")
public class CurrencyValidationFilter extends AbstractValidationFilter {

  @Override
  protected void validateRequest(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain) throws ServletException, IOException {
    if (isGetMethod(request)) {
      var requestUri = request.getRequestURI();
      var codeFromRequest = getCodeFromRequest(requestUri);
      if (codeFromRequest == null) {
        response.sendError(HttpServletResponse.SC_BAD_REQUEST,
            ERROR_MISSING_CURRENCY_CODE);
        return;
      }
      if (!isCurrencyCodeValid(codeFromRequest)) {
        sendBadRequestError(response,
            String.format(ERROR_INVALID_CURRENCY_CODE, codeFromRequest));
        return;
      }
      request.setAttribute(CODE, codeFromRequest);
      chain.doFilter(request, response);
    } else {
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
    }
  }

  private String getCodeFromRequest(String requestUri) {
    int lastSlashIndex = requestUri.lastIndexOf("/");
    if (lastSlashIndex != -1 && lastSlashIndex + 1 < requestUri.length()) {
      return requestUri.substring(lastSlashIndex + 1);
    }
    return null;
  }
}
