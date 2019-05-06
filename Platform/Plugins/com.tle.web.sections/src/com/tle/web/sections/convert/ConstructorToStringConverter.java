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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.entropysoft.transmorph.ConversionContext;
import net.entropysoft.transmorph.ConverterException;
import net.entropysoft.transmorph.converters.AbstractConverter;
import net.entropysoft.transmorph.type.TypeReference;

public class ConstructorToStringConverter extends AbstractConverter {
  private final Set<Class<?>> allowedClasses = Collections.synchronizedSet(new HashSet<Class<?>>());
  private final Set<Class<?>> disallowedClasses =
      Collections.synchronizedSet(new HashSet<Class<?>>());

  @Override
  public Object doConvert(
      ConversionContext context, Object sourceObject, TypeReference<?> destinationType)
      throws ConverterException {
    if (sourceObject == null) {
      return null;
    }
    return sourceObject.toString();
  }

  @Override
  protected boolean canHandleDestinationType(TypeReference<?> destinationType) {
    return destinationType.isType(String.class);
  }

  @Override
  protected boolean canHandleSourceObject(Object sourceObject) {
    if (sourceObject == null) {
      return true;
    }
    Class<? extends Object> srcClass = sourceObject.getClass();
    if (allowedClasses.contains(srcClass)) {
      return true;
    }
    if (disallowedClasses.contains(srcClass)) {
      return false;
    }
    try {
      srcClass.getConstructor(String.class);
      allowedClasses.add(srcClass);
      return true;
    } catch (NoSuchMethodException e) {
      disallowedClasses.add(srcClass);
      return false;
    }
  }
}
