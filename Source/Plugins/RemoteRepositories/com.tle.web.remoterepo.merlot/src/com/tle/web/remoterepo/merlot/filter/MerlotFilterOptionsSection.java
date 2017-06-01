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

package com.tle.web.remoterepo.merlot.filter;

import java.util.Collection;

import javax.inject.Inject;

import com.tle.common.NameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remoterepo.merlot.MerlotFilterListModel;
import com.tle.web.remoterepo.merlot.MerlotFilterType;
import com.tle.web.remoterepo.merlot.MerlotRemoteRepoSearchEvent;
import com.tle.web.remoterepo.merlot.MerlotWebService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.listmodel.EnumListModel;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class MerlotFilterOptionsSection extends AbstractPrototypeSection<Object>
	implements
		HtmlRenderer,
		SearchEventListener<MerlotRemoteRepoSearchEvent>
{
	@Inject
	private MerlotWebService merlotWebService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	private enum MobileOperatingSystem
	{
		IOS, ANDROID, BLACKBERRY, WINDOWS
	}

	private enum MobileType
	{
		PHONE, TABLET, OTHER
	}

	@PlugKey("filter.mobile.")
	private static String KEY_MOBILE_FILTER_PREFIX;

	@Component(name = "l")
	private SingleSelectionList<NameValue> languages;
	@Component(name = "t")
	private SingleSelectionList<NameValue> technicalFormats;
	@Component(name = "a")
	private SingleSelectionList<NameValue> materialAudiences;
	@Component(name = "c")
	@PlugKey("filter.free.title")
	private Checkbox free;
	@Component(name = "cc")
	@PlugKey("filter.creativecommons.title")
	private Checkbox creativeCommons;
	@Component(name = "mos")
	private MultiSelectionList<MobileOperatingSystem> mobileOS;
	@Component(name = "mt")
	private MultiSelectionList<MobileType> mobileType;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.setLayout(id, SearchResultsActionsSection.AREA_FILTER);
		languages.setListModel(new MerlotFilterListModel(languageFilterType));
		technicalFormats.setListModel(new MerlotFilterListModel(technicalFormatFilterType));
		materialAudiences.setListModel(new MerlotFilterListModel(materialAudienceFilterType));
		mobileOS.setListModel(new EnumListModel<MobileOperatingSystem>(KEY_MOBILE_FILTER_PREFIX, true,
			MobileOperatingSystem.values()));
		mobileType.setListModel(new EnumListModel<MobileType>(KEY_MOBILE_FILTER_PREFIX, true, MobileType.values()));

	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !merlotWebService.getSettings(context).isAdvancedApi() )
		{
			return null;
		}
		// try to get the options. we may not be able to talk to MERLOT and we
		// want a proper error page,
		// not an embedded freemarker stack trace.
		languages.getListModel().getOptions(context);

		return viewFactory.createResult("filter/merlotfilteroptions.ftl", this);
	}

	@Override
	public void prepareSearch(SectionInfo info, MerlotRemoteRepoSearchEvent event) throws Exception
	{
		event.setLanguage(languages.getSelectedValueAsString(info));
		event.setTechnicalFormat(technicalFormats.getSelectedValueAsString(info));
		event.setMaterialAudience(materialAudiences.getSelectedValueAsString(info));
		event.setCost(!free.isChecked(info));
		event.setCreativeCommons(creativeCommons.isChecked(info));
		event.setMobileOS(mobileOS.getSelectedValuesAsStrings(info));
		event.setMobileType(mobileType.getSelectedValuesAsStrings(info));

	}

	public Checkbox getFree()
	{
		return free;
	}

	public Checkbox getCreativeCommons()
	{
		return creativeCommons;
	}

	public SingleSelectionList<NameValue> getLanguages()
	{
		return languages;
	}

	public SingleSelectionList<NameValue> getTechnicalFormats()
	{
		return technicalFormats;
	}

	public SingleSelectionList<NameValue> getMaterialAudiences()
	{
		return materialAudiences;
	}

	public MultiSelectionList<MobileOperatingSystem> getMobileOS()
	{
		return mobileOS;
	}

	public MultiSelectionList<MobileType> getMobileType()
	{
		return mobileType;
	}

	private final MerlotFilterType languageFilterType = new MerlotFilterType()
	{
		@Override
		public Collection<NameValue> getValues(SectionInfo info)
		{
			return merlotWebService.getLanguages(info);
		}
	};

	private final MerlotFilterType technicalFormatFilterType = new MerlotFilterType()
	{
		@Override
		public Collection<NameValue> getValues(SectionInfo info)
		{
			return merlotWebService.getTechnicalFormats(info);
		}
	};

	private final MerlotFilterType materialAudienceFilterType = new MerlotFilterType()
	{
		@Override
		public Collection<NameValue> getValues(SectionInfo info)
		{
			return merlotWebService.getAudiences(info);
		}
	};

}
