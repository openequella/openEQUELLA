/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.xml.service.impl;

import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.mapper.Mapper;

import java.util.Collections;

public class OldSingletonMapConverter extends ReflectionConverter {

    private static final Class MAP = Collections.singletonMap(Boolean.TRUE, null).getClass();

    public OldSingletonMapConverter(Mapper mapper, ReflectionProvider provider) {
        super(mapper, provider);
    }

    public boolean canConvert(Class type) {
        return MAP == type;
    }
}
