/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.sections.events;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dytech.edge.exceptions.BadRequestException;
import com.tle.common.Check;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public class ParametersEvent extends AbstractSectionEvent<ParametersEventListener> {
  private final Map<String, String[]> parameters = new HashMap<String, String[]>();
  private boolean initial;

  public ParametersEvent(Map<String, String[]> initialParams, boolean initial) {
    if (initialParams != null) {
      parameters.putAll(initialParams);
    }
    this.initial = initial;
  }

  @Override
  public Class<ParametersEventListener> getListenerClass() {
    return ParametersEventListener.class;
  }

  public boolean hasParameter(String param) {
    String[] params = parameters.get(param);
    if (params != null && params.length > 0) {
      return true;
    }
    return false;
  }

  public Map<String, String[]> getParameterMap() {
    return parameters;
  }

  public String getParameter(String param, boolean mandatory) throws BadRequestException {
    String[] params = parameters.get(param);
    if (params != null && params.length > 0) {
      return params[0];
    }
    if (mandatory) {
      throw new BadRequestException(param);
    }
    return null;
  }

  public int getIntParameter(String param, boolean mandatory) throws BadRequestException {
    String val = getParameter(param, mandatory);
    if (val == null) {
      return 0;
    }
    try {
      return Integer.parseInt(val);
    } catch (NumberFormatException nfe) {
      throw new BadRequestException(param);
    }
  }

  public Date getDateParameter(String param, boolean mandatory) throws BadRequestException {
    String val = getParameter(param, mandatory);
    if (Check.isEmpty(val)) {
      return null;
    }
    try {
      return new Date(Long.parseLong(val));
    } catch (NumberFormatException nfe) {
      throw new BadRequestException(param);
    }
  }

  public boolean getBooleanParameter(String param, boolean mandatory) throws BadRequestException {
    String val = getParameter(param, mandatory);
    if (val == null) {
      return false;
    }
    return Boolean.parseBoolean(val);
  }

  public void parameterHandled(String key) {
    parameters.remove(key);
  }

  public Set<String> getParameterNames() {
    return parameters.keySet();
  }

  @Override
  public void fire(SectionId sectionId, SectionInfo info, ParametersEventListener listener)
      throws Exception {
    listener.handleParameters(info, this);
  }

  @Override
  public void beforeFiring(SectionInfo info, SectionTree tree) {
    super.beforeFiring(info, tree);
    if (parameters.isEmpty()) {
      stopProcessing();
    }
  }

  @Override
  public void finishedFiring(SectionInfo info, SectionTree tree) {
    info.processEvent(new AfterParametersEvent(this), tree);
  }

  public String[] getParameterValues(String param) {
    return parameters.get(param);
  }

  public boolean isInitial() {
    return initial;
  }

  public void setInitial(boolean initial) {
    this.initial = initial;
  }
}
