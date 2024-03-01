package ru.starkov.servlet;

import static ru.starkov.util.Constants.CODE;
import static ru.starkov.util.Constants.CURRENCIES_PATH;
import static ru.starkov.util.Constants.CURRENCY_PATH;
import static ru.starkov.util.Constants.NAME;
import static ru.starkov.util.Constants.SIGN;
import static ru.starkov.util.Constants.WEB_APP_PATH;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ru.starkov.dto.CurrencyRequestDto;
import ru.starkov.dto.mapper.CurrencyMapper;
import ru.starkov.exception.CurrencyAlreadyExistException;
import ru.starkov.exception.DatabaseException;
import ru.starkov.service.CurrencyService;

/**
 * Servlet to manage currency-related requests. Extends {@link AbstractHttpServlet}.
 */
@WebServlet(
    name = "CurrenciesServlet",
    description = "Handles operations related to currencies",
    value = CURRENCIES_PATH
)
public final class CurrenciesServlet extends AbstractHttpServlet {

  private CurrencyService currencyService;
  private CurrencyMapper currencyMapper;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    var servletContext = config.getServletContext();
    this.currencyService = (CurrencyService) servletContext.getAttribute(
        CurrencyService.class.getName());
    this.currencyMapper = (CurrencyMapper) servletContext.getAttribute(
        CurrencyMapper.class.getName());
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      handleCurrencyListRequest(resp);
    } catch (DatabaseException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          e.getMessage());
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          DEFAULT_UNKNOWN_ERROR_MESSAGE);
    }
  }

  @Override
  protected void doPatch(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    super.doPatch(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    try {
      var code = (String) req.getAttribute(CODE);
      var name = (String) req.getAttribute(NAME);
      var sign = (String) req.getAttribute(SIGN);
      var currencyDto = new CurrencyRequestDto(code, name, sign);
      var currency = currencyService.save(currencyDto);
      resp.sendRedirect(WEB_APP_PATH + CURRENCY_PATH + "/" + currency.getCode());
    } catch (CurrencyAlreadyExistException e) {
      resp.sendError(HttpServletResponse.SC_CONFLICT, e.getMessage());
    } catch (DatabaseException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DEFAULT_UNKNOWN_ERROR_MESSAGE);
    }
  }

  private void handleCurrencyListRequest(HttpServletResponse resp) throws IOException {
    var currencies = currencyService.findAll();
    try (var writer = resp.getWriter()) {
      resp.setStatus(HttpServletResponse.SC_OK);
      writer.write(gson.toJson(currencyMapper.collectionToDto(currencies)));
    }
  }
}
