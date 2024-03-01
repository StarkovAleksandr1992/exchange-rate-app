package ru.starkov.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * A filter for setting character encoding and content type for servlet responses.
 *
 * <p>This filter ensures that the character encoding is set to UTF-8 and the content type
 * is set to JSON for all servlet responses passing through it.
 */
@WebFilter(value = "/*")
public class EncodingAndContentTypeFilter implements Filter {

  private static final String CONTENT_TYPE = "application/json";
  private static final String CHARSET = "UTF-8";


  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    response.setCharacterEncoding(CHARSET);
    response.setContentType(CONTENT_TYPE);
    chain.doFilter(request, response);
  }
}
