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

package com.tle.web.service.oai;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import java.util.Properties;
import javax.inject.Named;

public class OAIModule extends AbstractModule {

  @Override
  protected void configure() {
    // provides methods
  }

  @SuppressWarnings("nls")
  @Provides
  @Named("oaiProps")
  Properties provideProperties() {
    Properties props = new Properties();
    props.put("AbstractCatalog.oaiCatalogClassName", OAICatalog.class.getName());
    props.put("AbstractCatalog.recordFactoryClassName", XMLRecordFactory.class.getName());
    props.put("Identify.repositoryName", "EQUELLA");
    props.put("AbstractCatalog.granularity", "YYYY-MM-DDThh:mm:ssZ");
    props.put("Identify.earliestDatestamp", "1998-01-01T00:00:00Z");
    props.put("Identify.deletedRecord", "transient");
    props.put("OAIHandler.urlEncodeSetSpec", "false");
    return props;
  }

  @SuppressWarnings({"nls", "deprecation"})
  @Provides
  @Named("oaiLegacyProps")
  Properties provideLegacyProperties() {
    Properties props = new Properties();
    props.put(
        "AbstractCatalog.oaiCatalogClassName",
        com.tle.web.service.oai.legacy.OAICatalog.class.getName());
    props.put(
        "AbstractCatalog.recordFactoryClassName",
        com.tle.web.service.oai.legacy.XMLRecordFactory.class.getName());
    props.put("Identify.repositoryName", "The Learning Edge");
    props.put("AbstractCatalog.granularity", "YYYY-MM-DDThh:mm:ssZ");
    props.put("Identify.earliestDatestamp", "1998-01-01T00:00:00Z");
    props.put("Identify.deletedRecord", "transient");
    return props;
  }
}
