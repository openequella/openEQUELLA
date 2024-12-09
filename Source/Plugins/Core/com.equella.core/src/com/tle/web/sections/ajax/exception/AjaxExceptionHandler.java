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

package com.tle.web.sections.ajax.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.tle.common.Utils;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsController;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.errors.SectionsExceptionHandler;
import com.tle.web.sections.events.SectionEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Bind
@Singleton
public class AjaxExceptionHandler implements SectionsExceptionHandler {
  private static Log LOGGER = LogFactory.getLog(AjaxExceptionHandler.class);
  private static ObjectMapper mapper;

  static {
    mapper = new ObjectMapper();
  }

  @Override
  public boolean canHandle(SectionInfo info, Throwable ex, SectionEvent<?> event) {
    return (ex instanceof AjaxException);
  }

  @SuppressWarnings("nls")
  @Override
  public void handle(
      Throwable exception, SectionInfo info, SectionsController controller, SectionEvent<?> event) {
    LOGGER.error("Error during ajax request", exception);
    info.setRendered();
    final HttpServletResponse response = info.getResponse();
    if (!response.isCommitted()) {
      response.reset();
      response.setStatus(500);
      response.setHeader("Content-Type", "application/json");
      try {
        final Map<String, Object> message = new HashMap<String, Object>();
        final Throwable rootCause = Throwables.getRootCause(exception);
        final String errorMessage =
            Utils.coalesce(rootCause.getMessage(), rootCause.getClass().getCanonicalName());
        message.put("message", errorMessage);
        mapper.writeValue(response.getWriter(), message);
      } catch (IOException e) {
        throw new SectionsRuntimeException(e);
      }
    }
  }
}
