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

public class ProxySettings {
  private String host;
  private int port;
  private String username;
  private String password;

  public ProxySettings() {
    super();
  }

  /**
   * @return Returns the host.
   */
  public String getHost() {
    return host;
  }

  /**
   * @param host The host to set.
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * @return Returns the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password The password to set.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return Returns the port.
   */
  public int getPort() {
    return port;
  }

  /**
   * @param port The port to set.
   */
  public void setPort(int port) {
    this.port = port;
  }

  /**
   * @return Returns the username.
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username The username to set.
   */
  public void setUsername(String username) {
    this.username = username;
  }
}
