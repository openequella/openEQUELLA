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

package com.tle.integration.lti.generic;

import com.tle.common.externaltools.constants.ExternalToolConstants;
import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.guice.Bind;
import com.tle.web.lti.usermanagement.LtiWrapperExtension;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

@Bind
@Singleton
public class GenericLtiWrapperExtension implements LtiWrapperExtension {

  @Override
  public String getUserId(HttpServletRequest request, LtiConsumer consumer) {
    return getGenericLtiParam(
        request, consumer, LtiConsumer.ATT_CUSTOM_USER_ID, ExternalToolConstants.USER_ID);
  }

  @Override
  public String getUsername(HttpServletRequest request, LtiConsumer consumer) {
    return getGenericLtiParam(
        request,
        consumer,
        LtiConsumer.ATT_CUSTOM_USERNAME,
        ExternalToolConstants.LIS_PERSON_SOURCEDID);
  }

  /** This is the fallback to standard LIS params */
  @Override
  public String getFirstName(HttpServletRequest request) {
    return request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_GIVEN);
  }

  /** This is the fallback to standard LIS params */
  @Override
  public String getLastName(HttpServletRequest request) {
    return request.getParameter(ExternalToolConstants.LIS_PERSON_NAME_FAMILY);
  }

  /** This is the fallback to standard LIS params */
  @Override
  public String getEmail(HttpServletRequest request) {
    return request.getParameter(ExternalToolConstants.LIS_PERSON_CONTACT_EMAIL_PRIMARY);
  }

  /**
   * Default is true (prefix the user id with a hash of the consumer).
   *
   * <p>This can be overridden by configuring the custom LTI setting of 'Prefix ID'
   *
   * <p>Note: This is different then the username prefix that an LTI Consumer can be configured to
   * append.
   */
  @Override
  public boolean isPrefixUserId(LtiConsumer consumer) {
    if (consumer == null) {
      return true;
    }

    return consumer.getAttribute(LtiConsumer.ATT_CUSTOM_ENABLE_ID_PREFIX, true);
  }

  private String getGenericLtiParam(
      HttpServletRequest request, LtiConsumer consumer, String customParam, String defaultParam) {

    if ((request == null) || (consumer == null)) {
      return null;
    }

    String paramVal = request.getParameter(consumer.getAttribute(customParam));
    if (StringUtils.isEmpty(paramVal)) {
      paramVal = request.getParameter(defaultParam);
    }

    return paramVal;
  }
}
