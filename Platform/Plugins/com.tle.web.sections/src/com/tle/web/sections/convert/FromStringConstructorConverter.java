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

package com.tle.web.sections.convert;

import com.google.common.base.Throwables;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractConverter;
import net.entropysoft.transmorph.type.TypeReference;

public class FromStringConstructorConverter extends AbstractConverter {
  private final Map<Class<?>, Constructor<?>> constructors =
      Collections.synchronizedMap(new HashMap<Class<?>, Constructor<?>>());

  @Override
  protected boolean canHandleDestinationType(TypeReference<?> destinationType) {
    Class<?> rawType = destinationType.getRawType();
    if (!constructors.containsKey(rawType)) {
      try {
        constructors.put(rawType, rawType.getConstructor(String.class));
      } catch (NoSuchMethodException e) {
        constructors.put(rawType, null);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return constructors.get(rawType) != null;
  }

  @Override
  protected boolean canHandleSourceObject(Object sourceObject) {
    return sourceObject instanceof String;
  }

  @Override
  public Object doConvert(
      ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
      throws ConverterException {
    Constructor<?> cons = constructors.get(destinationType.getRawType());
    try {
      return cons.newInstance(sourceObject);
    } catch (InvocationTargetException t) {
      if (t.getTargetException() instanceof ConvertedToNull) {
        return null;
      }
      throw Throwables.propagate(t.getTargetException());
    } catch (Exception e) {
      throw Throwables.propagate(e);
    }
  }
}
