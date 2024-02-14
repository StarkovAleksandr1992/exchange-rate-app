package ru.starkov.servlet.listener;

import com.google.gson.Gson;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ru.starkov.repository.dao.CurrencyDao;
import ru.starkov.repository.dao.ExchangeRateDao;
import ru.starkov.repository.dao.impl.CurrencyDaoImpl;
import ru.starkov.repository.dao.impl.ExchangeRateDaoImpl;
import ru.starkov.service.CurrencyService;
import ru.starkov.service.ExchangeRateService;

@WebListener
public class ContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContextListener.super.contextInitialized(sce);
        initBeans(sce);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContextListener.super.contextDestroyed(sce);
    }

    private void initBeans(ServletContextEvent sce) {
        CurrencyDao currencyDao = CurrencyDaoImpl.getInstance();
        CurrencyService currencyService = new CurrencyService(currencyDao);
        sce.getServletContext().setAttribute("currencyService", currencyService);

        ExchangeRateDao exchangeRateDao = ExchangeRateDaoImpl.getInstance();
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao);
        sce.getServletContext().setAttribute("exchangeRateService", exchangeRateService);

        Gson gson = new Gson();
        sce.getServletContext().setAttribute("gson", gson);
    }
}
