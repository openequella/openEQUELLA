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

package com.tle.web.htmleditor.tinymce.addon.tle;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.edge.common.ScriptContext;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.selection.BeanLookupSelectionCallback;
import com.tle.web.selection.SelectableInterface;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.SelectionsMadeCallback;

/**
 * Base class for all tinymce addons that spawn a selection session.
 * 
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractSelectionAddon extends AbstractTleTinyMceAddOn
{
	public static final String SESSION_ID = "$SESSION_ID$";
	public static final String PAGE_ID = "$PAGE_ID$";

	@Inject
	private SelectionService selectionService;

	@Override
	public void preRender(PreRenderContext context)
	{
		// Nothing
	}

	@Override
	public SectionResult execute(SectionInfo info, String action, String sessionId, String pageId,
		String tinyMceBaseUrl, boolean restrictedCollections, @Nullable Set<String> collectionUuids,
		boolean restrictedDynacolls, @Nullable Set<String> dynaCollUuids, boolean restrictedSearches,
		@Nullable Set<String> searchUuids, boolean restrictedContributables, @Nullable Set<String> contributableUuids)
	{
		SelectionSession session = new SelectionSession(new BeanLookupSelectionCallback(getCallbackClass()));

		session.setAttribute(SESSION_ID, sessionId);
		session.setAttribute(PAGE_ID, pageId);

		if( restrictedCollections )
		{
			session.setAllCollections(false);
			session.setCollectionUuids(collectionUuids != null ? collectionUuids : new HashSet<String>());

			// remote repos carried along in shadow of collections
			session.setAllRemoteRepositories(false);
			session.setRemoteRepositoryIds(new HashSet<String>());
		}
		else
		{
			session.setAllCollections(true);
			session.setAllRemoteRepositories(true);
		}

		if( restrictedDynacolls )
		{
			session.setAllDynamicCollections(false);
			session.setDynamicCollectionIds(dynaCollUuids != null ? dynaCollUuids : new HashSet<String>());
		}
		else
		{
			session.setAllDynamicCollections(true);
		}

		if( restrictedSearches )
		{
			session.setAllPowerSearches(false);
			session.setPowerSearchIds(searchUuids != null ? searchUuids : new HashSet<String>());
		}
		else
		{
			session.setAllPowerSearches(true);
		}

		if( restrictedContributables )
		{
			session.setAllContributionCollections(false);
			// Session doesn't like null Sets
			session.setContributionCollectionIds(contributableUuids != null ? contributableUuids
				: new HashSet<String>());
		}
		else
		{
			session.setAllContributionCollections(true);
		}

		session.setSelectDraft(true);
		session.setSelectAttachments(true);
		session.setSelectItem(true);
		session.setSelectPackage(true);
		session.setSelectMultiple(false);
		session.setAddToRecentSelections(isAddToRecentSelections());
		session.setTitleKey(getResourceHelper().key("sessiontitle." + getId()));
		selectionService.forwardToNewSession(info, session, getSelectable(info, session));
		return null;
	}

	protected abstract boolean isAddToRecentSelections();

	/**
	 * Add/remove selectables, banned types and other selection session
	 * initialisation
	 * 
	 * @param info
	 * @param session
	 */
	protected abstract SelectableInterface getSelectable(SectionInfo info, SelectionSession session);

	/**
	 * When the selection session is finished, it will create an instance of
	 * this class and invoke the execute method on it.
	 * 
	 * @return The callback class type
	 */
	protected abstract Class<? extends SelectionsMadeCallback> getCallbackClass();

	@Override
	public ObjectExpression getInitialisation(RenderContext context)
	{
		return null;
	}

	@Override
	@Nullable
	public String getResourcesUrl()
	{
		return null;
	}

	@Override
	public void setScriptContext(SectionInfo info, ScriptContext scriptContext)
	{
		// Who cares?
	}
}
