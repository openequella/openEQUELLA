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

package com.tle.core.connectors.migration.v50;

import com.tle.beans.Institution;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.connectors.entity.Connector;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateCreationFilter;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.hibernate.impl.TablesOnlyFilter;
import com.tle.core.migration.AbstractCreateMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;

@SuppressWarnings("nls")
@Bind
@Singleton
public class CreateConnectorEntities extends AbstractCreateMigration {
  private PluginTracker<Object> connectorTracker;

  @Override
  protected HibernateCreationFilter getFilter(HibernateMigrationHelper helper) {
    Set<String> tables = new HashSet<String>();
    tables.add("connector");
    List<Extension> extensions = connectorTracker.getExtensions();
    for (Extension ext : extensions) {
      Collection<Parameter> tableParams = ext.getParameters("table");
      for (Parameter param : tableParams) {
        tables.add(param.valueAsString());
      }
    }
    return new TablesOnlyFilter(tables.toArray(new String[tables.size()]));
  }

  @SuppressWarnings("unchecked")
  @Override
  protected Class<?>[] getDomainClasses() {
    Set<Class<?>> domainClasses = new HashSet<Class<?>>();
    Collections.addAll(
        domainClasses,
        Connector.class,
        BaseEntity.class,
        BaseEntity.Attribute.class,
        LanguageBundle.class,
        Institution.class,
        LanguageString.class);
    List<Extension> extensions = connectorTracker.getExtensions();
    for (Extension ext : extensions) {
      Collection<Parameter> params = ext.getParameters("domainClass");
      for (Parameter param : params) {
        domainClasses.add(connectorTracker.getClassForName(ext, param.valueAsString()));
      }
    }

    return domainClasses.toArray(new Class<?>[domainClasses.size()]);
  }

  @Override
  public MigrationInfo createMigrationInfo() {
    return new MigrationInfo("com.tle.core.connectors.migration.connectorentities.title");
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    connectorTracker =
        new PluginTracker<Object>(
            pluginService, "com.tle.core.connectors", "connectorSchema", null);
  }
}
