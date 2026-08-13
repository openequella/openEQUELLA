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

package com.tle.client.harness;

import com.tle.admin.PluginServiceImpl;
import com.tle.common.applet.client.ClientService;
import java.net.URL;
import java.util.Locale;

public interface HarnessInterface {
  /**
   * Method called after successful login result in JSESSIONID being set in the system cookie store.
   */
  void start();

  void setLocale(Locale locale);

  /** The endpoint for the openEQUELA server which has already been authenticated against. */
  void setEndpointURL(URL url);

  void setPluginService(PluginServiceImpl pluginService);

  /** Set a pre-configured client service instance. */
  void setClientService(ClientService clientService);
}
