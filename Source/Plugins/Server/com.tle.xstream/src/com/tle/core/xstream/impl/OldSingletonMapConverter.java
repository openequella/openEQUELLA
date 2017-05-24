package com.tle.core.xstream.impl;

import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionConverter;
import com.thoughtworks.xstream.converters.reflection.ReflectionProvider;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
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
