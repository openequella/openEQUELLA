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

package com.dytech.edge.installer;

import com.dytech.devlib.PropBagEx;
import com.dytech.installer.ForeignCommand;
import com.dytech.installer.InstallerException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class CacheConfig extends ForeignCommand {
  private String path;
  private String tle_host;
  private String tle_port;
  private String tle_username;
  private String tle_password;
  private String proxy_host;
  private String proxy_port;
  private String proxy_username;
  private String proxy_password;

  public CacheConfig(PropBagEx commandBag, PropBagEx resultBag) throws InstallerException {
    super(commandBag, resultBag);

    path = resultBag.getNode("install.path");

    tle_host = resultBag.getNode("tle/host");
    tle_port = resultBag.getNode("tle/port");
    tle_username = resultBag.getNode("tle/username");
    tle_password = resultBag.getNode("tle/password");

    proxy_host = resultBag.getNode("proxy/host");
    proxy_port = resultBag.getNode("proxy/port");
    proxy_username = resultBag.getNode("proxy/username");
    proxy_password = resultBag.getNode("proxy/password");
  }

  @Override
  public void execute() throws InstallerException {
    propogateTaskStarted(3);

    File f = new File(path + "/config.xml");
    PropBagEx config = new PropBagEx(f);

    propogateSubtaskCompleted();

    config.createNode("soapserver/host", tle_host);
    config.createNode("soapserver/port", tle_port);
    config.createNode("soapserver/username", tle_username);
    config.createNode("soapserver/password", tle_password);
    config.createNode("proxy/host", proxy_host);
    config.createNode("proxy/port", proxy_port);
    config.createNode("proxy/username", proxy_username);
    config.createNode("proxy/password", proxy_password);
    propogateSubtaskCompleted();

    try (BufferedWriter out = new BufferedWriter(new FileWriter(f))) {
      out.write(config.toString());
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    propogateTaskCompleted();
  }

  @Override
  public String toString() {
    return new String("Writing configuration...");
  }
}
