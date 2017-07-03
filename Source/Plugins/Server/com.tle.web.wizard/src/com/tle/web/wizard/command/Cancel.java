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

package com.tle.web.wizard.command;

import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.FederatedSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.impl.WizardCommand;
import com.tle.web.wizard.section.RootWizardSection;
import com.tle.web.wizard.section.WizardSectionInfo;
import com.tle.web.workflow.tasks.ModerationService;

@SuppressWarnings("nls")
public class Cancel extends WizardCommand
{
	static
	{
		PluginResourceHandler.init(Cancel.class);
	}

	@PlugKey("command.cancel.cancel")
	private static String KEY_CANCEL;
	@PlugKey("command.cancel.confirm")
	private static String KEY_CONFIRM;

	@Inject
	private ModerationService moderationService;
	@Inject
	private ViewItemUrlFactory viewItemUrl;
	@Inject
	private WizardService wizardService;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private FederatedSearchService federatedSearchService;
	@Inject
	private SelectionService selectionService;

	public Cancel()
	{
		super(KEY_CANCEL, "cancel");
	}

	@Override
	public String getWarning(SectionInfo info, WizardSectionInfo winfo)
	{
		WizardState state = winfo.getWizardState();
		return ((state.isLockedForEditing() || state.isNewItem()) ? KEY_CONFIRM : null);
	}

	@Override
	public boolean isEnabled(SectionInfo info, WizardSectionInfo winfo)
	{
		return !winfo.getWizardState().isNoCancel()
			&& (!moderationService.isModerating(info) || winfo.isLockedForEditing());
	}

	@Override
	public void execute(SectionInfo info, WizardSectionInfo winfo, String data) throws Exception
	{
		final WizardState state = winfo.getWizardState();
		final boolean locked = state.isLockedForEditing();

		winfo.cancelEdit();

		if( moderationService.isModerating(info) )
		{
			if( locked )
			{
				moderationService.setEditing(info, false);
			}
			wizardService.reload(state, false);
		}
		else
		{
			final RootWizardSection rootSection = info.lookupSection(RootWizardSection.class);
			rootSection.removeState(info, state);

			if( state.isEntryThroughEdit() )
			{
				// forward to viewing the item we are editing
				final ViewItemUrl vurl = viewItemUrl.createItemUrl(info, state.getItemId());
				vurl.forward(info);
			}
			else
			{
				if( forwardToContribute(info) )
				{
					// forward to contribute
					info.forwardAsBookmark(info.createForward("/access/contribute.do"));
				}
				else
				{
					if( selectionService.getCurrentSession(info) != null )
					{
						selectionService.forwardToSelectable(info, null);
					}
					else
					{
						info.forwardAsBookmark(info.createForward("/home.do"));
					}
				}
			}

		}
	}

	private List<ItemDefinition> enumerateCreatable(SectionInfo info)
	{
		List<ItemDefinition> results = itemDefinitionService.enumerateCreateable();

		SelectionSession css = selectionService.getCurrentSession(info);
		if( css != null && !css.isAllContributionCollections() )
		{
			selectionService.filterFullEntities(results, css.getContributionCollectionIds());
		}

		return results;
	}

	private boolean forwardToContribute(SectionInfo info)
	{

		List<ItemDefinition> contributableCollections = enumerateCreatable(info);
		boolean singleCol = contributableCollections.size() == 1;

		if( singleCol )
		{
			List<FederatedSearch> fedSearches = federatedSearchService
				.getForCollectionUuid(contributableCollections.get(0).getUuid());
			boolean isFedSearch = false;
			if( !Check.isEmpty(fedSearches) )
			{
				for( FederatedSearch federatedSearch : fedSearches )
				{
					if( !federatedSearch.isDisabled() )
					{
						isFedSearch = true;
						break;
					}
				}
			}
			if( !isFedSearch )
			{
				return false;
			}

		}
		return true;
	}
}
