package com.tle.core.adminconsole;

import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.web.resources.AbstractResourcesServlet;
import java.io.IOException;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mapped to /institution_url/adminconsole.jar Basically this servlet exists because the console
 * bootstrapper needs a stable location to download from, whereas other resources include the
 * Equella version number in the URL
 */
@Bind
public class AdminConsoleDownloadServlet extends AbstractResourcesServlet {

  @Inject private PluginService pluginService;

  public AdminConsoleDownloadServlet() {
    isCalculateETag = true;
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // response.setContentType("application/java-archive");
    response.setHeader("Cache-Control", "private, max-age=5, must-revalidate");

    service(request, response, "adminconsole.jar", "application/java-archive");
  }

  @Override
  public String getRootPath() {
    return "web/";
  }

  @Override
  public String getPluginId(HttpServletRequest request) {
    return pluginService.getPluginIdForObject(AdminConsoleDownloadServlet.class);
  }
}
