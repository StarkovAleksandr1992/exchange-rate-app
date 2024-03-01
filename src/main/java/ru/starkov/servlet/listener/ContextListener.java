package ru.starkov.servlet.listener;

import com.google.gson.Gson;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ru.starkov.dao.CurrencyDao;
import ru.starkov.dao.ExchangeRateDao;
import ru.starkov.dao.impl.CurrencyDaoImpl;
import ru.starkov.dao.impl.ExchangeRateDaoImpl;
import ru.starkov.dto.mapper.CurrencyMapper;
import ru.starkov.dto.mapper.ExchangeRateMapper;
import ru.starkov.service.CurrencyService;
import ru.starkov.service.ExchangeRateService;
import ru.starkov.service.ExchangeService;
import ru.starkov.util.ConnectionManager;

/**
 * The ContextListener class is a servlet context listener responsible for initializing and destroying resources
 * when the servlet context is created and destroyed.
 * It initializes SQL driver, initializes beans, and manages the connection pool.
 */
@WebListener
public class ContextListener implements ServletContextListener {

  private static void initSqlDriver() {
    try {
      Class.forName("org.postgresql.Driver");
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    ServletContextListener.super.contextInitialized(sce);
    initSqlDriver();
    initBeans(sce);
    ConnectionManager.initConnectionPool();
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    ServletContextListener.super.contextDestroyed(sce);
    ConnectionManager.closeConnections();
  }

  private void initBeans(ServletContextEvent sce) {
    ServletContext servletContext = sce.getServletContext();

    CurrencyDao currencyDao = CurrencyDaoImpl.getInstance();
    CurrencyService currencyService = new CurrencyService(currencyDao);
    servletContext.setAttribute(CurrencyService.class.getName(), currencyService);

    ExchangeRateDao exchangeRateDao = ExchangeRateDaoImpl.getInstance();
    ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyDao);
    servletContext.setAttribute(ExchangeRateService.class.getName(), exchangeRateService);

    Gson gson = new Gson();
    servletContext.setAttribute(Gson.class.getName(), gson);

    CurrencyMapper currencyMapper = CurrencyMapper.INSTANCE;
    servletContext.setAttribute(CurrencyMapper.class.getName(), currencyMapper);

    ExchangeRateMapper exchangeRateMapper = ExchangeRateMapper.INSTANCE;
    servletContext.setAttribute(ExchangeRateMapper.class.getName(), exchangeRateMapper);

    ExchangeService exchangeService = new ExchangeService(exchangeRateDao, currencyDao,
        currencyMapper);
    servletContext.setAttribute(ExchangeService.class.getName(), exchangeService);
  }
}
