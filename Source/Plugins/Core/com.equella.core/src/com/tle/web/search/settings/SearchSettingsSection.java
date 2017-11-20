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

package com.tle.web.search.settings;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.Search.SortType;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.common.settings.standard.SearchSettings.SearchFilter;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.search.service.impl.SearchPrivilegeTreeProvider;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.SubmitValuesFunction;
import com.tle.web.sections.jquery.libraries.JQuerySlider;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

/**
 * @author Aaron
 */
@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class SearchSettingsSection extends OneColumnLayout<SearchSettingsSection.SearchSettingsSectionModel>
{
	@PlugKey("settings.page.title")
	private static Label TITLE_LABEL;
	@PlugKey("settings.currentlyselectedstuff.edit")
	private static Label EDIT_SEARCH_FILTER_LABEL;
	@PlugKey("settings.currentlyselectedstuff.remove")
	private static Label REMOVE_SEARCH_FILTER_LABEL;
	@PlugKey("settings.currentlyselectedstuff.removeconfirm")
	private static Confirm REMOVE_SEARCH_FILTER_CONFIRM;
	@PlugKey("settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("settings.save.boost.zero")
	private static String BOOST_VALUE_ZERO;

	@PlugKey("settings.sort.rank")
	private static String SORT_RANK_KEY;
	@PlugKey("settings.sort.datemodified")
	private static String SORT_DATE_MODIFIED_KEY;
	@PlugKey("settings.sort.datecreated")
	private static String SORT_DATE_CREATED_KEY;
	@PlugKey("settings.sort.name")
	private static String SORT_NAME_KEY;
	@PlugKey("settings.sort.rating")
	private static String SORT_RATING_KEY;

	@PlugKey("settings.filter.title")
	private static Label LABEL_FILTERS;

	@PlugKey("settings.label.indexnone")
	private static String INDEX_NONE_LABEL;
	@PlugKey("settings.label.referenced")
	private static String INDEX_REFERENCED_LABEL;
	@PlugKey("settings.label.linked")
	private static String INDEX_REFLINKED_LABEL;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ReceiptService receiptService;
	@Inject
	private SearchPrivilegeTreeProvider securityProvider;

	@TreeLookup
	private EditSearchFilterSection editSection;

	@EventFactory
	private EventGenerator events;

	@Component
	private SingleSelectionList<VoidKeyOption> defaultSortType;
	@Component
	private Checkbox showNonLiveCheckbox;
	@Component
	private Checkbox authenticateByDefault;
	@Component
	private Checkbox disableGalleryView;
	@Component(name = "dicc")
	private Checkbox disableFileCountCheckbox;
	@Component
	private Checkbox disableVideosView;

	@Component
	private SingleSelectionList<NameValue> harvestOptions;

	@Component
	@PlugKey("settings.save")
	private Button saveButton;
	@Component
	@PlugKey("settings.button.newfilter")
	private Link newFilterLink;
	@Component(name = "f")
	private SelectionsTable filtersTable;

	@Component
	private TextField titleBoost;
	@Component
	private TextField descriptionBoost;
	@Component
	private TextField attachmentBoost;

	@Inject
	private PluginTracker<SearchSettingsExtension> extensionsTracker;
	private List<SearchSettingsExtension> extensions;

	private SubmitValuesFunction removeFilterFunc;
	private SubmitValuesFunction editFilterFunc;

	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(SearchSettingsSection.class);
	private static final IncludeFile INCLUDE = new IncludeFile(RESOURCES.url("scripts/slider.js"));
	private static final JSCallable SHOW_SLIDER = new ExternallyDefinedFunction("showSlider", JQuerySlider.PRERENDER,
		INCLUDE);

	@Override
	protected TemplateResult setupTemplate(RenderEventContext context)
	{
		securityProvider.checkAuthorised();

		SearchSettingsSectionModel model = getModel(context);
		SectionId modalSection = model.getModalSection();
		final SearchSettings settings = getSearchSettings();

		int titleBoostValue = settings.getTitleBoost();
		int descriptionBoostValue = settings.getDescriptionBoost();
		int attachmentBoostValue = settings.getAttachmentBoost();

		if( model.isNotSuccessful() )
		{
			model.addError("boostValueZero", CurrentLocale.get(BOOST_VALUE_ZERO));
			titleBoostValue = 0;
			descriptionBoostValue = 0;
			attachmentBoostValue = 0;
		}

		titleBoost.setValue(context, String.valueOf(titleBoostValue));
		titleBoost.addReadyStatements(context, SHOW_SLIDER, "title-slider", titleBoostValue);

		descriptionBoost.setValue(context, String.valueOf(descriptionBoostValue));
		descriptionBoost.addReadyStatements(context, SHOW_SLIDER, "description-slider", descriptionBoostValue);

		attachmentBoost.setValue(context, String.valueOf(attachmentBoostValue));
		attachmentBoost.addReadyStatements(context, SHOW_SLIDER, "attachment-slider", attachmentBoostValue);

		if( modalSection != null )
		{
			return getTemplateResult(context);
		}

		if( defaultSortType.getSelectedValueAsString(context) == null )
		{
			defaultSortType.setSelectedStringValue(context, settings.getDefaultSearchSort());
			showNonLiveCheckbox.setChecked(context, settings.isSearchingShowNonLiveCheckbox());
			authenticateByDefault.setChecked(context, settings.isAuthenticateFeedsByDefault());
		}

		disableGalleryView.setChecked(context, settings.isSearchingDisableGallery());
		disableVideosView.setChecked(context, settings.isSearchingDisableVideos());
		disableFileCountCheckbox.setChecked(context, settings.isFileCountDisabled());
		harvestOptions.setSelectedStringValue(context, Integer.toString(settings.getUrlLevel()));

		final List<SectionId> extensionsAsSectionIds = Lists.newArrayList();
		extensionsAsSectionIds.addAll(extensions);
		model.setExtensions(SectionUtils.renderSectionIds(context, extensionsAsSectionIds));

		GenericTemplateResult templateResult = new GenericTemplateResult();
		templateResult.addNamedResult(BODY, viewFactory.createResult("settings/searchsettings.ftl", this));
		HelpAndScreenOptionsSection.addHelp(context, viewFactory.createResult("settings/searchsettingshelp.ftl", this));
		return templateResult;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		removeFilterFunc = events.getSubmitValuesFunction("removefilter");
		editFilterFunc = events.getSubmitValuesFunction("editfilter");

		final ArrayList<VoidKeyOption> sorts = new ArrayList<VoidKeyOption>();
		sorts.add(new VoidKeyOption(SORT_RANK_KEY, SortType.RANK.toString()));
		sorts.add(new VoidKeyOption(SORT_DATE_MODIFIED_KEY, SortType.DATEMODIFIED.toString()));
		sorts.add(new VoidKeyOption(SORT_DATE_CREATED_KEY, SortType.DATECREATED.toString()));
		sorts.add(new VoidKeyOption(SORT_NAME_KEY, SortType.NAME.toString()));
		sorts.add(new VoidKeyOption(SORT_RATING_KEY, SortType.RATING.toString()));
		defaultSortType.setListModel(new SimpleHtmlListModel<VoidKeyOption>(sorts));

		SimpleHtmlListModel<NameValue> harvestOptionsList = new SimpleHtmlListModel<NameValue>(
			new NameValue(CurrentLocale.get(INDEX_NONE_LABEL), Integer.toString(SearchSettings.URL_DEPTH_LEVEL_NONE)),
			new NameValue(CurrentLocale.get(INDEX_REFERENCED_LABEL),
				Integer.toString(SearchSettings.URL_DEPTH_LEVEL_REFERENCED)),
			new NameValue(CurrentLocale.get(INDEX_REFLINKED_LABEL),
				Integer.toString(SearchSettings.URL_DEPTH_LEVEL_REFERENCED_AND_LINKED)));
		harvestOptions.setListModel(harvestOptionsList);
		saveButton.setClickHandler(events.getNamedHandler("save"));

		newFilterLink.setClickHandler(events.getNamedHandler("editfilter", Constants.BLANK));

		filtersTable.setAddAction(newFilterLink);
		filtersTable.setColumnHeadings(LABEL_FILTERS, null);
		filtersTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.NONE);
		filtersTable.setSelectionsModel(new SearchFilterModel());

		extensions = extensionsTracker.getBeanList();
		for( SearchSettingsExtension s : extensions )
		{
			tree.registerInnerSection(s, id);
		}
	}

	private void save(SectionInfo info, SearchSettings settings)
	{
		settings.setDefaultSearchSort(defaultSortType.getSelectedValueAsString(info));
		settings.setSearchingShowNonLiveCheckbox(showNonLiveCheckbox.isChecked(info));
		settings.setAuthenticateFeedsByDefault(authenticateByDefault.isChecked(info));

		settings.setTitleBoost(Integer.parseInt(titleBoost.getValue(info)));
		settings.setDescriptionBoost(Integer.parseInt(descriptionBoost.getValue(info)));
		settings.setAttachmentBoost(Integer.parseInt(attachmentBoost.getValue(info)));
		settings.setSearchingDisableGallery(disableGalleryView.isChecked(info));
		settings.setSearchingDisableVideos(disableVideosView.isChecked(info));
		settings.setFileCountDisabled(getDisableFileCountCheckbox().isChecked(info));
		int selectedOption = Integer.parseInt(harvestOptions.getSelectedValueAsString(info));
		settings.setUrlLevel(selectedOption);

		for( SearchSettingsExtension s : extensions )
		{
			s.save(info, settings);
		}

		configService.setProperties(settings);

		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		if( isAllBoostValuesZero(info) )
		{
			getModel(info).setNotSuccessful(true);
		}
		else
		{
			save(info, getSearchSettings());
			getModel(info).setNotSuccessful(false);
		}
	}

	private boolean isAllBoostValuesZero(SectionInfo info)
	{
		if( titleBoost.getValue(info).equals("0") && descriptionBoost.getValue(info).equals("0")
			&& attachmentBoost.getValue(info).equals("0") )
		{
			return true;
		}
		return false;
	}

	@EventHandlerMethod
	public void editfilter(SectionInfo info, String filterUuid)
	{
		if( Check.isEmpty(filterUuid) )
		{
			editSection.newFilter(info);
		}
		else
		{
			editSection.setFilter(info, getFilter(getSearchSettings(), filterUuid));
		}
	}

	@EventHandlerMethod
	public void removefilter(SectionInfo info, String filterUuid)
	{
		final SearchSettings settings = getSearchSettings();
		final SearchFilter f = getFilter(settings, filterUuid);
		if( f != null )
		{
			settings.getFilters().remove(f);
		}
		save(info, settings);
	}

	@Nullable
	private SearchFilter getFilter(SearchSettings settings, String filterUuid)
	{
		for( SearchFilter filter : settings.getFilters() )
		{
			if( filter.getId().equals(filterUuid) )
			{
				return filter;
			}
		}
		return null;
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	public SingleSelectionList<VoidKeyOption> getDefaultSortType()
	{
		return defaultSortType;
	}

	public Checkbox getShowNonLiveCheckbox()
	{
		return showNonLiveCheckbox;
	}

	public Checkbox getAuthenticateByDefault()
	{
		return authenticateByDefault;
	}

	public Checkbox getDisableGalleryView()
	{
		return disableGalleryView;
	}

	public Checkbox getDisableVideosView()
	{
		return disableVideosView;
	}

	public SingleSelectionList<NameValue> getHarvestOptions()
	{
		return harvestOptions;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public SelectionsTable getFiltersTable()
	{
		return filtersTable;
	}

	@Nullable
	private SearchSettings getSearchSettings()
	{
		return configService.getProperties(new SearchSettings());
	}

	public TextField getTitleBoost()
	{
		return titleBoost;
	}

	public TextField getDescriptionBoost()
	{
		return descriptionBoost;
	}

	public TextField getAttachmentBoost()
	{
		return attachmentBoost;
	}

	private class SearchFilterModel extends DynamicSelectionsTableModel<SearchFilter>
	{
		@Override
		protected List<SearchFilter> getSourceList(SectionInfo info)
		{
			final SearchSettings settings = getSearchSettings();
			return settings != null ? settings.getFilters() : null;
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, SearchFilter filter,
			List<SectionRenderable> actions, int index)
		{
			final String filterId = filter.getId();
			final OverrideHandler editHandler = new OverrideHandler(editFilterFunc, filterId);

			selection.setViewAction(new LinkRenderer(new HtmlLinkState(new TextLabel(filter.getName()), editHandler)));

			actions.add(makeAction(EDIT_SEARCH_FILTER_LABEL, editHandler));
			actions.add(makeAction(REMOVE_SEARCH_FILTER_LABEL,
				new OverrideHandler(removeFilterFunc, filterId).addValidator(REMOVE_SEARCH_FILTER_CONFIRM)));
		}
	}

	@Override
	public Class<SearchSettingsSectionModel> getModelClass()
	{
		return SearchSettingsSectionModel.class;
	}

	public Checkbox getDisableFileCountCheckbox()
	{
		return disableFileCountCheckbox;
	}

	@NonNullByDefault(false)
	public static class SearchSettingsSectionModel extends OneColumnLayout.OneColumnLayoutModel
	{
		@Bookmarked
		private boolean notSuccessful;
		private List<SectionRenderable> extensions;
		private Map<String, String> errors = new HashMap<String, String>();

		public List<SectionRenderable> getExtensions()
		{
			return extensions;
		}

		public void setExtensions(List<SectionRenderable> extensions)
		{
			this.extensions = extensions;
		}

		public Map<String, String> getErrors()
		{
			return errors;
		}

		public void addError(String key, String value)
		{
			this.errors.put(key, value);
		}

		public boolean isNotSuccessful()
		{
			return notSuccessful;
		}

		public void setNotSuccessful(boolean notSuccessful)
		{
			this.notSuccessful = notSuccessful;
		}
	}
}