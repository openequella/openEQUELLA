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

package com.tle.core.xml.service.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.ClassLoaderReference;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.security.WildcardTypePermission;
import com.tle.common.security.streaming.XStreamSecurityManager;
import com.tle.core.guice.Bind;
import com.tle.core.xml.service.XmlService;
import java.io.Reader;
import java.io.Writer;
import javax.inject.Singleton;

@Bind(XmlService.class)
@Singleton
public final class XmlServiceImpl implements XmlService {
  private final XStream xstream;
  private final WildcardTypePermission xstreamTypePermission;

  public XmlServiceImpl() {
    xstream = new ExtXStream(null);
    /** Allow all inner classes for the DRMPage class. */
    xstreamTypePermission =
        new WildcardTypePermission(true, new String[] {"com.dytech.edge.wizard.beans.DRMPage**"});
    xstream.addPermission(xstreamTypePermission);
  }

  @Override
  public String serialiseToXml(Object o) {
    if (o == null) {
      return null;
    } else {
      return xstream.toXML(o);
    }
  }

  @Override
  public XStream createDefault(ClassLoader loader) {
    XStream xs = new ExtXStream(loader);
    xs.addPermission(xstreamTypePermission);
    return xs;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialiseFromXml(ClassLoader loader, String xml) {
    Thread currentThread = Thread.currentThread();
    ClassLoader oldLoader = currentThread.getContextClassLoader();
    try {
      currentThread.setContextClassLoader(loader);
      if (xml == null) {
        return null;
      } else {
        return (T) xstream.fromXML(xml);
      }
    } finally {
      currentThread.setContextClassLoader(oldLoader);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T deserialiseFromXml(ClassLoader loader, Reader reader) {
    Thread currentThread = Thread.currentThread();
    ClassLoader oldLoader = currentThread.getContextClassLoader();
    try {
      currentThread.setContextClassLoader(loader);
      return (T) xstream.fromXML(reader);
    } finally {
      currentThread.setContextClassLoader(oldLoader);
    }
  }

  @Override
  public void serialiseToWriter(Object o, Writer writer) {
    xstream.toXML(o, writer);
  }

  public static class ExtXStream extends XStream {
    public ExtXStream(ClassLoader loader) {
      super(
          null,
          new XppDriver(),
          loader != null ? loader : new ClassLoaderReference(new CompositeClassLoader()));
      autodetectAnnotations(true);
      registerConverter(new OldSingletonMapConverter(getMapper(), getReflectionProvider()));
      registerConverter(new OldSqlTimestampConverter());
      XStreamSecurityManager.applyPolicy(this);
    }

    @Override
    protected boolean useXStream11XmlFriendlyMapper() {
      return true;
    }
  }
}
