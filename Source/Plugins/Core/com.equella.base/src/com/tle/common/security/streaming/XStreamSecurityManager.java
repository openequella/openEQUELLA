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

package com.tle.common.security.streaming;

import com.thoughtworks.xstream.XStream;

public final class XStreamSecurityManager {

  private XStreamSecurityManager() {
    throw new UnsupportedOperationException();
  }

  public static void applyPolicy(XStream xstream) {
    // Anything you want to be XStream'd needs to be allowed here
    xstream.allowTypesByWildcard(
        new String[] {
          "com.tle.**", "com.dytech.**",
        });
  }

  /**
   * A stock XStream carrying the oEQ type policy, for XML that begins and ends inside one process —
   * ad-hoc conversions, clipboard and file export from the Admin Console, and the like.
   *
   * <p>Prefer {@code com.tle.core.xstream.ExtXStream} for anything persisted or sent over a wire.
   * Entity blobs, institution export/import and the opaque blobs in the GraphQL API are in a legacy
   * format that only {@code ExtXStream} can read and write, and mixing the two is how every
   * collection with a single-locale language bundle stopped opening in the Admin Console.
   *
   * <p>Some persisted strings do still round-trip through this factory — the summary display
   * configuration read and written by {@code BasicConfig}, {@code DisplayNodesConfig} and {@code
   * ItemSummaryTemplateTab}, and read back by {@code ItemSummaryApi}. Those are safe because both
   * ends use this factory and the payloads are plain string maps with no language bundles in them,
   * not because the format is interchangeable. Anything richer belongs on {@code ExtXStream}.
   */
  public static XStream newXStream() {
    XStream xs = new XStream();
    applyPolicy(xs);
    return xs;
  }
}
