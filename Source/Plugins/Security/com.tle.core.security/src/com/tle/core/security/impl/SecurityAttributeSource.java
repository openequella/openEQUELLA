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

  /**
   * Finds the security attribute for a given method and target class.
   *
   * <p>This method checks the annotations on the method to determine the security attributes
   * associated with it. If no relevant annotations are found, it returns null.
   *
   * <p>When building the security attribute, if the target method has been annotated with an
   * annotation that requires a domain object, it will determine which parameter of the method is
   * the domain object.
   *
   * @param method The method to check for security attributes.
   * @param targetClass The class that contains the method.
   * @return A SecurityAttribute object containing the security attributes, or null if none found.
   */
  private SecurityAttribute findSecurityAttribute(Method method, Class<?> targetClass) {
    SecurityAttribute attr = new SecurityAttribute();
    Annotation[] annotations = method.getAnnotations();
    boolean have = addAnnotationAttrs(annotations, attr, targetClass);

    if (!have) {
      return null;
    }
    if (DebugSettings.isDebuggingMode() && method.getDeclaringClass().isInterface()) {
      LOGGER.error("**************************************************");
      LOGGER.error("Please move these to the implementation:" + method);
      LOGGER.error("**************************************************");
    }
    if (attr.getOnCallMode() == OnCallMode.DOMAIN) {
      attr.setDomainArg(getDomainObjectParameter(method));
    }
    return attr;
  }

  /**
   * Adds security attributes based on the annotations present on the method.
   *
   * <p>This method iterates through the annotations and sets the appropriate security attributes
   * based on the type of annotation found. It supports various security annotations such as {@link
   * SecureOnCallSystem}, {@link SecureOnCall}, {@link SecureAllOnCall}, {@link SecureAllOnReturn},
   * {@link RequiresPrivilege}, {@link RequiresPrivilegeWithNoTarget}, and {@link SecureOnReturn}.
   *
   * @param annotations The array of annotations present on the method.
   * @param attr The SecurityAttribute object to populate with security attributes.
   * @param targetClass The class that contains the method.
   * @return true if any relevant security attributes were added, false otherwise.
   */
  private boolean addAnnotationAttrs(
      Annotation[] annotations, SecurityAttribute attr, Class<?> targetClass) {
    boolean have = false;
    for (Annotation annotation : annotations) {
      Class<? extends Annotation> anType = annotation.annotationType();
      if (anType == SecureOnCallSystem.class) {
        attr.setSystemOnly(true);
        have = true;
      } else if (anType == SecureOnCall.class) {
        setOnCallMode(attr, OnCallMode.DOMAIN);
        attr.addOnCallPrivilege(
            resolveVirtualBaseEntityPrivilege(((SecureOnCall) annotation).priv(), targetClass));
        have = true;
      } else if (anType == SecureAllOnCall.class) {
        addAnnotationAttrs(((SecureAllOnCall) annotation).value(), attr, targetClass);
        have = true;
      } else if (anType == SecureAllOnReturn.class) {
        addAnnotationAttrs(((SecureAllOnReturn) annotation).value(), attr, targetClass);
        have = true;
      } else if (anType == RequiresPrivilege.class) {
        setOnCallMode(attr, OnCallMode.ANY);
        attr.addOnCallPrivilege(
            resolveVirtualBaseEntityPrivilege(
                ((RequiresPrivilege) annotation).priv(), targetClass));
        have = true;
      } else if (anType == RequiresPrivilegeWithNoTarget.class) {
        attr.addOnCallPrivilege(
            resolveVirtualBaseEntityPrivilege(
                ((RequiresPrivilegeWithNoTarget) annotation).priv(), targetClass));
        setOnCallMode(attr, OnCallMode.TOPLEVEL);
        have = true;
      } else if (anType == SecureOnReturn.class) {
        attr.addFilterPrivilege(
            resolveVirtualBaseEntityPrivilege(((SecureOnReturn) annotation).priv(), targetClass));
        attr.setFilterMatching(true);
        have = true;
      }
    }
    return have;
  }

  private void setOnCallMode(SecurityAttribute attr, OnCallMode mode) {
    if (attr.getOnCallMode() == null) {
      attr.setOnCallMode(mode);
    } else if (attr.getOnCallMode() != mode) {
      throw new Error(
          "Can't mix RequiresPrivilege, RequiresPrivilegeWithNoTarget and"
              + " SecureOnCall"); //$NON-NLS-1$
    }
  }

  /**
   * Resolves the virtual base entity privilege by checking if the privilege ends with {@code
   * SecurityConstants.VIRTUAL_BASE_ENTITY}. If it does, it replaces it with the value specified in
   * the {@code SecureEntity} annotation of the target class.
   *
   * @param priv The privilege string to resolve.
   * @param targetClass The class that contains the SecureEntity annotation.
   * @return The resolved privilege string or the original privilege if no resolution is needed.
   */
  private String resolveVirtualBaseEntityPrivilege(String priv, Class<?> targetClass) {
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

  /**
   * Returns the index of the parameter that is a domain object. This is used to determine which
   * parameter to use for the domain object when checking privileges.
   *
   * <p>The determination as to which parameter is the domain object is based on the parameter types
   * of the method. It checks against a set of classes that are considered domain objects, which can
   * be extended via plugins.
   *
   * @param method The method to check.
   * @return The index of the parameter that is a domain object.
   */
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

  /**
   * Returns a collection of classes that are considered domain objects. This includes static
   * classes defined in the code and any additional classes defined by plugins.
   *
   * <p>This method is synchronized to ensure thread safety when building the collection of classes.
   *
   * @return A collection of classes that are domain objects.
   */
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
