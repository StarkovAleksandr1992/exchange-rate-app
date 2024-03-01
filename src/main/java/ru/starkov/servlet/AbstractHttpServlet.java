package ru.starkov.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;

/**
 * An abstract servlet providing common functionality for HTTP servlets. Extends
 * {@link HttpServlet}. Intended for initializing fields used in servlet subclasses.
 */
@WebServlet
public abstract class AbstractHttpServlet extends HttpServlet {
  protected static final String DEFAULT_UNKNOWN_ERROR_MESSAGE =
      "Error occurred, please try again later";
  protected Gson gson;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    var servletContext = config.getServletContext();
    this.gson = (Gson) servletContext.getAttribute(Gson.class.getName());
  }
}
