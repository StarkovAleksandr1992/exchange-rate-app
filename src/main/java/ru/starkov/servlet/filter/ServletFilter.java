package ru.starkov.servlet.filter;

import jakarta.servlet.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ServletFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding(StandardCharsets.UTF_8);
        response.setCharacterEncoding(StandardCharsets.UTF_8);
        response.setContentType("application/json");
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
