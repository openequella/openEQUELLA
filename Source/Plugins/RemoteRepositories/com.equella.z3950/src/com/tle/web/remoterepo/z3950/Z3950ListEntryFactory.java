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

package com.tle.web.remoterepo.z3950;

import com.google.inject.Provider;
import com.tle.core.guice.Bind;
import com.tle.core.remoterepo.z3950.Z3950SearchResult;
import com.tle.web.remoterepo.RemoteRepoListEntry;
import com.tle.web.remoterepo.RemoteRepoListEntryFactory;
import com.tle.web.sections.SectionInfo;
import javax.inject.Inject;
import javax.inject.Singleton;

@Bind
@Singleton
public class Z3950ListEntryFactory implements RemoteRepoListEntryFactory<Z3950SearchResult> {
  @Inject private Provider<Z3950ListEntry> entryProvider;

  @Override
  public RemoteRepoListEntry<Z3950SearchResult> createListEntry(
      SectionInfo info, Z3950SearchResult result) {
    Z3950ListEntry entry = entryProvider.get();
    entry.setResult(result);
    return entry;
  }
}
