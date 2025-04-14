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

package com.tle.web.remoterepo.merlot;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.search.MerlotSettings;
import com.tle.beans.search.SearchSettings;
import com.tle.core.guice.Bind;
import com.tle.web.remoterepo.RemoteRepoSearch;
import com.tle.web.remoterepo.RemoteRepoSection;
import com.tle.web.sections.SectionInfo;
import javax.inject.Singleton;

@SuppressWarnings("nls")
@Bind
@Singleton
public class MerlotRemoteRepoSearch implements RemoteRepoSearch {
  @Override
  public void forward(SectionInfo info, FederatedSearch search) {
    SectionInfo forward = info.createForward("/access/merlot.do");

    RemoteRepoSection results = forward.lookupSection(RemoteRepoSection.class);
    results.setSearchUuid(forward, search.getUuid());

    info.forwardAsBookmark(forward);
  }

  @Override
  public SearchSettings createSettings(FederatedSearch search) {
    MerlotSettings settings = new MerlotSettings();
    settings.load(search);
    return settings;
  }

  @Override
  public String getContextKey() {
    return MerlotRootRemoteRepoSection.CONTEXT_KEY;
  }
}
