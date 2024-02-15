package ru.starkov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.starkov.model.Currency;
import ru.starkov.service.CurrencyService;
import ru.starkov.servlet.dto.CurrencyDto;
import ru.starkov.servlet.mapper.CurrencyMapper;

import java.io.IOException;
import java.util.Optional;

@WebServlet("/currencies/*")
public class CurrencyServlet extends HttpServlet {

    private static final String CURRENCIES_PATH = "currencies";

    private CurrencyService currencyService;
    private CurrencyMapper currencyMapper;
    private Gson gson;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        var servletContext = config.getServletContext();
        this.currencyService = (CurrencyService) servletContext.getAttribute("currencyService");
        this.currencyMapper = (CurrencyMapper) servletContext.getAttribute("currencyMapper");
        this.gson = (Gson) servletContext.getAttribute("gson");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String requestURI = req.getRequestURI();
            if (requestURI.endsWith(CURRENCIES_PATH)) {
                handleCurrencyListRequest(resp);
            } else if (requestURI.endsWith(CURRENCIES_PATH + "/")) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Код валюты отсутствует в адресе");
            } else {
                handleSingleCurrencyRequest(req, resp);
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Произошла ошибка, попробуйте выполнить запрос позднее");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            var currencyDto = new CurrencyDto();
            currencyDto.setCode(req.getParameter("code"));
            currencyDto.setSign(req.getParameter("sign"));
            currencyDto.setFullname(req.getParameter("name"));
            var currency = currencyMapper.toModel(currencyDto);
            var currencyWithId = currencyService.save(currency);
            resp.sendRedirect(req.getRequestURI()+ "/" + currencyWithId.getCode());

        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,  e.getMessage() + "Произошла ошибка, попробуйте выполнить запрос позднее");
        }

    }

    private void handleCurrencyListRequest(HttpServletResponse resp) throws IOException {
        try (var writer = resp.getWriter()) {
            var currencies = currencyService.findAll();
            var json = gson.toJson(currencyMapper.collectionToDto(currencies));
            writer.write(json);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private void handleSingleCurrencyRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try (var writer = resp.getWriter()) {
            String code = req.getRequestURI().substring(req.getRequestURI().lastIndexOf("/") + 1);
            Optional<Currency> currencyOptional = currencyService.findByCode(code);
            if (currencyOptional.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Валюта с указанным кодом не найдена: " + code);
                return;
            }
            var json = gson.toJson(currencyMapper.toDto(currencyOptional.get()));
            writer.write(json);
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }
}