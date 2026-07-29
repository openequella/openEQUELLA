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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.ClassLoaderReference;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.security.WildcardTypePermission;
import com.tle.common.security.streaming.XStreamSecurityManager;

/**
 * The canonical oEQ XStream configuration for XML that outlives a single process.
 *
 * <p>Anything read from or written to oEQ persistence must use this class, not a plain {@link
 * XStream}: the {@code xstream_immutable} entity blobs (wizard pages, summary display templates,
 * ...), institution export/import, and the opaque XML blobs carried by the GraphQL API. The format
 * predates the XStream defaults, so a stock instance cannot read it — most visibly {@link
 * OldSingletonMapConverter}'s {@code k}/{@code v} shape for {@code Collections.singletonMap}, which
 * stock XStream writes as {@code entry}/{@code key}/{@code value} instead.
 *
 * <p><b>Both ends of a wire must use this class.</b> When the Admin Console read GraphQL wizard
 * blobs with a stock instance instead, every collection holding a single-locale language bundle
 * failed to open with a {@code ConversionException} on {@code .../strings/k}.
 */
public class ExtXStream extends XStream {

  /**
   * DRM pages serialise an anonymous {@code Comparator} (see {@code DRMPage.NetworkSet}), and
   * wildcard permissions exclude anonymous types unless asked for them explicitly.
   */
  private static final WildcardTypePermission DRM_PAGE_PERMISSION =
      new WildcardTypePermission(true, new String[] {"com.dytech.edge.wizard.beans.DRMPage**"});

  public ExtXStream(ClassLoader loader) {
    super(
        null,
        new XppDriver(),
        loader != null ? loader : new ClassLoaderReference(new CompositeClassLoader()));
    autodetectAnnotations(true);
    registerConverter(new OldSingletonMapConverter(getMapper(), getReflectionProvider()));
    registerConverter(new OldSqlTimestampConverter());
    XStreamSecurityManager.applyPolicy(this);
    addPermission(DRM_PAGE_PERMISSION);
  }

  @Override
  protected boolean useXStream11XmlFriendlyMapper() {
    return true;
  }
}
