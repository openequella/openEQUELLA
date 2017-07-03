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

package com.tle.web.portal.standard.editor;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.common.portal.entity.Portlet;
import com.tle.common.portal.entity.impl.PortletRecentContrib;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.core.portal.service.PortletService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.filter.ItemStatusListModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.expression.CombinedExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class RecentContribPortletEditorSection
	extends
		AbstractPortletEditorSection<AbstractPortletEditorSection.AbstractPortletEditorModel>
{
	private static final int DEFAULT_AGE = 30;

	private static final String TYPE = "recent";
	public static final String KEY_TITLEONLY = "titleOnly";
	@PlugKey("editor.rss.label.titleonly")
	private static String titleOnly;
	@PlugKey("editor.rss.label.titledesc")
	private static String titleAndDescription;

	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(RecentContribPortletEditorSection.class);

	@PlugKey("editor.recent.collections.all")
	private static Label ALL_NAME_KEY;

	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private PortletService portletService;

	@ViewFactory
	private FreemarkerFactory thisView;

	@Component(name = "ac", stateful = false)
	private Checkbox allCollections;
	@Component(name = "c", stateful = false)
	private MultiSelectionList<BaseEntityLabel> collection;
	@Component(name = "s", stateful = false)
	private SingleSelectionList<ItemStatus> itemStatus;
	@Component(name = "q", stateful = false)
	private TextField query;
	@Component(name = "a", stateful = false)
	private TextField age;
	@Component(name = "d", stateful = false)
	private SingleSelectionList<BundleNameValue> displayTypeList;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		allCollections.setLabel(ALL_NAME_KEY);

		// Disable/enable the list of collections when "All Collections" is
		// checked/unchecked
		allCollections.addEventStatements(JSHandler.EVENT_CHANGE,
			new FunctionCallStatement(collection.createDisableFunction(), allCollections.createGetExpression()));

		// Disable "All Collections" if one or more collections have been picked
		collection.addEventStatements(JSHandler.EVENT_CHANGE,
			new FunctionCallStatement(allCollections.createDisableFunction(),
				new CombinedExpression(collection.createGetExpression(), new PropertyExpression("length != 0"))));

		EnumSet<ItemStatus> statuses = EnumSet.allOf(ItemStatus.class);
		statuses.remove(ItemStatus.PERSONAL);
		SimpleHtmlListModel<ItemStatus> statii = new ItemStatusListModel();
		statii.addAll(statuses);
		itemStatus.setListModel(statii);

		DynamicHtmlListModel<BaseEntityLabel> listModel = new DynamicHtmlListModel<BaseEntityLabel>()
		{
			@Override
			protected Iterable<BaseEntityLabel> populateModel(SectionInfo info)
			{
				return collectionService.listSearchable();
			}

			@Override
			protected Option<BaseEntityLabel> convertToOption(SectionInfo info, BaseEntityLabel bent)
			{
				return new NameValueOption<BaseEntityLabel>(
					new BundleNameValue(bent.getBundleId(), Long.toString(bent.getId()), bundleCache), bent);
			}
		};
		listModel.setSort(true);

		collection.setListModel(listModel);
		displayTypeList.setAlwaysSelect(true);
		displayTypeList.setListModel(new SimpleHtmlListModel<BundleNameValue>(
			new BundleNameValue(titleAndDescription, null), new BundleNameValue(titleOnly, KEY_TITLEONLY)));
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "rct";
	}

	@Override
	protected Portlet createNewPortlet()
	{
		Portlet portlet = new Portlet(TYPE);
		PortletRecentContrib extraData = new PortletRecentContrib();
		extraData.setAgeDays(DEFAULT_AGE);
		portlet.setExtraData(extraData);
		return portlet;
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		collection.setSelectedStringValues(info, null);
		allCollections.setChecked(info, false);
		displayTypeList.setSelectedStringValue(info, null);
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		PortletRecentContrib extra = (PortletRecentContrib) portlet.getExtraData();
		Collection<ItemDefinition> collections = extra.getCollections();

		if( Check.isEmpty(collections) )
		{
			String sessionId = getModel(info).getSessionId();
			if( !Check.isEmpty(sessionId) )
			{
				allCollections.setChecked(info,
					!portletService.loadSession(sessionId).getValidationErrors().containsKey("collection"));
			}
			collection.addReadyStatements(info, collection.createDisableFunction(),
				allCollections.createGetExpression());
		}
		else
		{
			allCollections.addReadyStatements(info, allCollections.createDisableFunction(),
				new CombinedExpression(collection.createGetExpression(), new PropertyExpression("length != 0")));

			// Note: using DB IDs for the list. See #6212. Proper fix would
			// require major changes
			// to entity editing sessions
			collection.setSelectedStringValues(info,
				Collections2.transform(collections, new Function<ItemDefinition, String>()
				{
					@Override
					public String apply(ItemDefinition col)
					{
						return Long.toString(col.getId());
					}
				}));
		}
		query.setValue(info, extra.getQuery());
		final int ageDays = extra.getAgeDays();
		if( ageDays == 0 )
		{
			age.setValue(info, Constants.BLANK);
		}
		else
		{
			age.setValue(info, Integer.toString(extra.getAgeDays()));
		}
		itemStatus.setSelectedStringValue(info, portlet.getAttribute("status"));
		displayTypeList.setSelectedStringValue(info, portlet.getAttribute(KEY_TITLEONLY));
	}

	@Override
	protected SectionRenderable customRender(RenderEventContext context,
		AbstractPortletEditorSection.AbstractPortletEditorModel model, PortletEditingBean portlet) throws Exception
	{
		return thisView.createResult("edit/editrecentportlet.ftl", context);
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		PortletRecentContrib extra = new PortletRecentContrib();
		portlet.setExtraData(extra);
		final Set<String> longIds = collection.getSelectedValuesAsStrings(info);
		if( longIds.size() == 0 )
		{
			extra.setCollections(null);
		}
		else
		{
			final Collection<Long> ids = Collections2.transform(longIds, new Function<String, Long>()
			{
				@Override
				public Long apply(String id)
				{
					return Long.valueOf(id);
				}
			});
			extra.setCollections(collectionService.getByIds(ids));
		}
		extra.setQuery(query.getValue(info));
		portlet.setAttribute("status", itemStatus.getSelectedValueAsString(info));
		final String ageStr = age.getValue(info);
		if( !Check.isEmpty(ageStr) )
		{
			try
			{
				int i = Integer.parseInt(ageStr);
				if( i > 0 )
				{
					extra.setAgeDays(i);
				}
				else
				{
					extra.setAgeDays(0);
				}
			}
			catch( NumberFormatException nfe )
			{
				extra.setAgeDays(0);
			}
		}
		portlet.setAttribute(KEY_TITLEONLY, displayTypeList.getSelectedValueAsString(info));
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		final String ageStr = age.getValue(info);
		if( !Check.isEmpty(ageStr) )
		{
			try
			{
				int i = Integer.parseInt(ageStr);
				if( i <= 0 )
				{
					errors.put("age", RESOURCES.getString("editor.recent.error.results.zero"));
				}
			}
			catch( NumberFormatException nfe )
			{
				errors.put("age", RESOURCES.getString("editor.recent.error.results.zero"));
			}
		}
		else
		{
			errors.put("age", RESOURCES.getString("editor.recent.error.results.zero"));
		}

		if( !allCollections.isChecked(info) )
		{
			final Set<String> collectionUuids = collection.getSelectedValuesAsStrings(info);
			if( collectionUuids.size() == 0 )
			{
				errors.put("collection", RESOURCES.getString("editor.recent.error.results.collection"));
			}
		}
	}

	public MultiSelectionList<BaseEntityLabel> getCollection()
	{
		return collection;
	}

	public TextField getQuery()
	{
		return query;
	}

	public TextField getAge()
	{
		return age;
	}

	@Override
	public Class<AbstractPortletEditorSection.AbstractPortletEditorModel> getModelClass()
	{
		return AbstractPortletEditorSection.AbstractPortletEditorModel.class;
	}

	public SingleSelectionList<ItemStatus> getItemStatus()
	{
		return itemStatus;
	}

	public SingleSelectionList<BundleNameValue> getDisplayTypeList()
	{
		return displayTypeList;
	}

	public Checkbox getAllCollections()
	{
		return allCollections;
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return null;
	}
}
