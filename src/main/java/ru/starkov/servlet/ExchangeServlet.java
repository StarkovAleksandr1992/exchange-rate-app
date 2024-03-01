package ru.starkov.servlet;

import static ru.starkov.util.Constants.AMOUNT;
import static ru.starkov.util.Constants.EXCHANGE_PATH;
import static ru.starkov.util.Constants.FROM_CURRENCY_CODE;
import static ru.starkov.util.Constants.TO_CURRENCY_CODE;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import ru.starkov.exception.CurrencyNotFoundException;
import ru.starkov.exception.DatabaseException;
import ru.starkov.exception.ExchangeRateNotFoundException;
import ru.starkov.service.ExchangeService;


/**
 * The ExchangeServlet class handles currency conversion operations. It serves GET requests to
 * convert currency amounts from one currency to another. Extends {@link AbstractHttpServlet}.
 */
@WebServlet(
    name = "ExchangeServlet",
    description = "Handles currency conversion operations",
    value = EXCHANGE_PATH
)
public class ExchangeServlet extends AbstractHttpServlet {

  private ExchangeService exchangeService;


  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    var servletContext = config.getServletContext();
    this.exchangeService = (ExchangeService) servletContext.getAttribute(
        ExchangeService.class.getName());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    try {
      var fromCurrencyCode = (String) req.getAttribute(FROM_CURRENCY_CODE);
      var toCurrencyCode = (String) req.getAttribute(TO_CURRENCY_CODE);
      var amount = (BigDecimal) req.getAttribute(AMOUNT);

      var conversionResult = exchangeService.convert(fromCurrencyCode, toCurrencyCode,
          amount);
      try (var writer = resp.getWriter()) {
        writer.write(gson.toJson(conversionResult));
      }
    } catch (CurrencyNotFoundException | ExchangeRateNotFoundException e) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } catch (DatabaseException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DEFAULT_UNKNOWN_ERROR_MESSAGE);
    }

  }
}
