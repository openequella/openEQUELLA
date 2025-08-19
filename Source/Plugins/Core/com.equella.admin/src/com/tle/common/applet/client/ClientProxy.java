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

package com.tle.common.applet.client;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientProxy implements InvocationHandler {
  public static final Logger LOGGER = LoggerFactory.getLogger(ClientProxy.class);

  private final Object iface;

  public ClientProxy(Object iface) {
    this.iface = iface;
  }

  // SonarQube flags 'throws Throwable' as a code smell, but this is required for compatibility
  // with external proxy frameworks (e.g., CgLibProxy) that expect this signature
  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return invoke(proxy, method, args, true);
  }

  private Object invoke(Object proxy, Method method, Object[] args, boolean retry)
      throws Throwable {
    logInvocationDetails(method);

    String methodName = method.getName();
    Method pMethod = iface.getClass().getMethod(methodName, method.getParameterTypes());
    try {
      return pMethod.invoke(iface, args);
    } catch (InvocationTargetException e) {
      throw e.getCause();
    }
  }

  private static void logInvocationDetails(Method method) {
    if (LOGGER.isDebugEnabled()) {
      String argTypes =
          Arrays.stream(method.getParameterTypes())
              .map(Class::getName)
              .collect(Collectors.joining(", "));
      LOGGER.debug(
          "Remote call: {}.{}({})",
          method.getDeclaringClass().getName(),
          method.getName(),
          argTypes);
    }
  }
}
