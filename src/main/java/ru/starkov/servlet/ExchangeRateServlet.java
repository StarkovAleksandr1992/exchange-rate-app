package ru.starkov.servlet;

import static ru.starkov.util.Constants.BASE_CURRENCY_CODE;
import static ru.starkov.util.Constants.EXCHANGE_RATE_PATH;
import static ru.starkov.util.Constants.HTTP_METHOD_PATCH;
import static ru.starkov.util.Constants.RATE;
import static ru.starkov.util.Constants.TARGET_CURRENCY_CODE;
import static ru.starkov.util.Constants.WEB_APP_PATH;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import ru.starkov.dto.ExchangeRateInfo;
import ru.starkov.dto.mapper.ExchangeRateMapper;
import ru.starkov.exception.DatabaseException;
import ru.starkov.exception.ExchangeRateNotFoundException;
import ru.starkov.service.ExchangeRateService;

/**
 * Servlet to manage operations related to a single exchange rate. Extends
 * {@link AbstractHttpServlet}.
 */
@WebServlet(
    name = "ExchangeRateServlet",
    description = "Handles operations related to a single exchange rate",
    value = EXCHANGE_RATE_PATH + "/*"
)
public final class ExchangeRateServlet extends AbstractHttpServlet {

  private ExchangeRateService exchangeRateService;
  private ExchangeRateMapper exchangeRateMapper;

  @Override
  public void init(final ServletConfig config) throws ServletException {
    super.init(config);
    var servletContext = config.getServletContext();
    this.exchangeRateService = (ExchangeRateService) servletContext.getAttribute(
        ExchangeRateService.class.getName());
    this.exchangeRateMapper = (ExchangeRateMapper) servletContext.getAttribute(
        ExchangeRateMapper.class.getName());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      var baseCurrencyCode = (String) req.getAttribute(BASE_CURRENCY_CODE);
      var targetCurrencyCode = (String) req.getAttribute(TARGET_CURRENCY_CODE);
      var exchangeRate = exchangeRateService
          .findByCurrencyCodes(baseCurrencyCode, targetCurrencyCode);
      try (var writer = resp.getWriter()) {
        resp.setStatus(HttpServletResponse.SC_OK);
        writer.write(gson.toJson(exchangeRateMapper.toDto(exchangeRate)));
      }
    } catch (ExchangeRateNotFoundException e) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } catch (DatabaseException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DEFAULT_UNKNOWN_ERROR_MESSAGE);
    }
  }

  @Override
  protected void service(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    if (req.getMethod().equals(HTTP_METHOD_PATCH)) {
      try {
        var baseCurrencyCode = (String) req.getAttribute(BASE_CURRENCY_CODE);
        var targetCurrencyCode = (String) req.getAttribute(TARGET_CURRENCY_CODE);
        var rate = (BigDecimal) req.getAttribute(RATE);
        var exchangeRateInfo = new ExchangeRateInfo(baseCurrencyCode, targetCurrencyCode,
            rate);
        exchangeRateService.update(exchangeRateInfo);
        resp.sendRedirect(
            WEB_APP_PATH + EXCHANGE_RATE_PATH + "/" + baseCurrencyCode + targetCurrencyCode);
      } catch (ExchangeRateNotFoundException e) {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
      } catch (DatabaseException e) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      } catch (Exception e) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DEFAULT_UNKNOWN_ERROR_MESSAGE);
      }
    } else {
      super.service(req, resp);
    }
  }
}
