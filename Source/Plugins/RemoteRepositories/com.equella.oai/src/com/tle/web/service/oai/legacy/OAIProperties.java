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

package com.tle.web.service.oai.legacy;

import ORG.oclc.oai.server.catalog.AbstractCatalog;
import com.tle.common.Utils;
import com.tle.common.settings.standard.MailSettings;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.settings.service.ConfigurationService;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Named;

@Deprecated
@Bind
public class OAIProperties {
  private final Properties properties;

  @Inject private ConfigurationService configConstants;
  @Inject private InstitutionService institutionService;

  @Inject
  public OAIProperties(@Named("oaiLegacyProps") Properties properties) throws Throwable {
    this.properties = new OAIExtendedProperties(properties);
    AbstractCatalog.factory(this.properties, null);
  }

  // Unfortunately we can't extend OAIProperties from Properties
  // or otherwise spring autowire will go 'spack'
  private class OAIExtendedProperties extends Properties {
    private static final long serialVersionUID = 1L;

    public OAIExtendedProperties(Properties p) {
      super(p);
    }

    @Override
    public synchronized String getProperty(String key) {
      if ("Identify.adminEmail".equals(key)) // $NON-NLS-1$
      {
        return Utils.ent(configConstants.getProperties(new MailSettings()).getSender());
      } else if ("OAIHandler.baseURL".equals(key)) // $NON-NLS-1$
      {
        return institutionService.getInstitutionUrl() + "oai"; // $NON-NLS-1$
      }
      return super.getProperty(key);
    }
  }

  public Properties getProperties() {
    return properties;
  }
}
