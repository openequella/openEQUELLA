/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.endpoint.srw;

import ORG.oclc.os.SRW.SRWServletInfo;
import com.tle.core.guice.Bind;
import java.io.IOException;
import javax.inject.Singleton;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;

@Bind
@Singleton
public class SRWServletExt extends ORG.oclc.os.SRW.SRWServlet {
  private static final long serialVersionUID = 1L;

  private static final Logger LOGGER = Logger.getLogger(SRWServletExt.class);
  private static final String DB_NAME = "tle"; // $NON-NLS-1$

  @Override
  public void init() throws ServletException {
    Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
    final ServletConfig config = super.getServletConfig();
    super.init();

    // We do so we don't have specify a 'database' on the URL path
    // eg. http://localhost:8988/dev/first/srw/
    srwInfo =
        new SRWServletInfo() {
          @Override
          public String getDBName(HttpServletRequest request) {
            return DB_NAME;
          }
        };
    srwInfo.init(config);
    srwInfo
        .getProperties()
        .put("defaultSchema", EquellaSRWDatabase.DEFAULT_SCHEMA.getTleId()); // $NON-NLS-1$
    srwInfo
        .getProperties()
        .put(
            "db." + DB_NAME + ".class",
            EquellaSRWDatabase.class.getName()); // $NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void service(ServletRequest req, ServletResponse res)
      throws ServletException, IOException {
    ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      super.service(req, res);
    } catch (Exception e) {
      LOGGER.error("Error invoking SRU/SRW servlet", e);
    } finally {
      Thread.currentThread().setContextClassLoader(oldLoader);
    }
  }
}
