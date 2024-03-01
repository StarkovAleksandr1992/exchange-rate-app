package ru.starkov.servlet.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The FilterLogic functional interface defines a contract for filter logic to be applied within a
 * servlet filter. It provides a single method, {@code apply}, which takes HttpServletRequest,
 * HttpServletResponse, and FilterChain parameters and throws IOException and ServletException.
 */
@FunctionalInterface
public interface FilterLogic {

  /**
   * Applies the filter logic to the given HttpServletRequest and HttpServletResponse.
   *
   * @param request  the HttpServletRequest to be filtered
   * @param response the HttpServletResponse to be filtered
   * @param chain    the FilterChain to invoke the next filter in the chain
   * @throws IOException      if an I/O error occurs during the filtering process
   * @throws ServletException if a servlet error occurs during the filtering process
   */
  void apply(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws IOException, ServletException;
}
