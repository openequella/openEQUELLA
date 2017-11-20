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

package com.tle.web.quickcontributeandversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.VersionSelection;
import com.tle.common.Check;
import com.tle.common.Format;
import com.tle.common.NameValue;
import com.tle.common.settings.standard.QuickContributeAndVersionSettings;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class RootQuickContributeAndVersionSettingsSection
	extends
		OneColumnLayout<RootQuickContributeAndVersionSettingsSection.QuickContributeAndVersionSettingsModel>
{
	@PlugKey("quickcontributeandversionsettings.title")
	private static Label TITLE_LABEL;
	@PlugKey("quickcontribute.settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("quickcontributeandversionsettings.none")
	private static String NO_COLLECTION_KEY;
	@PlugKey("quickcontributeandversionsettings.forcecurrent")
	private static String FORCE_CURRENT_KEY;
	@PlugKey("quickcontributeandversionsettings.forcelatest")
	private static String FORCE_LATEST_KEY;
	@PlugKey("quickcontributeandversionsettings.defaultcurrent")
	private static String DEFAULT_CURRENT_KEY;
	@PlugKey("quickcontributeandversionsettings.defaultlatest")
	private static String DEFAULT_LATEST_KEY;

	@Component(name = "fins")
	private SingleSelectionList<NameValue> collectionSelector;
	@Component
	private SingleSelectionList<NameValue> versionViewOptions;

	@Component(name = "e", stateful = false)
	@PlugKey("selectionsettings.disablebutton.label")
	private Checkbox disable;
	@Component
	@PlugKey("settings.save.button")
	private Button saveButton;

	@EventFactory
	private EventGenerator events;

	@Inject
	private QuickContributeAndVersionSettingsPrivilegeTreeProvider securityProvider;
	@Inject
	private ConfigurationService configService;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private ReceiptService receiptService;

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();
		// Get the currently store one click collection, if any
		QuickContributeAndVersionSettings settings = configService
			.getProperties(new QuickContributeAndVersionSettings());
		String existingSelectedUuid = settings.getOneClickCollection();
		collectionSelector.setSelectedStringValue(info, existingSelectedUuid);

		VersionSelection currentVersionSelection = settings.getVersionSelection();
		if( currentVersionSelection != null )
		{
			versionViewOptions.setSelectedStringValue(info, currentVersionSelection.toString());
		}

		disable.setChecked(info, settings.isButtonDisable());

		return new GenericTemplateResult(
			viewFactory.createNamedResult(BODY, "quickcontributeandversionsettings.ftl", this));
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		collectionSelector.setListModel(new DynamicHtmlListModel<NameValue>()
		{
			@Override
			protected Iterable<NameValue> populateModel(SectionInfo info)
			{
				final List<NameValue> populateMe = new ArrayList<NameValue>();
				List<ItemDefinition> allCollections = itemDefinitionService.enumerate();
				for( ItemDefinition itemDef : allCollections )
				{
					populateMe.add(new BundleNameValue(itemDef.getName(), itemDef.getUuid(), bundleCache));
				}
				Collections.sort(populateMe, Format.NAME_VALUE_COMPARATOR);
				populateMe.add(0, new BundleNameValue(NO_COLLECTION_KEY, null));
				collectionSelector.getState(info).setDisallowMultiple(true);
				return populateMe;
			}
		});

		SimpleHtmlListModel<NameValue> versionOptions = new SimpleHtmlListModel<NameValue>(
			new BundleNameValue(FORCE_CURRENT_KEY, VersionSelection.FORCE_CURRENT.toString()),
			new BundleNameValue(FORCE_LATEST_KEY, VersionSelection.FORCE_LATEST.toString()),
			new BundleNameValue(DEFAULT_CURRENT_KEY, VersionSelection.DEFAULT_TO_CURRENT.toString()),
			new BundleNameValue(DEFAULT_LATEST_KEY, VersionSelection.DEFAULT_TO_LATEST.toString()));
		versionViewOptions.setListModel(versionOptions);
		versionViewOptions.setAlwaysSelect(true);

		saveButton.setClickHandler(events.getNamedHandler("save"));
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	public SingleSelectionList<NameValue> getCollectionSelector()
	{
		return collectionSelector;
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		boolean altered = false;
		QuickContributeAndVersionSettings settings = configService
			.getProperties(new QuickContributeAndVersionSettings());
		String existingSelectedUuid = settings.getOneClickCollection();
		NameValue selectedNV = collectionSelector.getSelectedValue(info);
		String selectedNVUuid = selectedNV != null ? selectedNV.getValue() : null;
		if( !(Check.isEmpty(existingSelectedUuid) && Check.isEmpty(selectedNVUuid)) )
		{
			if( existingSelectedUuid == null )
			{
				existingSelectedUuid = "";
			}
			if( !existingSelectedUuid.equals(selectedNVUuid) )
			{
				altered |= true;
				settings.setOneClickCollection(selectedNVUuid);
			}
		}

		NameValue selectedVersionSelectionNV = versionViewOptions.getSelectedValue(info);
		VersionSelection oldVersionSelection = settings.getVersionSelection();
		String oldVersionSelectAsString = oldVersionSelection != null ? oldVersionSelection.toString() : "";
		String newVersionSelectAsString = selectedVersionSelectionNV != null
			&& !Check.isEmpty(selectedVersionSelectionNV.getValue()) ? selectedVersionSelectionNV.getValue() : "";

		if( !oldVersionSelectAsString.equals(newVersionSelectAsString) )
		{
			altered |= true;
			settings.setVersionSelection(VersionSelection.valueOf(newVersionSelectAsString));
		}

		boolean buttonDisable = settings.isButtonDisable();
		if( !disable.isChecked(info) == buttonDisable )
		{
			altered |= true;
			settings.setButtonDisable(disable.isChecked(info));
		}

		if( altered )
		{
			configService.setProperties(settings);
			receiptService.setReceipt(SAVE_RECEIPT_LABEL);
		}
	}

	public SingleSelectionList<NameValue> getVersionViewOptions()
	{
		return versionViewOptions;
	}

	/**
	 * @return the saveButton
	 */
	public Button getSaveButton()
	{
		return saveButton;
	}

	@Override
	public Class<QuickContributeAndVersionSettingsModel> getModelClass()
	{
		return QuickContributeAndVersionSettingsModel.class;
	}

	public Checkbox getDisable()
	{
		return disable;
	}

	public static class QuickContributeAndVersionSettingsModel extends OneColumnLayout.OneColumnLayoutModel
	{
		// Token implementation
	}
}
