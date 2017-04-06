package com.tle.web.itemadmin.section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.common.text.NumberStringComparator;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.PresetSearch;
import com.tle.common.search.whereparser.InvalidWhereException;
import com.tle.common.search.whereparser.WhereParser;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.entity.ItemDefinitionService;
import com.tle.web.i18n.BundleCache;
import com.tle.web.itemadmin.WithinEntry;
import com.tle.web.itemadmin.WithinExtension;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.search.service.AutoCompleteResult;
import com.tle.web.search.service.AutoCompleteService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.search.SearchResultsActionsSection;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.LabelOption;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
public class ItemAdminQuerySection
	extends
		AbstractResetFiltersQuerySection<ItemAdminQuerySection.ItemAdminQueryModel, FreetextSearchEvent>
{
	static
	{
		PluginResourceHandler.init(AbstractResetFiltersQuerySection.class);
	}

	private static final String ALL_KEY = "all";
	private static final String DIV_QUERY = "searchform";

	@PlugKey("query.addquery")
	private static Label ADD_QUERY_LABEL;
	@PlugKey("query.editquery")
	private static Label EDIT_QUERY_LABEL;

	@EventFactory
	private EventGenerator events;

	@Inject
	private BundleCache bundleCache;
	@Inject
	private ItemDefinitionService itemDefinitionService;

	@Component(parameter = "in", supported = true)
	private SingleSelectionList<WithinEntry> collectionList;
	@Component
	private Button editQueryButton;
	@Component
	@PlugKey("query.clearquery")
	private Button clearQueryButton;

	@PlugKey("query.collections.title")
	private static Label KEY_COLLECTIONS;

	@TreeLookup
	private ItemAdminWhereSection whereSection;
	@Inject
	private AutoCompleteService autoCompleteService;

	@AjaxFactory
	private AjaxGenerator ajax;

	@TreeLookup
	private ItemAdminSearchResultsSection searchResults;

	private PluginTracker<WithinExtension> tracker;
	private Map<String, WithinExtension> extensions;

	private JSHandler collectionChangedHandler;

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		tracker = new PluginTracker<WithinExtension>(pluginService, getClass(), "withinExtension", "id")
			.setBeanKey("bean");
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		ItemAdminQueryModel model = getModel(context);
		WithinEntry selected = collectionList.getSelectedValue(context);
		if( selected != null )
		{
			model.setWhereShowing(selected.isWhere());
		}
		ArrayList<SectionRenderable> sections = new ArrayList<SectionRenderable>();

		for( WithinExtension withinExtension : extensions.values() )
		{
			sections.add(withinExtension.render(context));
		}
		model.setSections(sections);

		editQueryButton.setLabel(context, Check.isEmpty(model.getCriteria()) ? ADD_QUERY_LABEL : EDIT_QUERY_LABEL);

		renderQueryActions(context, model);
		return viewFactory.createResult("query.ftl", this);
	}

	@AjaxMethod
	public AutoCompleteResult[] updateSearchTerms(SectionInfo info)
	{
		FreetextSearchEvent fte = searchResults.createSearchEvent(info);
		fte.setExcludeKeywords(true);
		info.processEvent(fte);

		return autoCompleteService.getAutoCompleteResults(fte.getFinalSearch(), queryField.getValue(info));
	}

	@Override
	protected String getAjaxDiv()
	{
		return DIV_QUERY;
	}

	public DefaultSearch createDefaultSearch(SectionInfo info)
	{
		DefaultSearch search;
		WithinEntry selected = collectionList.getSelectedValue(info);
		if( selected != null )
		{
			if( Check.isEmpty(selected.getTypeId()) )
			{
				FreeTextQuery freetext = null;

				StringBuilder where = new StringBuilder();
				ItemAdminQueryModel model = getModel(info);
				List<String> clauses = model.getCriteria();
				if( !Check.isEmpty(clauses) )
				{
					whereSection.getWhereClauses().setValues(info, clauses);
					for( String clause : clauses )
					{
						where.append(clause.trim());
						where.append(' ');
					}
					String whereString = where.toString();
					if( whereString.length() > 0 )
					{
						try
						{
							freetext = WhereParser.parse(whereString);
						}
						catch( InvalidWhereException e )
						{
							model.setWhereException(e);
						}
					}
				}

				search = new PresetSearch(null, freetext, false);
				search.setCollectionUuids(Collections.singleton(selected.getBel().getUuid()));
			}
			else
			{
				WithinExtension withinExtension = extensions.get(selected.getTypeId());
				search = withinExtension.createDefaultSearch(info, selected);
			}

		}
		else
		{
			search = new PresetSearch(null, null, false);
			// Redmine #8120 Use all collections except SYSTEM (e.g My Content)
			Collection<String> collections = Collections2.transform(itemDefinitionService.listAll(),
				new Function<BaseEntityLabel, String>()
				{
					@Override
					public String apply(BaseEntityLabel bel)
					{
						return bel.getUuid();
					}
				});
			search.setCollectionUuids(collections);
		}

		return search;

	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		super.prepareSearch(info, event);
		Exception whereException = getModel(info).getWhereException();
		if( whereException != null )
		{
			event.setException(whereException);
		}
	}

	@Override
	public Class<ItemAdminQueryModel> getModelClass()
	{
		return ItemAdminQueryModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "iaq";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		extensions = tracker.getBeanMap();
		for( WithinExtension withinExtension : extensions.values() )
		{
			withinExtension.register(id, tree);
		}

		searchButton.setLabel(new KeyLabel("item.section.query.search"));

		collectionList.setListModel(new ItemAdminWhereModel());
		editQueryButton.setClickHandler(events.getNamedHandler("editQuery"));
		clearQueryButton.setClickHandler(events.getNamedHandler("clearQuery"));
		queryField.setAutoCompleteCallback(ajax.getAjaxFunction("updateSearchTerms"));

		collectionList.setDefaultRenderer("richdropdown");
		collectionList.setGrouped(true);

	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		searchButton.setClickHandler(searchResults.getRestartSearchHandler(tree));
		queryField.addEventStatements("autoselect", searchResults.getRestartSearchHandler(tree));
		collectionList.addChangeEventHandler(getCollectionChangedHandler(tree));
	}

	public JSHandler getCollectionChangedHandler(SectionTree tree)
	{

		if( collectionChangedHandler == null )
		{
			ArrayList<String> ajaxIds = new ArrayList<String>();
			ajaxIds.add(DIV_QUERY);
			ajaxIds.add(SearchResultsActionsSection.ACTIONS_AJAX_ID);

			collectionChangedHandler = new OverrideHandler(searchResults.getResultsUpdater(tree,
				events.getEventHandler("changedCollection"), ajaxIds.toArray(new String[ajaxIds.size()])));
		}
		return collectionChangedHandler;
	}

	@EventHandlerMethod
	public void editQuery(SectionInfo info)
	{
		whereSection.setEditQuery(info, true, collectionList.getSelectedValueAsString(info));
	}

	@EventHandlerMethod
	public void clearQuery(SectionInfo info)
	{
		getModel(info).clearCriteria();
		whereSection.clearOptions(info);
	}

	@EventHandlerMethod
	public void changedCollection(SectionInfo info)
	{
		clearQuery(info);
	}

	public SingleSelectionList<WithinEntry> getCollectionList()
	{
		return collectionList;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ItemAdminQueryModel();
	}

	public class ItemAdminQueryModel extends AbstractResetFiltersQuerySection.AbstractQuerySectionModel
	{
		private boolean whereShowing;
		@Bookmarked(supported = true, parameter = "c")
		private List<String> criteria;
		private Exception whereException;
		private List<SectionRenderable> sections;

		public void clearCriteria()
		{
			criteria = null;
		}

		public boolean isWhereShowing()
		{
			return whereShowing;
		}

		public void setWhereShowing(boolean whereShowing)
		{
			this.whereShowing = whereShowing;
		}

		public List<String> getCriteria()
		{
			return criteria;
		}

		public void setCriteria(List<String> criteria)
		{
			this.criteria = criteria;
		}

		public Exception getWhereException()
		{
			return whereException;
		}

		public void setWhereException(Exception whereException)
		{
			this.whereException = whereException;
		}

		public List<SectionRenderable> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}

	}

	public class ItemAdminWhereModel extends DynamicHtmlListModel<WithinEntry>
	{
		public ItemAdminWhereModel()
		{
			setSort(true);
			setComparator(new NumberStringComparator<Option<WithinEntry>>(true)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public String convertToString(Option<WithinEntry> t)
				{
					WithinEntry withinEntry = t.getObject();
					return Strings.nullToEmpty(withinEntry.getOrder() + " " + t.getGroupName() + " " + t.getName());
				}
			});
		}

		@Override
		protected Iterable<WithinEntry> populateModel(SectionInfo info)
		{
			List<BaseEntityLabel> listAll = itemDefinitionService.listAll();
			List<WithinEntry> list = Lists.newArrayList(Lists.transform(listAll,
				new Function<BaseEntityLabel, WithinEntry>()
				{

					@Override
					public WithinEntry apply(BaseEntityLabel input)
					{
						return new WithinEntry(input, KEY_COLLECTIONS, null, true, 1);
					}
				}));

			for( WithinExtension withinExtension : extensions.values() )
			{
				withinExtension.populateModel(info, list);
			}
			return list;
		}

		@Override
		protected Option<WithinEntry> convertToOption(SectionInfo info, final WithinEntry obj)
		{
			BaseEntityLabel bel = obj.getBel();
			Label label = obj.getOverrideLabel() == null ? new BundleLabel(bel.getBundleId(), bundleCache) : obj
				.getOverrideLabel();
			return new LabelOption<WithinEntry>(label, bel.getUuid(), obj)
			{
				@Override
				public String getGroupName()
				{
					return obj.getGroup().getText();
				}
			};
		}

		@Override
		protected Option<WithinEntry> getTopOption()
		{
			return new KeyOption<WithinEntry>("com.tle.web.itemadmin.query.collection.all", ALL_KEY, null)
			{
				@Override
				public String getGroupName()
				{
					return KEY_COLLECTIONS.getText();
				}
			};
		}

		@Nullable
		@Override
		public WithinEntry getValue(SectionInfo info, @Nullable String value)
		{
			if( value == null || ALL_KEY.equals(value) )
			{
				return null;
			}
			return super.getValue(info, value);
		}
	}

	public Button getEditQueryButton()
	{
		return editQueryButton;
	}

	public ItemAdminSearchResultsSection getSearchResults()
	{
		return searchResults;
	}

	public Button getClearQueryButton()
	{
		return clearQueryButton;
	}

	public Label getCollectionsLabel()
	{
		return KEY_COLLECTIONS;
	}
}
