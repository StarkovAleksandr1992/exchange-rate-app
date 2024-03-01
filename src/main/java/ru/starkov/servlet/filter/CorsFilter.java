package ru.starkov.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The CorsFilter class implements a servlet filter to handle Cross-Origin Resource Sharing (CORS)
 * requests. It intercepts incoming requests and adds appropriate CORS headers to allow
 * communication between different origins.
 */
@WebFilter(value = "/*")
public class CorsFilter implements Filter {

  private static final String[] allowedOrigins = {
      "http://localhost"
  };

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {

    var httpServletRequest = (HttpServletRequest) request;
    var requestOrigin = httpServletRequest.getHeader("Origin");
    if (isAllowedOrigin(requestOrigin)) {
      // Authorize the origin, all headers, and all methods
      ((HttpServletResponse) response).addHeader("Access-Control-Allow-Origin",
          requestOrigin);
      ((HttpServletResponse) response).addHeader("Access-Control-Allow-Headers", "*");
      ((HttpServletResponse) response).addHeader("Access-Control-Allow-Methods",
          "GET, OPTIONS, PUT, POST, PATCH");

      HttpServletResponse resp = (HttpServletResponse) response;

      // CORS handshake (pre-flight request)
      if (httpServletRequest.getMethod().equals("OPTIONS")) {
        resp.setStatus(HttpServletResponse.SC_ACCEPTED);
        return;
      }
    }
    // pass the request along the filter chain
    chain.doFilter(request, response);
  }

  private boolean isAllowedOrigin(String origin) {
    for (String allowedOrigin : allowedOrigins) {
      if (origin.equals(allowedOrigin)) {
        return true;
      }
    }
    return false;
  }
}
