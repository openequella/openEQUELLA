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

package com.tle.client.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.tle.admin.helper.ClientConfigurationHelper;
import com.tle.admin.helper.RestConfigurationHelper;
import com.tle.admin.rest.RestConfiguration;
import com.tle.admin.service.AdminBaseEntityService;
import com.tle.admin.service.AdminBaseEntityServiceImpl;
import com.tle.admin.service.AdminKeepAliveService;
import com.tle.admin.service.AdminKeepAliveServiceImpl;
import com.tle.admin.service.AdminLoginService;
import com.tle.admin.service.AdminLoginServiceImpl;
import com.tle.admin.service.AdminSchemaService;
import com.tle.admin.service.AdminSchemaServiceImpl;
import com.tle.admin.service.AdminTLEGroupService;
import com.tle.admin.service.AdminTLEGroupServiceImpl;
import com.tle.admin.service.AdminTLEUserService;
import com.tle.admin.service.AdminTLEUserServiceImpl;
import com.tle.common.applet.client.ClientService;
import io.github.openequella.graphql.ClientConfiguration;
import java.net.URL;
import javax.inject.Singleton;

public class ClientModule extends AbstractModule {
  final URL serverUrl;
  final ClientService clientService;

  public ClientModule(URL serverUrl, ClientService clientService) {
    this.serverUrl = serverUrl;
    this.clientService = clientService;
  }

  @Override
  protected void configure() {
    // Remember, Guice does not provide Spring like component scanning - you must do it all
    // yourself.
    // In the server code base we do have the ScannerModule which does something more like
    // component scanning, but it is not used in the client code base. And our list of classes
    // here will be straightforward, so we can just list them out.
    bind(AdminBaseEntityService.class).to(AdminBaseEntityServiceImpl.class);
    bind(AdminKeepAliveService.class).to(AdminKeepAliveServiceImpl.class);
    bind(AdminLoginService.class).to(AdminLoginServiceImpl.class);
    bind(AdminSchemaService.class).to(AdminSchemaServiceImpl.class);
    bind(AdminTLEGroupService.class).to(AdminTLEGroupServiceImpl.class);
    bind(AdminTLEUserService.class).to(AdminTLEUserServiceImpl.class);
  }

  @Provides
  @Singleton
  ClientConfiguration provideClientConfiguration() {
    return ClientConfigurationHelper.create(serverUrl);
  }

  @Provides
  @Singleton
  RestConfiguration provideRestConfiguration() {
    return RestConfigurationHelper.create(serverUrl);
  }
}
