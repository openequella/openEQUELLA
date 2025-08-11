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

package com.tle.core.security.impl;

import com.tle.common.Check;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Interceptor that checks the security attributes of a method which has been annotated with one of
 * the security annotations, such as those in the package com.tle.core.security.annotations.
 */
@Bind
@Singleton
public class MethodSecurityInterceptor implements MethodInterceptor {
  @Inject private SecurityAttributeSource attributeSource;
  @Inject private TLEAclManager tleAclManager;

  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable {
    final SecurityAttribute secAttr = getSecurityAttribute(invocation);

    if (CurrentUser.getUserState().isSystem()) {
      return invocation.proceed();
    }
    if (secAttr.isSystemOnly()) {
      throw new AccessDeniedException("You must be the system administrator");
    }

    enforceOnCallSecurity(invocation, secAttr);
    // Now enforce post call security
    if (secAttr.isFilterMatching()) {
      return filterResult(invocation, secAttr);
    }

    return invocation.proceed();
  }

  private SecurityAttribute getSecurityAttribute(MethodInvocation invocation) {
    Class<?> targetClass = (invocation.getThis() != null ? invocation.getThis().getClass() : null);

    return attributeSource.getAttribute(invocation.getMethod(), targetClass);
  }

  /**
   * Enforces the security attributes for on-call methods, checking if the user has the required
   * privileges based on the mode specified in the security attribute.
   *
   * @param invocation The method invocation context.
   * @param secAttr The security attribute containing the required privileges and mode.
   */
  private void enforceOnCallSecurity(MethodInvocation invocation, SecurityAttribute secAttr) {
    Object domainObj = null;
    Set<String> onCallPrivs = secAttr.getOnCallPrivileges();
    if (!Check.isEmpty(onCallPrivs) && secAttr.getOnCallMode() != null) {
      switch (secAttr.getOnCallMode()) {
        case DOMAIN:
          domainObj = invocation.getArguments()[secAttr.getDomainArg()];
        // Let it fall through
        case TOPLEVEL:
          if (tleAclManager.filterNonGrantedPrivileges(domainObj, onCallPrivs).isEmpty()) {
            throwAccessDenied(onCallPrivs);
          }
          break;
        case ANY:
          if (tleAclManager.filterNonGrantedPrivileges(onCallPrivs).isEmpty()) {
            throwAccessDenied(onCallPrivs);
          }
          break;
      }
    }
  }

  private void checkSingleObject(Set<String> privs, Object domainObj) {
    if (tleAclManager.filterNonGrantedObjects(privs, Collections.singleton(domainObj)).isEmpty()) {
      throwAccessDenied(privs);
    }
  }

  @SuppressWarnings("unchecked")
  private Object filterResult(MethodInvocation invocation, final SecurityAttribute secAttr)
      throws Throwable {
    Object returnedObject = invocation.proceed();
    switch (returnedObject) {
      case null -> {
        return null;
      }
      case Collection ignored -> {
        return filterCollection(secAttr, (Collection<Object>) returnedObject);
      }
      case Map ignored -> {
        return filterMap(secAttr, (Map<Object, Object>) returnedObject);
      }
      default -> {
        checkSingleObject(secAttr.getFilterPrivileges(), returnedObject);
        return returnedObject;
      }
    }
  }

  private void throwAccessDenied(Set<String> privsList) {
    StringBuilder privs = new StringBuilder();
    for (String priv : privsList) {
      if (!privs.isEmpty()) {
        privs.append(", ");
      }
      privs.append(priv);
    }
    throw new AccessDeniedException(
        "You do not have the required privileges to access this object [" + privs + "]");
  }

  private Collection<Object> filterCollection(
      SecurityAttribute secAttr, Collection<Object> returnedObject) {
    if (returnedObject.isEmpty()) {
      return returnedObject;
    }

    Set<String> privileges = secAttr.getFilterPrivileges();
    if (Check.isEmpty(privileges)) {
      return returnedObject;
    }

    return tleAclManager.filterNonGrantedObjects(privileges, returnedObject);
  }

  private Map<Object, Object> filterMap(SecurityAttribute config, Map<Object, Object> map) {
    Collection<Object> c = filterCollection(config, map.values());
    Map<Object, Object> newMap = new HashMap<>();
    for (Map.Entry<Object, Object> entry : map.entrySet()) {
      Object val = entry.getValue();
      if (c.contains(val)) {
        newMap.put(entry.getKey(), val);
      }
    }
    return newMap;
  }

  public SecurityAttributeSource getAttributeSource() {
    return attributeSource;
  }
}
