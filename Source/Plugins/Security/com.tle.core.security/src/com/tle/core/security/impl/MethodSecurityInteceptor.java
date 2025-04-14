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

@Bind
@Singleton
public class MethodSecurityInteceptor implements MethodInterceptor {
  @Inject private SecurityAttributeSource attributeSource;
  @Inject private TLEAclManager tleAclManager;

  // Sonar may not like 'throws Throwable' declarations, but we're bound by
  // the external jar's interface
  @Override
  public Object invoke(MethodInvocation invocation) throws Throwable // NOSONAR
      {
    Class<?> targetClass = (invocation.getThis() != null ? invocation.getThis().getClass() : null);

    final SecurityAttribute secAttr =
        attributeSource.getAttribute(invocation.getMethod(), targetClass);
    if (CurrentUser.getUserState().isSystem()) {
      return invocation.proceed();
    }
    if (secAttr.isSystemOnly()) {
      throw new AccessDeniedException("You must be the system administrator"); // $NON-NLS-1$
    }
    Object domainObj = null;
    Set<String> onCallPrivs = secAttr.getOnCallPrivileges();
    if (!Check.isEmpty(onCallPrivs) && secAttr.getOnCallmode() != null) {
      switch (secAttr.getOnCallmode()) {
        case DOMAIN:
          domainObj = invocation.getArguments()[secAttr.getDomainArg()]; // NOSONAR
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
    if (secAttr.isFilterMatching()) {
      return filterResult(invocation, secAttr);
    }
    return invocation.proceed();
  }

  private void checkSingleObject(Set<String> privs, Object domainObj) {
    if (tleAclManager.filterNonGrantedObjects(privs, Collections.singleton(domainObj)).isEmpty()) {
      throwAccessDenied(privs);
    }
  }

  @SuppressWarnings("unchecked")
  private Object filterResult(MethodInvocation invocation, final SecurityAttribute secAttr)
      throws Throwable // NOSONAR
      {
    Object returnedObject = invocation.proceed();
    if (returnedObject == null) {
      return null;
    }

    if (returnedObject instanceof Collection) {
      return filterCollection(secAttr, (Collection<Object>) returnedObject);
    } else if (returnedObject instanceof Map) {
      return filterMap(secAttr, (Map<Object, Object>) returnedObject);
    } else {
      checkSingleObject(secAttr.getFilterPrivileges(), returnedObject);
      return returnedObject;
    }
  }

  private void throwAccessDenied(Set<String> privsList) {
    StringBuilder privs = new StringBuilder();
    for (String priv : privsList) {
      if (privs.length() > 0) {
        privs.append(", "); // $NON-NLS-1$
      }
      privs.append(priv);
    }
    throw new AccessDeniedException(
        "You do not have the required privileges to access this object [" //$NON-NLS-1$
            + privs.toString()
            + "]"); //$NON-NLS-1$
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
    Map<Object, Object> newMap = new HashMap<Object, Object>();
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
