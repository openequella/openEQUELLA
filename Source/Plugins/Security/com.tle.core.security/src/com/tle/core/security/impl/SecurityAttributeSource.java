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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.common.EntityPack;
import com.tle.common.security.SecurityConstants;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.security.impl.SecurityAttribute.OnCallMode;
import com.tle.web.DebugSettings;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.Extension.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.BridgeMethodResolver;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

@SuppressWarnings({"unchecked", "nls"})
@Bind
@Singleton
public class SecurityAttributeSource {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityAttributeSource.class);
  private static final SecurityAttribute NULL_SECURITY_ATTRIBUTE = new SecurityAttribute();

  @Inject
  @Named("domainParam")
  private PluginTracker<Object> domainParamTracker;

  private Set<Class<?>> classesToCheck;

  private static final Set<Class<?>> STATIC_CLASSES_TO_CHECK =
      ImmutableSet.of(
          BaseEntity.class, EntityPack.class, Item.class, ItemPack.class, ActivateRequest.class);

  final Map<CacheKey, SecurityAttribute> attributeCache = new ConcurrentHashMap<>(16);

  public SecurityAttribute getAttribute(Method method, Class<?> targetClass) {
    CacheKey cacheKey = new CacheKey(method, targetClass);
    SecurityAttribute cached = this.attributeCache.get(cacheKey);
    if (cached != null) {
      if (cached.equals(NULL_SECURITY_ATTRIBUTE)) {
        return null;
      }
      return cached;
    }
    SecurityAttribute secAtt = computeSecurityAttribute(method, targetClass);
    if (secAtt == null) {
      this.attributeCache.put(cacheKey, NULL_SECURITY_ATTRIBUTE);
    } else {
      this.attributeCache.put(cacheKey, secAtt);
    }
    return secAtt;
  }

  private SecurityAttribute computeSecurityAttribute(Method method, Class<?> targetClass) {
    // The method may be on an interface, but we need attributes from the
    // target class.
    //
    // If the target class is null, the method will be unchanged.
    //
    // If we are dealing with a method with generic parameters, this should find the
    // original method.
    Method specificMethod =
        BridgeMethodResolver.findBridgedMethod(
            ClassUtils.getMostSpecificMethod(method, targetClass));

    // First try is the method in the target class.
    SecurityAttribute txAtt = findSecurityAttribute(specificMethod, targetClass);
    if (txAtt != null) {
      return txAtt;
    }

    if (!specificMethod.equals(method)) {
      // Fallback is to look at the original method.
      txAtt = findSecurityAttribute(method, targetClass);
      if (txAtt != null) {
        return txAtt;
      }
    }
    return null;
  }

  private SecurityAttribute findSecurityAttribute(Method method, Class<?> targetClass) {
    SecurityAttribute attr = new SecurityAttribute();
    Annotation[] annotations = method.getAnnotations();
    boolean have = addAnnotationAttrs(annotations, attr, targetClass);

    if (!have) {
      return null;
    }
    if (DebugSettings.isDebuggingMode() && method.getDeclaringClass().isInterface()) {
      LOGGER.error("**************************************************"); // $NON-NLS-1$
      LOGGER.error("Please move these to the implementation:" + method); // $NON-NLS-1$
      LOGGER.error("**************************************************"); // $NON-NLS-1$
    }
    if (attr.getOnCallmode() == OnCallMode.DOMAIN) {
      attr.setDomainArg(getDomainObjectParameter(method));
    }
    return attr;
  }

  private boolean addAnnotationAttrs(
      Annotation[] annotations, SecurityAttribute attr, Class<?> targetClass) {
    boolean have = false;
    for (Annotation annotation : annotations) {
      Class<? extends Annotation> anType = annotation.annotationType();
      if (anType == SecureOnCallSystem.class) {
        attr.setSystemOnly(true);
        have = true;
      } else if (anType == SecureOnCall.class) {
        setOnCallmode(attr, OnCallMode.DOMAIN);
        attr.addOnCallPrivilege(resolvePrivilege(((SecureOnCall) annotation).priv(), targetClass));
        have = true;
      } else if (anType == SecureAllOnCall.class) {
        addAnnotationAttrs(((SecureAllOnCall) annotation).value(), attr, targetClass);
        have = true;
      } else if (anType == SecureAllOnReturn.class) {
        addAnnotationAttrs(((SecureAllOnReturn) annotation).value(), attr, targetClass);
        have = true;
      } else if (anType == RequiresPrivilege.class) {
        setOnCallmode(attr, OnCallMode.ANY);
        attr.addOnCallPrivilege(
            resolvePrivilege(((RequiresPrivilege) annotation).priv(), targetClass));
        have = true;
      } else if (anType == RequiresPrivilegeWithNoTarget.class) {
        attr.addOnCallPrivilege(
            resolvePrivilege(((RequiresPrivilegeWithNoTarget) annotation).priv(), targetClass));
        setOnCallmode(attr, OnCallMode.TOPLEVEL);
        have = true;
      } else if (anType == SecureOnReturn.class) {
        attr.addFilterPrivilege(
            resolvePrivilege(((SecureOnReturn) annotation).priv(), targetClass));
        attr.setFilterMatching(true);
        have = true;
      }
    }
    return have;
  }

  private void setOnCallmode(SecurityAttribute attr, OnCallMode mode) {
    if (attr.getOnCallmode() == null) {
      attr.setOnCallmode(mode);
    } else if (attr.getOnCallmode() != mode) {
      throw new Error(
          "Can't mix RequiresPrivilege, RequiresPrivilegeWithNoTarget and"
              + " SecureOnCall"); //$NON-NLS-1$
    }
  }

  private String resolvePrivilege(String priv, Class<?> targetClass) {
    if (priv.endsWith(SecurityConstants.VIRTUAL_BASE_ENTITY)) {
      SecureEntity annotation = targetClass.getAnnotation(SecureEntity.class);
      if (annotation == null) {
        throw new Error(targetClass.getName() + " needs a SecureEntity attribute!"); // $NON-NLS-1$
      }
      priv =
          priv.substring(0, priv.length() - SecurityConstants.VIRTUAL_BASE_ENTITY.length())
              + annotation.value();
    }
    return priv;
  }

  private int getDomainObjectParameter(Method method) {
    Class<?> params[] = method.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      for (Class<?> type : getClassesToCheck()) {
        if (type.isAssignableFrom(params[i])) {
          return i;
        }
      }
    }
    throw new Error("Could not find a domain object for:" + method); // $NON-NLS-1$
  }

  private synchronized Collection<Class<?>> getClassesToCheck() {
    if (classesToCheck == null) {
      Builder<Class<?>> builder = ImmutableSet.builder();
      builder.addAll(STATIC_CLASSES_TO_CHECK);
      List<Extension> extensions = domainParamTracker.getExtensions();
      for (Extension extension : extensions) {
        Collection<Parameter> clazzes = extension.getParameters("class");
        for (Parameter clazzParam : clazzes) {
          builder.add(domainParamTracker.getClassForName(extension, clazzParam.valueAsString()));
        }
      }
      classesToCheck = builder.build();
    }
    return classesToCheck;
  }

  private static class CacheKey {

    private final Method method;

    private final Class<?> targetClass;

    public CacheKey(Method method, Class<?> targetClass) {
      this.method = method;
      this.targetClass = targetClass;
    }

    @Override
    public boolean equals(Object other) {
      if (this == other) {
        return true;
      }
      if (!(other instanceof CacheKey)) {
        return false;
      }
      CacheKey otherKey = (CacheKey) other;
      return (this.method.equals(otherKey.method)
          && ObjectUtils.nullSafeEquals(this.targetClass, otherKey.targetClass));
    }

    @Override
    public int hashCode() {
      return this.method.hashCode() * 29
          + (this.targetClass != null ? this.targetClass.hashCode() : 0);
    }
  }
}
