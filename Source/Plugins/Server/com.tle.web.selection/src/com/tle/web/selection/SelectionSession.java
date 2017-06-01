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

package com.tle.web.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tle.beans.item.ItemId;
import com.tle.beans.item.VersionSelection;
import com.tle.web.sections.equella.ModalSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;

public class SelectionSession extends ModalSession
{
	private static final long serialVersionUID = 1L;

	public static final String KEY_QUERY_STRING = "query_string"; //$NON-NLS-1$

	private Set<String> collectionUuids = Collections.emptySet();
	private Set<String> powerSearchIds = Collections.emptySet();
	private Set<String> contributionCollectionIds = Collections.emptySet();
	private Set<String> dynamicCollectionIds = Collections.emptySet();
	private Set<String> remoteRepositoryIds = Collections.emptySet();
	private boolean allCollections;
	private boolean allPowerSearches;
	private boolean allContributionCollections;
	private boolean allDynamicCollections = true;
	private boolean allRemoteRepositories = true;
	private VersionSelection overrideVersionSelection;
	private boolean useDownloadPrivilege;

	private boolean selectMultiple = true;
	private boolean skipCheckoutPage = false;
	private boolean cancelDisabled = false;
	private boolean attachmentUuidUrls = false;

	private boolean selectAttachments = true;
	private boolean selectItem = true;
	private boolean selectPackage = true;
	private boolean addToRecentSelections = true;
	private boolean selectDraft = false;
	private boolean selectScrapbook = false;

	private String homeSelectable;
	private Set<String> allowedSelectNavActions;
	private String titleKey;

	private Set<String> mimeTypes = Collections.emptySet();

	private final SelectionsMadeCallback selectionsMadeCallback;
	private final Map<String, Map<String, String[]>> searchContexts = new HashMap<String, Map<String, String[]>>();

	private String initialItemXml;
	private String initialPowerXml;

	private String defaultCollectionUuid;

	// Includes selected resources for normal sessions (at the root)
	private final TargetStructure structure = new TargetStructure();
	private String targetFolder;

	private boolean forcePost;
	private Layout layout = Layout.NORMAL;

	public SelectionSession(SelectionsMadeCallback selectionsMadeCallback)
	{
		super(selectionsMadeCallback);
		this.selectionsMadeCallback = selectionsMadeCallback;
	}

	public boolean isSelectMultiple()
	{
		return selectMultiple;
	}

	public void setSelectMultiple(boolean selectMultiple)
	{
		this.selectMultiple = selectMultiple;
	}

	public boolean isSkipCheckoutPage()
	{
		return skipCheckoutPage;
	}

	public void setSkipCheckoutPage(boolean skipCheckoutPage)
	{
		this.skipCheckoutPage = skipCheckoutPage;
	}

	public boolean isCancelDisabled()
	{
		return cancelDisabled;
	}

	public void setCancelDisabled(boolean cancelDisabled)
	{
		this.cancelDisabled = cancelDisabled;
	}

	public boolean isAttachmentUuidUrls()
	{
		return attachmentUuidUrls;
	}

	public void setAttachmentUuidUrls(boolean attachmentUuidUrls)
	{
		this.attachmentUuidUrls = attachmentUuidUrls;
	}

	public Collection<SelectedResource> getSelectedResources()
	{
		return structure.getAllResources().values();
	}

	public Collection<SelectedResourceKey> getSelectedResourceKeys()
	{
		return structure.getAllResources().keySet();
	}

	public boolean isAllCollections()
	{
		return allCollections;
	}

	public void setAllCollections(boolean allCollections)
	{
		this.allCollections = allCollections;
	}

	public boolean isAllPowerSearches()
	{
		return allPowerSearches;
	}

	public void setAllPowerSearches(boolean allPowerSearches)
	{
		this.allPowerSearches = allPowerSearches;
	}

	public boolean isAllContributionCollections()
	{
		return allContributionCollections;
	}

	public void setAllContributionCollections(boolean allContributionCollections)
	{
		this.allContributionCollections = allContributionCollections;
	}

	public boolean isSelectItem()
	{
		return selectItem;
	}

	public void setSelectItem(boolean selectItem)
	{
		this.selectItem = selectItem;
	}

	public boolean isSelectAttachments()
	{
		return selectAttachments;
	}

	public void setSelectAttachments(boolean selectAttachments)
	{
		this.selectAttachments = selectAttachments;
	}

	public boolean isSelectPackage()
	{
		return selectPackage;
	}

	public void setSelectPackage(boolean selectPackage)
	{
		this.selectPackage = selectPackage;
	}

	public void addResource(SelectedResource resource)
	{
		addResource(resource, structure);
	}

	public void addResource(SelectedResource resource, TargetFolder folder)
	{
		resource.getKey().setFolderId(folder.getId());
		folder.addResource(resource);
	}

	public SelectedResource getResource(SelectedResourceKey key)
	{
		return structure.getAllResources().get(key);
	}

	public boolean containsResource(SelectedResourceKey key, boolean anywhere)
	{
		final Map<SelectedResourceKey, SelectedResource> allResources = structure.getAllResources();
		for( SelectedResourceKey selectedKey : allResources.keySet() )
		{
			if( (anywhere && selectedKey.equalsExceptFolder(key)) || selectedKey.equals(key) )
			{
				return true;
			}
		}
		return false;
	}

	public void clearResources()
	{
		structure.clearAllResources();
	}

	public Set<SelectedResourceKey> findAllForItem(ItemId itemId)
	{
		int version = itemId.getVersion();
		String uuid = itemId.getUuid();
		Set<SelectedResourceKey> forItem = new HashSet<SelectedResourceKey>();
		for( SelectedResourceKey key : getSelectedResourceKeys() )
		{
			if( key.getUuid().equals(uuid) && key.getVersion() == version )
			{
				forItem.add(key);
			}
		}
		return forItem;
	}

	public List<SelectedResourceDetails> getSelectionDetails()
	{
		List<SelectedResourceDetails> details = new ArrayList<SelectedResourceDetails>();
		for( SelectedResource selResource : getSelectedResources() )
		{
			details.add(new SelectedResourceDetails(selResource));
		}
		return details;
	}

	public SelectionsMadeCallback getSelectionsMadeCallback()
	{
		return selectionsMadeCallback;
	}

	public void setSearchContext(String key, Map<String, String[]> searchContext)
	{
		searchContexts.put(key, searchContext);
	}

	public Map<String, String[]> getSearchContext(String key)
	{
		return searchContexts.get(key);
	}

	public void setUseDownloadPrivilege(boolean useDownloadPrivilege)
	{
		this.useDownloadPrivilege = useDownloadPrivilege;
	}

	public boolean isUseDownloadPrivilege()
	{
		return useDownloadPrivilege;
	}

	public String getInitialItemXml()
	{
		return initialItemXml;
	}

	public void setInitialItemXml(String initialItemXml)
	{
		this.initialItemXml = initialItemXml;
	}

	public String getInitialPowerXml()
	{
		return initialPowerXml;
	}

	public void setInitialPowerXml(String initialPowerXml)
	{
		this.initialPowerXml = initialPowerXml;
	}

	/**
	 * The target course and it's folders in the LMS. Used by CourseListSection
	 * This also stores the SelectedResources for normal selection sessions.
	 * 
	 * @return
	 */
	public TargetStructure getStructure()
	{
		return structure;
	}

	public String getHomeSelectable()
	{
		return homeSelectable;
	}

	public void setHomeSelectable(String homeSelectable)
	{
		this.homeSelectable = homeSelectable;
	}

	public Set<String> getAllowedSelectNavActions()
	{
		return allowedSelectNavActions;
	}

	public void setAllowedSelectNavActions(Set<String> allowedSelectNavActions)
	{
		this.allowedSelectNavActions = allowedSelectNavActions;
	}

	public Set<String> getCollectionUuids()
	{
		return collectionUuids;
	}

	public void setCollectionUuids(Set<String> collectionIds)
	{
		this.collectionUuids = collectionIds;
	}

	public Set<String> getPowerSearchIds()
	{
		return powerSearchIds;
	}

	public void setPowerSearchIds(Set<String> powerSearchIds)
	{
		this.powerSearchIds = powerSearchIds;
	}

	public Set<String> getContributionCollectionIds()
	{
		return contributionCollectionIds;
	}

	public void setContributionCollectionIds(Set<String> contributionCollectionIds)
	{
		this.contributionCollectionIds = contributionCollectionIds;
	}

	public Set<String> getDynamicCollectionIds()
	{
		return dynamicCollectionIds;
	}

	public void setDynamicCollectionIds(Set<String> dynamicCollectionIds)
	{
		this.dynamicCollectionIds = dynamicCollectionIds;
	}

	public boolean isAllDynamicCollections()
	{
		return allDynamicCollections;
	}

	public void setAllDynamicCollections(boolean allDynamicCollections)
	{
		this.allDynamicCollections = allDynamicCollections;
	}

	public Set<String> getRemoteRepositoryIds()
	{
		return remoteRepositoryIds;
	}

	public void setRemoteRepositoryIds(Set<String> remoteRepositoryIds)
	{
		this.remoteRepositoryIds = remoteRepositoryIds;
	}

	public boolean isAllRemoteRepositories()
	{
		return allRemoteRepositories;
	}

	public void setAllRemoteRepositories(boolean allRemoteRepositories)
	{
		this.allRemoteRepositories = allRemoteRepositories;
	}

	public String getTitleKey()
	{
		return titleKey;
	}

	public void setTitleKey(String titleKey)
	{
		this.titleKey = titleKey;
	}

	public boolean isAddToRecentSelections()
	{
		return addToRecentSelections;
	}

	public void setAddToRecentSelections(boolean addToRecentSelections)
	{
		this.addToRecentSelections = addToRecentSelections;
	}

	public Set<String> getMimeTypes()
	{
		return mimeTypes;
	}

	public void setMimeTypes(Set<String> mimeTypes)
	{
		this.mimeTypes = mimeTypes;
	}

	public boolean isForcePost()
	{
		return forcePost;
	}

	public void setForcePost(boolean forcePost)
	{
		this.forcePost = forcePost;
	}

	public Layout getLayout()
	{
		return layout;
	}

	public void setLayout(Layout layout)
	{
		this.layout = layout;
	}

	public VersionSelection getOverrideVersionSelection()
	{
		return overrideVersionSelection;
	}

	public void setOverrideVersionSelection(VersionSelection overrideVersionSelection)
	{
		this.overrideVersionSelection = overrideVersionSelection;
	}

	public String getTargetFolder()
	{
		return targetFolder;
	}

	public void setTargetFolder(String targetFolder)
	{
		this.targetFolder = targetFolder;
	}

	public boolean isSelectDraft()
	{
		return selectDraft;
	}

	public void setSelectDraft(boolean selectDraft)
	{
		this.selectDraft = selectDraft;
	}

	public boolean isSelectScrapbook()
	{
		return selectScrapbook;
	}

	public void setSelectScrapbook(boolean selectScrapbook)
	{
		this.selectScrapbook = selectScrapbook;
	}

	public String getDefaultCollectionUuid()
	{
		return defaultCollectionUuid;
	}

	public void setDefaultCollectionUuid(String defaultCollectionUuid)
	{
		this.defaultCollectionUuid = defaultCollectionUuid;
	}
}
