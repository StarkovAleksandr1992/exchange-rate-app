package ru.starkov.servlet;

import static ru.starkov.util.Constants.BASE_CURRENCY_CODE;
import static ru.starkov.util.Constants.EXCHANGE_RATES_PATH;
import static ru.starkov.util.Constants.EXCHANGE_RATE_PATH;
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
import java.util.List;
import ru.starkov.dto.ExchangeRateInfo;
import ru.starkov.dto.mapper.ExchangeRateMapper;
import ru.starkov.exception.DatabaseException;
import ru.starkov.exception.ExchangeRateAlreadyExistException;
import ru.starkov.exception.ExchangeRateNotFoundException;
import ru.starkov.model.ExchangeRate;
import ru.starkov.service.ExchangeRateService;


/**
 * Servlet to manage exchange rates-related requests. Extends {@link AbstractHttpServlet}.
 */
@WebServlet(
    name = "ExchangeRatesServlet",
    description = "Handles operations related to exchange rates",
    value = EXCHANGE_RATES_PATH
)
public final class ExchangeRatesServlet extends AbstractHttpServlet {


  private ExchangeRateService exchangeRateService;
  private ExchangeRateMapper exchangeRateMapper;


  @Override
  public void init(ServletConfig config) throws ServletException {
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
      List<ExchangeRate> exchangeRates = exchangeRateService.findAll();
      try (var writer = resp.getWriter()) {
        resp.setStatus(HttpServletResponse.SC_OK);
        writer.write(gson.toJson(exchangeRateMapper.collectionToListDto(exchangeRates)));
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
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      var baseCurrencyCode = (String) req.getAttribute(BASE_CURRENCY_CODE);
      var targetCurrencyCode = (String) req.getAttribute(TARGET_CURRENCY_CODE);
      var rate = (BigDecimal) req.getAttribute(RATE);
      var exchangeRateInfo = new ExchangeRateInfo(baseCurrencyCode, targetCurrencyCode, rate);
      var exchangeRate = exchangeRateService.save(exchangeRateInfo);
      resp.sendRedirect(
          WEB_APP_PATH + EXCHANGE_RATE_PATH + "/"
              + exchangeRate.getBaseCurrency().getCode()
              + exchangeRate.getTargetCurrency().getCode());
    } catch (ExchangeRateAlreadyExistException e) {
      resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
    } catch (DatabaseException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DEFAULT_UNKNOWN_ERROR_MESSAGE);
    }
  }
}
