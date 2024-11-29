package com.tle.client.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.tle.admin.helper.ClientConfigurationHelper;
import com.tle.admin.service.AdminTLEUserService;
import com.tle.admin.service.AdminTLEUserServiceImpl;
import com.tle.common.applet.SessionHolder;
import com.tle.common.applet.client.ClientService;
import com.tle.core.remoting.RemoteTLEUserService;
import io.github.openequella.graphql.ClientConfiguration;
import javax.inject.Singleton;

public class ClientModule extends AbstractModule {
  final SessionHolder holder;
  final ClientService clientService;

  public ClientModule(SessionHolder holder, ClientService clientService) {
    this.holder = holder;
    this.clientService = clientService;
  }

  @Override
  protected void configure() {
    // Remember, Guice does not provide Spring like component scanning - you must do it all
    // yourself.
    // In the server code base we do have the ScannerModule which does something more like
    // component scanning, but it is not used in the client code base. And our list of classes
    // here will be straightforward, so we can just list them out.
    bind(AdminTLEUserService.class).to(AdminTLEUserServiceImpl.class);
  }

  @Provides
  @Singleton
  ClientConfiguration provideClientConfiguration() {
    // Set the client configuration for the GraphQL library
    ClientConfiguration clientConfiguration = ClientConfigurationHelper.create(holder.getUrl());
    ClientConfigurationHelper.loadSystemCookies(clientConfiguration);

    return clientConfiguration;
  }

  @Provides
  @Singleton
  RemoteTLEUserService provideRemoteTLEUserService() {
    // Make sure to use getInvokerService as getService will also end up calling this method.
    return clientService.getInvokerService(RemoteTLEUserService.class);
  }
}
