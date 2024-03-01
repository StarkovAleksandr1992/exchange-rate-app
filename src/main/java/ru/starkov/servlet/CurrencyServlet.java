package ru.starkov.servlet;

import static ru.starkov.util.Constants.CODE;
import static ru.starkov.util.Constants.CURRENCY_PATH;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import ru.starkov.dto.mapper.CurrencyMapper;
import ru.starkov.exception.CurrencyNotFoundException;
import ru.starkov.exception.DatabaseException;
import ru.starkov.service.CurrencyService;


/**
 * Servlet to manage operations related to a single currency. Extends {@link AbstractHttpServlet}.
 */
@WebServlet(
    name = "CurrencyServlet",
    description = "Handle operations related to a single currency",
    value = CURRENCY_PATH + "/*"
)
public final class CurrencyServlet extends AbstractHttpServlet {


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
      handleSingleCurrencyRequest(req, resp);
    } catch (CurrencyNotFoundException e) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
    } catch (DatabaseException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          e.getMessage());
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          DEFAULT_UNKNOWN_ERROR_MESSAGE);
    }
  }

  private void handleSingleCurrencyRequest(HttpServletRequest req, HttpServletResponse resp)
      throws IOException, CurrencyNotFoundException {
    var code = (String) req.getAttribute(CODE);
    var currency = currencyService.findByCode(code);
    try (var writer = resp.getWriter()) {
      resp.setStatus(HttpServletResponse.SC_OK);
      writer.write(gson.toJson(currencyMapper.toDto(currency)));
    }
  }
}
