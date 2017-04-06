package com.tle.web.remoterepo.service;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.entity.FederatedSearch;
import com.tle.core.filesystem.StagingFile;
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
