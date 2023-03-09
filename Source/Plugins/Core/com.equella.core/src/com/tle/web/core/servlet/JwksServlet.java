package com.tle.web.core.servlet;

import com.tle.core.guice.Bind;
import com.tle.core.webkeyset.service.WebKeySetService;
import java.io.IOException;
import java.io.PrintWriter;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Bind
@Singleton
public class JwksServlet extends HttpServlet {
  @Inject WebKeySetService webKeySetService;

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.setContentType("application/json");

    PrintWriter out = resp.getWriter();
    out.print(webKeySetService.generateJWKS());
    out.flush();
  }
}
