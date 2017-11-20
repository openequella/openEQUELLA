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

package com.tle.web.remoterepo.service;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.web.sections.SectionInfo;

/**
 * To leverage this service your Remote Repo search tree must have the
 * RootRemoteRepoSection as the root section.
 * 
 * @author aholland
 */
public interface RemoteRepoWebService
{
	void forwardToWizard(SectionInfo info, PropBagEx xml, FederatedSearch search);

	void forwardToWizard(SectionInfo info, StagingFile staging, PropBagEx xml, FederatedSearch search);

	void forwardToSearch(SectionInfo info, FederatedSearch search, boolean clearContext);

	FederatedSearch getRemoteRepository(SectionInfo info);

	/**
	 * Generally, you shouldn't need this...
	 * 
	 * @param info
	 * @param repositoryUuid
	 */
	void setRemoteRepository(SectionInfo info, String searchUuid);

	/**
	 * Gets display text based on the display transform (if appropriate) of the
	 * search
	 * 
	 * @param search
	 * @param xml
	 * @return Returns null if there is no associated display transform (or the
	 *         search type doesn't support one)
	 */
	String getDisplayText(FederatedSearch search, PropBagEx xml);
}
