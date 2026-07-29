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

package com.tle.core.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.converters.collections.MapConverter;
import com.thoughtworks.xstream.core.util.HierarchicalStreams;
import com.thoughtworks.xstream.io.ExtendedHierarchicalStreamWriterHelper;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.mapper.Mapper;
import java.util.Collections;
import java.util.Map;

/**
 * Reads and writes {@code Collections.singletonMap} in the shape oEQ has always persisted it: a
 * {@code k} element for the key and a {@code v} element for the value.
 *
 * <p>Those names came from the private fields of the JDK's {@code Collections$SingletonMap},
 * because this used to be a {@link
 * com.thoughtworks.xstream.converters.reflection.ReflectionConverter}. They are now simply the
 * format, written out explicitly, which buys two things: the format no longer depends on JDK
 * internals keeping those field names, and reading oEQ XML no longer requires {@code
 * --add-opens=java.base/java.util=ALL-UNNAMED} in whichever JVM happens to be doing it — so a plain
 * unit test, or any future embedding, can read it without privileged access.
 *
 * <p>Stock XStream writes singleton maps as {@code entry}/{@code key}/{@code value} instead, so it
 * cannot read anything oEQ has written. See {@link ExtXStream}.
 */
public class OldSingletonMapConverter extends MapConverter {

  private static final Class<?> MAP = Collections.singletonMap(Boolean.TRUE, null).getClass();

  private static final String KEY_NODE = "k";
  private static final String VALUE_NODE = "v";

  public OldSingletonMapConverter(Mapper mapper) {
    super(mapper);
  }

  @Override
  public boolean canConvert(Class type) {
    return MAP == type;
  }

  @Override
  public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
    Map.Entry<?, ?> entry = ((Map<?, ?>) source).entrySet().iterator().next();
    writeNamedItem(KEY_NODE, entry.getKey(), writer, context);
    writeNamedItem(VALUE_NODE, entry.getValue(), writer, context);
  }

  @Override
  public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
    Object key = null;
    Object value = null;

    // Read by name rather than by position: a null key or value is written as no element at all.
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      if (VALUE_NODE.equals(reader.getNodeName())) {
        value = readNamedItem(reader, context);
      } else {
        key = readNamedItem(reader, context);
      }
      reader.moveUp();
    }

    return Collections.singletonMap(key, value);
  }

  /**
   * Writes one item under the given element name, declaring its type the way the reflection
   * converter did — the key and value types both erase to {@code Object}, so the class attribute is
   * always needed to read the item back.
   */
  private void writeNamedItem(
      String name, Object item, HierarchicalStreamWriter writer, MarshallingContext context) {
    if (item == null) {
      return;
    }

    Class<?> itemType = item.getClass();
    ExtendedHierarchicalStreamWriterHelper.startNode(writer, name, itemType);

    String classAttribute = mapper().aliasForSystemAttribute("class");
    if (classAttribute != null && itemType != mapper().defaultImplementationOf(Object.class)) {
      writer.addAttribute(classAttribute, mapper().serializedClass(itemType));
    }

    context.convertAnother(item);
    writer.endNode();
  }

  private Object readNamedItem(HierarchicalStreamReader reader, UnmarshallingContext context) {
    return context.convertAnother(null, HierarchicalStreams.readClassType(reader, mapper()));
  }
}
