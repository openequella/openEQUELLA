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

package com.tle.web.sections.equella.search;

import javax.inject.Inject;

import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.services.user.UserPreferenceService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.render.SettingsRenderer;
import com.tle.web.sections.equella.search.event.AbstractSearchEvent;
import com.tle.web.sections.equella.search.event.AbstractSearchResultsEvent;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.equella.search.event.SearchResultsListener;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Pager;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;

@TreeIndexed
public class PagingSection<E extends AbstractSearchEvent<E>, RE extends AbstractSearchResultsEvent<RE>>
	extends
		AbstractPrototypeSection<PagingSection.PagingModel>
	implements
		SearchEventListener<E>,
		SearchResultsListener<RE>,
		BlueBarEventListener
{

	@PlugKey("search.resultsshowing")
	private static String KEY_SHOWING;
	@PlugKey("pager.topbar.label")
	private static Label LABEL_PERPAGE;
	@PlugKey("pager.topbar.label.searchattachment")
	private static Label LABEL_ATTACHMENT_SEARCH;
	@PlugKey("pager.options.")
	protected static String PER_PAGE_PFX;

	@Component
	private Checkbox attachmentSearch;
	@Component(parameter = "page")
	private Pager pager;
	@Component(name = "pp", stateful = true)
	private SingleSelectionList<PerPageOption> perPage;
	private boolean editPerPage = true;
	private int maxPagesShown = 10;
	private int defaultPerPage = 10;
	private boolean searchAttachments = false;

	@Inject
	private UserPreferenceService userPrefs;
	@Inject
	private ConfigurationService configConstants;

	private boolean renderScreenOptions = true;

	public void setRenderScreenOptions(boolean renderScreenOptions)
	{
		this.renderScreenOptions = renderScreenOptions;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		perPage.setListModel(getPerPageListModel());
		perPage.setAlwaysSelect(true);
	}

	protected SimpleHtmlListModel<PerPageOption> getPerPageListModel()
	{
		return new EnumListModel<PerPageOption>(PER_PAGE_PFX, true, PerPageOption.values());
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new PagingModel();
	}

	public static class PagingModel
	{
		private AbstractSearchResultsEvent<?> resultsEvent;
		private boolean galleryOptions;
		private boolean videoGallery;

		public boolean isGalleryOptions()
		{
			return galleryOptions;
		}

		public void setGalleryOptions(boolean galleryOptions)
		{
			this.galleryOptions = galleryOptions;
		}

		public AbstractSearchResultsEvent<?> getResultsEvent()
		{
			return resultsEvent;
		}

		public void setResultsEvent(AbstractSearchResultsEvent<?> resultsEvent)
		{
			this.resultsEvent = resultsEvent;
		}

		public boolean isVideoGallery()
		{
			return videoGallery;
		}

		public void setVideoGallery(boolean videoGallery)
		{
			this.videoGallery = videoGallery;
		}
	}

	public Label getResultsText(SectionInfo info)
	{
		AbstractSearchResultsEvent<?> event = getModel(info).getResultsEvent();
		if( event != null )
		{
			return new KeyLabel(KEY_SHOWING, event.getOffset() + 1, event.getCount() + event.getOffset(),
				event.getMaximumResults());
		}
		return null;
	}

	@Override
	public void prepareSearch(SectionInfo info, E event) throws Exception
	{
		int currentPage = getPager().getCurrentPage(info);
		if( currentPage <= 0 )
		{
			currentPage = 1;
		}
		int page = getPerPage(info);
		event.setOffset((currentPage - 1) * page);
		event.setCount(page);
	}

	@Override
	public void processResults(SectionInfo info, RE results)
	{
		int maximumResults = results.getMaximumResults();

		int page = getPerPage(info);
		Pager pgr = getPager();
		if( pgr.getCurrentPage(info) <= 0 )
		{
			pgr.setCurrentPage(info, 1);
		}
		pgr.setup(info, ((maximumResults - 1) / page) + 1, maxPagesShown);
		getModel(info).setResultsEvent(results);
	}

	protected int getPerPage(SectionInfo info)
	{
		if( editPerPage )
		{
			PerPageOption val = perPage.getSelectedValue(info);
			if( getModel(info).isGalleryOptions() )
			{
				return val.gallery;
			}
			else if( getModel(info).isVideoGallery() )
			{
				return val.video;
			}
			else
			{
				return val.standard;
			}

		}
		return defaultPerPage;
	}

	@SuppressWarnings("nls")
	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		if( !renderScreenOptions )
		{
			return;
		}

		if( userPrefs.isSearchAttachment() )
		{
			attachmentSearch.setChecked(context, true);
		}

		event.addScreenOptions(new SettingsRenderer(LABEL_PERPAGE, renderSection(context, perPage), "screen-option"));
		SearchSettings searchingSettings = getSearchSettings();
		if( isSearchAttachments() && searchingSettings.getAttachmentBoost() != 0 )
		{
			event.addScreenOptions(new SettingsRenderer(LABEL_ATTACHMENT_SEARCH,
				renderSection(context, attachmentSearch), "screen-option"));
		}
	}

	private SearchSettings getSearchSettings()
	{
		return configConstants.getProperties(new SearchSettings());
	}

	public Pager getPager()
	{
		return pager;
	}

	public void resetToFirst(SectionInfo info)
	{
		getPager().setCurrentPage(info, 1);
	}

	public interface PerPageCallback
	{
		int getPerPage(SectionInfo info);
	}

	public void setMaxPagesShown(int maxPagesShown)
	{
		this.maxPagesShown = maxPagesShown;
	}

	public int getDefaultPerPage()
	{
		return defaultPerPage;
	}

	public void setDefaultPerPage(int defaultPerPage)
	{
		this.defaultPerPage = defaultPerPage;
	}

	public int getMaxPagesShown()
	{
		return maxPagesShown;
	}

	public boolean isResultsAvailable(SectionInfo info)
	{
		final AbstractSearchResultsEvent<?> resultsEvent = getModel(info).getResultsEvent();
		return resultsEvent != null && resultsEvent.getCount() > 0;
	}

	public void setChangeHandlers(JSCallable pagechange, JSCallable perPageCall, JSCallable checkBoxCall)
	{
		getPager().setEventHandler(JSHandler.EVENT_CHANGE, new StatementHandler(pagechange));
		perPage.setEventHandler(JSHandler.EVENT_CHANGE, new StatementHandler(perPageCall));
		SearchSettings searchingSettings = getSearchSettings();
		if( searchingSettings.getAttachmentBoost() != 0 )
		{
			attachmentSearch.setEventHandler(JSHandler.EVENT_CHANGE, new StatementHandler(checkBoxCall));
		}
	}

	public void toggleDisabled(SectionInfo info)
	{
		if( userPrefs.isSearchAttachment() )
		{
			userPrefs.setSearchAttachment(false);
		}
		else
		{
			userPrefs.setSearchAttachment(true);
		}
	}

	public boolean isEditPerPage()
	{
		return editPerPage;
	}

	public void setEditPerPage(boolean editPerPage)
	{
		this.editPerPage = editPerPage;
	}

	public void setIsGalleryOptions(SectionInfo info, boolean galleryOptions)
	{
		getModel(info).setGalleryOptions(galleryOptions);
	}

	public void setIsVideoGallery(SectionInfo info, boolean videoGallery)
	{
		getModel(info).setVideoGallery(videoGallery);
	}

	public boolean isSearchAttachments()
	{
		return searchAttachments;
	}

	public void setSearchAttachments(boolean searchAttachments)
	{
		this.searchAttachments = searchAttachments;
	}

	public SingleSelectionList<PerPageOption> getPerPage()
	{
		return perPage;
	}

	protected static enum PerPageOption
	{
		MIN(10, 30, 30), MIDDLE(50, 60, 60), MAX(100, 90, 90);

		public int standard;
		public int gallery;
		public int video;

		private PerPageOption(int standard, int gallery, int video)
		{
			this.standard = standard;
			this.gallery = gallery;
			this.video = video;
		}
	}
}
