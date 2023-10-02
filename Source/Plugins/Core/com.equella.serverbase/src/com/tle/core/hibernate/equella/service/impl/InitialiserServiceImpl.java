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

package com.tle.core.hibernate.equella.service.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentCollectionConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedMapConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernatePersistentSortedSetConverter;
import com.thoughtworks.xstream.hibernate.converter.HibernateProxyConverter;
import com.thoughtworks.xstream.hibernate.mapper.HibernateMapper;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import com.tle.beans.IdCloneable;
import com.tle.common.security.streaming.XStreamSecurityManager;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.AbstractHibernateDao;
import com.tle.core.hibernate.equella.service.FieldProperty;
import com.tle.core.hibernate.equella.service.InitialiserCallback;
import com.tle.core.hibernate.equella.service.InitialiserService;
import com.tle.core.hibernate.equella.service.MethodProperty;
import com.tle.core.hibernate.equella.service.Property;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Function;
import javax.inject.Singleton;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Id;
import org.hibernate.annotations.AccessType;
import org.hibernate.proxy.HibernateProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

@Bind(InitialiserService.class)
@Singleton
public class InitialiserServiceImpl extends AbstractHibernateDao implements InitialiserService {
  private static final Logger LOGGER = LoggerFactory.getLogger(InitialiserServiceImpl.class);

  private final Map<Class<?>, CacheObject> cache = new HashMap<Class<?>, CacheObject>();

  @Override
  @SuppressWarnings("unchecked")
  public <T> T unwrapHibernate(T object) {
    if (object instanceof HibernateProxy) {
      object = (T) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
    }
    return object;
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.hibernate.equella.service.InitialiserService#initialise(java.lang.Object)
   */
  @Override
  public <T> T initialise(T object) {
    return initialise(
        object,
        new InitialiserCallback() {
          @Override
          public void set(Object obj, Property property, Object value) {
            property.set(obj, value);
          }

          @Override
          public void entitySimplified(Object old, Object newObj) {
            // nothing
          }
        });
  }

  /*
   * (non-Javadoc)
   * @see
   * com.tle.core.hibernate.equella.service.InitialiserService#evictFromSession(java.lang.Object
   * )
   */
  private void evictFromSession(Set<Object> os) {
    for (Object o : os) {
      try {
        getHibernateTemplate().evict(o);
      } catch (IllegalArgumentException iae) {
        if (iae.getMessage().startsWith("Non-entity object instance passed to evict")) {
          // This is being thrown in DefaultEvictEventListener.java:94
          // Does not appear to be a problem in this case, so trapping
          // and ignoring this specific case
          LOGGER.debug(
              "Ignoring error with type ["
                  + o.getClass().getCanonicalName()
                  + "]: "
                  + iae.getMessage());
        } else {
          throw iae;
        }
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T initialise(T object, InitialiserCallback callback) {
    if (object == null) {
      return null;
    } else {
      IdentityHashMap<Object, Object> map = new IdentityHashMap<Object, Object>();
      Map<Object, Object> evictees = new IdentityHashMap<Object, Object>();
      ArrayList<PropertySet> props = new ArrayList<PropertySet>();
      T newObj = (T) processObject(null, null, object, null, map, evictees, callback, props);
      evictFromSession(evictees.keySet());
      for (PropertySet set : props) {
        callback.set(set.object, set.property, set.value);
      }
      return newObj;
    }
  }

  private static class PropertySet {
    Object object;
    Property property;
    Object value;

    public PropertySet(Object object, Property property, Object value) {
      this.object = object;
      this.property = property;
      this.value = value;
    }
  }

  private Object processObject(
      Object parent,
      Property property,
      Object object,
      Class<?> declaredType,
      IdentityHashMap<Object, Object> map,
      Map<Object, Object> evictees,
      InitialiserCallback callback,
      List<PropertySet> propsToSet) {
    if (object == null) {
      return null;
    }

    Object newObject = object;

    if (map.containsKey(object)) {
      return map.get(object);
    }

    if (declaredType == null) {
      declaredType = object.getClass();
    }

    if (Map.class.isAssignableFrom(declaredType)) {
      return processMap(parent, property, (Map<?, ?>) object, map, evictees, callback, propsToSet);
    }

    if (Collection.class.isAssignableFrom(declaredType)) {
      return processCollection(
          parent, property, (Collection<?>) object, map, evictees, callback, propsToSet);
    }

    if (needsSimplify(parent, property, newObject)) {
      newObject = doSimplify(newObject, callback);
      return newObject;
    }

    if (newObject instanceof HibernateProxy) {
      newObject = unwrapHibernate(newObject);
    }

    map.put(object, newObject);
    List<Property> properties = getCacheObject(newObject.getClass()).getProperties();
    for (Property prop : properties) {
      Object value =
          processObject(
              newObject,
              prop,
              prop.get(newObject),
              prop.getReturnType(),
              map,
              evictees,
              callback,
              propsToSet);
      propsToSet.add(new PropertySet(object, prop, value));
    }
    evictees.put(object, object);
    return newObject;
  }

  private Object processMap(
      Object parent,
      Property property,
      Map<?, ?> mapObj,
      IdentityHashMap<Object, Object> map,
      Map<Object, Object> evictees,
      InitialiserCallback callback,
      List<PropertySet> propsToSet) {
    Map<Object, Object> newMap = getSupportedMap(mapObj);

    for (Map.Entry<?, ?> entry : mapObj.entrySet()) {
      Object newKey =
          processObject(
              parent, property, entry.getKey(), null, map, evictees, callback, propsToSet);
      Object newVal =
          processObject(
              parent, property, entry.getValue(), null, map, evictees, callback, propsToSet);
      newMap.put(newKey, newVal);
    }

    map.put(mapObj, newMap);
    return newMap;
  }

  private Object processCollection(
      Object parent,
      Property property,
      Collection<?> col,
      IdentityHashMap<Object, Object> map,
      Map<Object, Object> evictees,
      InitialiserCallback callback,
      List<PropertySet> propsToSet) {
    Collection<Object> newList = getSupportedCollection(col);

    for (Object colObj : col) {
      if (colObj != null) {
        Object newObj =
            processObject(parent, property, colObj, null, map, evictees, callback, propsToSet);
        newList.add(newObj);
      }
    }

    map.put(col, newList);
    return newList;
  }

  private Map<Object, Object> getSupportedMap(Map<?, ?> mapObj) {
    if (mapObj instanceof LinkedHashMap) {
      return new LinkedHashMap<Object, Object>();
    } else {
      return new HashMap<Object, Object>();
    }
  }

  private Object doSimplify(Object object, InitialiserCallback callback) {
    Class<?> persistent = getUnwrappedClass(object);
    CacheObject cacheObject = getCacheObject(persistent);
    Property idField = cacheObject.getIdField();
    if (idField != null) {
      try {
        Object newObject = persistent.getDeclaredConstructor().newInstance();
        Object id;
        if (object instanceof HibernateProxy) {
          id = ((HibernateProxy) object).getHibernateLazyInitializer().getIdentifier();
        } else {
          id = idField.get(object);
        }
        idField.set(newObject, id);
        callback.entitySimplified(object, newObject);
        return newObject;
      } catch (ReflectiveOperationException e) {
        throw new RuntimeException(e);
      }
    }
    return object;
  }

  private boolean needsSimplify(Object parent, Property property, Object thisOne) {
    if (parent == null || property == null || property.isDoNotSimplify()) {
      return false;
    } else {
      CacheObject cacheObject = getCacheObject(getUnwrappedClass(thisOne));
      return (cacheObject.isEntity()
          && !property.isCascade()
          && (property.isManyToMany() || property.isManyToOne()));
    }
  }

  private Class<?> getUnwrappedClass(Object thisOne) {
    if (thisOne instanceof HibernateProxy) {
      return ((HibernateProxy) thisOne).getHibernateLazyInitializer().getPersistentClass();
    } else {
      return thisOne.getClass();
    }
  }

  // Catering for numerous collection types, including the unloved Vector
  private Collection<Object> getSupportedCollection(Collection<?> col) {
    if (col instanceof Set) {
      return new HashSet<Object>();
    } else if (col instanceof Vector) // NOSONAR
    {
      return new Vector<Object>(); // NOSONAR
    } else {
      return new ArrayList<Object>();
    }
  }

  private CacheObject getCacheObject(Class<?> clazz) {
    synchronized (cache) {
      CacheObject o = cache.get(clazz);
      if (o == null) {
        o = new CacheObject(clazz);
        Class<?> parentClass = clazz.getSuperclass();
        if (parentClass != null) {
          CacheObject parentCache = getCacheObject(parentClass);
          o.addParent(parentCache);
        }
        cache.put(clazz, o);
      }
      return o;
    }
  }

  private static class CacheObject {
    private final List<Property> properties = new ArrayList<Property>();
    private Property id;
    private final boolean entity;

    public CacheObject(Class<?> clazz) {
      entity = isEntity(clazz);
      if (isProperty(clazz)) {
        for (PropertyDescriptor descriptor : BeanUtils.getPropertyDescriptors(clazz)) {
          Method readMethod = descriptor.getReadMethod();
          Method writeMethod = descriptor.getWriteMethod();
          if (readMethod != null && writeMethod != null) {
            if (readMethod.isAnnotationPresent(Id.class)) {
              id = new MethodProperty(descriptor);
            }
            Class<?> type = descriptor.getPropertyType();
            if (isComplexType(type)) {
              properties.add(new MethodProperty(descriptor));
            }
          }
        }
      } else {
        for (Field field : clazz.getDeclaredFields()) {
          boolean isStatic = Modifier.STATIC == (Modifier.STATIC & field.getModifiers());

          if (!isStatic && isComplexType(field.getType())) {
            properties.add(
                new FieldProperty(field, BeanUtils.getPropertyDescriptor(clazz, field.getName())));
          }
          if (field.isAnnotationPresent(Id.class)) {
            id = new FieldProperty(field, BeanUtils.getPropertyDescriptor(clazz, field.getName()));
          }
        }
      }
    }

    private boolean isComplexType(Class<?> type) {
      return type == Object.class
          || isEntity(type)
          || Collection.class.isAssignableFrom(type)
          || Map.class.isAssignableFrom(type);
    }

    private boolean isProperty(Class<?> clazz) {
      AccessType at = clazz.getAnnotation(AccessType.class);
      return at != null && at.value().equalsIgnoreCase("property"); // $NON-NLS-1$
    }

    private boolean isEntity(Class<?> clazz) {
      return clazz.isAnnotationPresent(Entity.class) || clazz.isAnnotationPresent(Embeddable.class);
    }

    public Property getIdField() {
      return id;
    }

    public List<Property> getProperties() {
      return properties;
    }

    public void addParent(CacheObject parent) {
      if (id == null) {
        id = parent.id;
      }
      properties.addAll(parent.properties);
    }

    public boolean isEntity() {
      return entity;
    }
  }

  @Override
  public void initialiseClones(Object object) {
    IdentityHashMap<Object, Object> map = new IdentityHashMap<Object, Object>();
    Map<Object, Object> evictees = new IdentityHashMap<Object, Object>();
    initialiseClones(object, map, evictees, null, null);
    evictFromSession(evictees.keySet());
  }

  private void initialiseClones(
      Object object,
      IdentityHashMap<Object, Object> previous,
      Map<Object, Object> evictees,
      Object parent,
      Property property) {
    if (object == null || previous.containsKey(object)) {
      return;
    }

    Class<? extends Object> clazz = object.getClass();
    if (clazz.isArray()
        || clazz.isPrimitive()
        || clazz.getPackage().getName().equals("java.lang")) // $NON-NLS-1$
    {
      return;
    }
    if (property != null
        && (property.isDoNotClone()
            || (getCacheObject(clazz).isEntity() && !(object instanceof IdCloneable)))) {
      return;
    }

    previous.put(object, object);

    if (object instanceof Collection) {
      Collection<?> col = (Collection<?>) object;
      for (Object colObj : col) {
        initialiseClones(unwrapHibernate(colObj), previous, evictees, parent, property);
      }
    } else if (object instanceof Map) {
      initialiseClones(((Map<?, ?>) object).keySet(), previous, evictees, parent, property);
      initialiseClones(((Map<?, ?>) object).values(), previous, evictees, parent, property);
    } else {
      List<Property> properties = getCacheObject(clazz).getProperties();
      for (Property childProp : properties) {
        initialiseClones(
            unwrapHibernate(childProp.get(object)), previous, evictees, object, childProp);
      }
    }

    evictees.put(object, object);
    if (object instanceof IdCloneable) {
      ((IdCloneable) object).setId(0);
    }
  }

  @Override
  public <T> Function<T, T> createCloner(ClassLoader classLoader) {
    final XStream xstream =
        new XStream() {
          @Override
          protected MapperWrapper wrapMapper(MapperWrapper next) {
            return new HibernateMapper(next);
          }
        };
    XStreamSecurityManager.applyPolicy(xstream);
    xstream.setClassLoader(classLoader);
    xstream.autodetectAnnotations(true);
    xstream.registerConverter(new HibernateProxyConverter());
    xstream.registerConverter(new HibernatePersistentCollectionConverter(xstream.getMapper()));
    xstream.registerConverter(new HibernatePersistentMapConverter(xstream.getMapper()));
    xstream.registerConverter(new HibernatePersistentSortedMapConverter(xstream.getMapper()));
    xstream.registerConverter(new HibernatePersistentSortedSetConverter(xstream.getMapper()));
    return t -> (T) xstream.fromXML(xstream.toXML(t));
  }
}
