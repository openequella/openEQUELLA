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

package com.tle.web.wizard.section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.item.ItemId;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.section.DuplicateDataSection.Model.DuplicateDataItem;
import com.tle.web.wizard.section.DuplicateDataSection.Model.DuplicateDataView;
import com.tle.web.wizard.section.model.DuplicateData;

public class DuplicateDataSection extends WizardSection<DuplicateDataSection.Model> implements SectionTabable
{
	private static final String PROP_NAME = "dups"; //$NON-NLS-1$

	@PlugKey("duplicatedatasection.pagename")
	private static Label LABEL_PAGENAME;

	@Inject
	private WizardService wizardService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ViewItemUrlFactory urlFactory;
	@Inject
	private ItemService itemService;

	@Component(stateful = false)
	private MappedBooleans duplicateCheckboxes;

	@Override
	public String getDefaultPropertyName()
	{
		return PROP_NAME;
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_AFTER_EVENTS)
	public void submit(SectionInfo info) throws Exception
	{
		Model model = getModel(info);
		if( model.isSubmit() )
		{
			model.setSubmit(false);
			WizardSectionInfo winfo = getWizardInfo(info);
			WizardState state = winfo.getWizardState();

			Map<String, DuplicateData> duplicates = state.getDuplicateData();
			for( DuplicateData data : duplicates.values() )
			{
				data.setAccepted(false);
			}

			Set<String> accepts = duplicateCheckboxes.getCheckedSet(info);
			for( String accepted : accepts )
			{
				duplicates.get(accepted).setAccepted(true);
			}

			// We need this here to reset saveable
			wizardService.checkPages(state);
		}
	}

	@SuppressWarnings("nls")
	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		WizardSectionInfo winfo = getWizardInfo(context);
		Model model = getModel(context);
		model.setSubmit(true);
		WizardState state = winfo.getWizardState();

		Map<String, DuplicateData> duplicated = state.getDuplicateData();

		Set<ItemId> allItemIds = new HashSet<ItemId>();
		List<DuplicateDataView> views = new ArrayList<DuplicateDataView>();

		boolean canAcceptAny = false;
		boolean mustChangeAny = false;
		for( Map.Entry<String, DuplicateData> entry : duplicated.entrySet() )
		{
			DuplicateData data = entry.getValue();

			if( data.isCanAccept() )
			{
				canAcceptAny = true;
			}
			else
			{
				mustChangeAny = true;
			}

			duplicateCheckboxes.setValue(context, entry.getKey(), data.isAccepted());
			views.add(new DuplicateDataView(data));

			allItemIds.addAll(data.getItems());
		}
		model.setDuplicateData(views);
		model.setCanAcceptAny(canAcceptAny);
		model.setMustChangeAny(mustChangeAny);

		Map<ItemId, LanguageBundle> itemNames = itemService.getItemNames(allItemIds);
		for( DuplicateDataView view : views )
		{
			List<DuplicateDataItem> nvs = new ArrayList<DuplicateDataItem>();
			List<ItemId> itemIds = view.getItemIds();
			for( ItemId itemId : itemIds )
			{
				LanguageBundle nameBundle = itemNames.get(itemId);
				BundleLabel title = new BundleLabel(nameBundle, itemId.getUuid(), bundleCache);
				HtmlLinkState link = new HtmlLinkState(title, urlFactory.createItemUrl(context, itemId));
				link.setTarget("_blank");
				nvs.add(new DuplicateDataItem(nameBundle, itemId.getUuid(), link));
			}
			view.setItems(nvs);
		}

		return viewFactory.createResult("wizard/duplicateData.ftl", context);
	}

	private boolean isInvalid(Collection<DuplicateData> duplicateURLs)
	{
		for( DuplicateData data : duplicateURLs )
		{
			if( data.isVisible() && !data.isAccepted() )
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void addTabs(SectionInfo info, List<SectionTab> tabs)
	{
		WizardSectionInfo winfo = getWizardInfo(info);
		Map<String, DuplicateData> du = winfo.getWizardState().getDuplicateData();
		if( !du.isEmpty() )
		{
			SectionTab tab = new SectionTab(this, LABEL_PAGENAME, ""); //$NON-NLS-1$
			tabs.add(tab);
			tab.setInvalid(isInvalid(du.values()));
		}
	}

	public HtmlBooleanState getCheckbox(SectionInfo info, String key)
	{
		return duplicateCheckboxes.getBooleanState(info, key);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	@Override
	public void setupShowingTab(SectionInfo info, SectionTab tab)
	{
		// nothing
	}

	public static class Model
	{
		private Collection<DuplicateDataView> duplicateData;
		@Bookmarked(stateful = false)
		private boolean submit;
		protected boolean canAcceptAny;
		protected boolean mustChangeAny;

		public void setDuplicateData(Collection<DuplicateDataView> duplicateData)
		{
			this.duplicateData = duplicateData;
		}

		public Collection<DuplicateDataView> getDuplicateData()
		{
			return duplicateData;
		}

		public boolean isCanAcceptAny()
		{
			return canAcceptAny;
		}

		public void setCanAcceptAny(boolean canAcceptAny)
		{
			this.canAcceptAny = canAcceptAny;
		}

		public boolean isMustChangeAny()
		{
			return mustChangeAny;
		}

		public void setMustChangeAny(boolean mustChangeAny)
		{
			this.mustChangeAny = mustChangeAny;
		}

		public static class DuplicateDataItem
		{
			private final LanguageBundle name;
			private final String uuid;
			private final HtmlLinkState link;

			public DuplicateDataItem(LanguageBundle name, String uuid, HtmlLinkState link)
			{
				this.name = name;
				this.uuid = uuid;
				this.link = link;
			}

			public LanguageBundle getName()
			{
				return name;
			}

			public String getUuid()
			{
				return uuid;
			}

			public HtmlLinkState getLink()
			{
				return link;
			}
		}

		public static class DuplicateDataView
		{
			private List<DuplicateDataItem> items;
			private DuplicateData dupe;

			public DuplicateDataView(DuplicateData data)
			{
				this.dupe = data;
			}

			public List<ItemId> getItemIds()
			{
				return dupe.getItems();
			}

			public List<DuplicateDataItem> getItems()
			{
				return items;
			}

			public void setItems(List<DuplicateDataItem> items)
			{
				this.items = items;
			}

			public boolean isVisible()
			{
				return dupe.isVisible();
			}

			public String getIdentifier()
			{
				return dupe.getIdentifier();
			}

			public String getValue()
			{
				return dupe.getValue();
			}

			public boolean isCanAccept()
			{
				return dupe.isCanAccept();
			}

		}

		public boolean isSubmit()
		{
			return submit;
		}

		public void setSubmit(boolean submit)
		{
			this.submit = submit;
		}
	}

	@Override
	public void leavingTab(SectionInfo info, SectionTab tab)
	{
		// nothing
	}

	@Override
	public void unfinishedTab(SectionInfo info, SectionTab tab)
	{
		// nothing
	}

}
