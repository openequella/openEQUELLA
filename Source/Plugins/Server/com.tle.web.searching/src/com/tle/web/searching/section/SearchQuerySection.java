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

package com.tle.web.searching.section;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.queries.FreeTextQuery;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.DynaCollection;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.search.DefaultSearch;
import com.tle.common.search.LiveItemSearch;
import com.tle.common.search.PresetSearch;
import com.tle.common.settings.standard.SearchSettings;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.dynacollection.DynaCollectionService;
import com.tle.core.fedsearch.FederatedSearchService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.remoterepo.service.RemoteRepoWebService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.search.event.FreetextSearchResultEvent;
import com.tle.web.search.filter.AbstractResetFiltersQuerySection;
import com.tle.web.search.service.AutoCompleteResult;
import com.tle.web.search.service.AutoCompleteService;
import com.tle.web.searching.OnSearchExtension;
import com.tle.web.searching.OnSearchExtensionHandler;
import com.tle.web.searching.SearchWhereModel;
import com.tle.web.searching.SearchWhereModel.WhereEntry;
import com.tle.web.searching.WithinType;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonTrait;
import com.tle.web.sections.equella.render.SettingsRenderer;
import com.tle.web.sections.equella.search.event.SearchResultsListener;
import com.tle.web.sections.events.AbstractDirectEvent;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.CallAndReferenceFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;
import com.tle.web.wizard.page.AjaxUpdateData;
import com.tle.web.wizard.page.ControlResult;
import com.tle.web.wizard.page.PageUpdateCallback;
import com.tle.web.wizard.page.WebWizardPageState;
import com.tle.web.wizard.page.WizardPage;
import com.tle.web.wizard.page.WizardPageService;

@NonNullByDefault
@SuppressWarnings("nls")
public class SearchQuerySection
	extends
		AbstractResetFiltersQuerySection<SearchQuerySection.SearchQueryModel, FreetextSearchEvent>
	implements
		BookmarkEventListener,
		SearchResultsListener<FreetextSearchResultEvent>,
		BlueBarEventListener
{
	private static final PluginResourceHelper resources = ResourcesService.getResourceHelper(SearchQuerySection.class);

	private static final String KEY_SEARCHED = "$SEARCHED$-";
	private static final String KEY_XML = "$POWERXML$";
	private static final Logger LOGGER = Logger.getLogger(SearchQuerySection.class);

	private static final String DIV_QUERY = "searchform";

	@PlugKey("query.addquery")
	private static Label ADD_QUERY_LABEL;
	@PlugKey("query.editquery")
	private static Label EDIT_QUERY_LABEL;
	@PlugKey("results.includenonlive")
	private static Label LABEL_NONLIVE;
	@PlugKey("query.hint")
	private static Label LABEL_QUERY_HINT;
	private static Label LABEL_SEARCH = new KeyLabel("item.section.query.search");

	private static IncludeFile UPDATE_INCLUDE = new IncludeFile(resources.url("scripts/updateinterface.js"));

	@EventFactory
	private EventGenerator events;

	@Inject
	private UserSessionService sessionService;
	@Inject
	private AuditLogService auditLogService;
	@Inject
	private DynaCollectionService dynaCollectionService;
	@Inject
	private WizardPageService wizardPageService;
	@Inject
	private SelectionService selectionService;
	@Inject
	private FederatedSearchService federatedSearchService;
	@Inject
	private RemoteRepoWebService remoteRepoWebService;
	@Inject
	private AutoCompleteService autoCompleteService;
	@Inject
	private ItemDefinitionService collectionService;
	@Inject
	private SearchWhereModel searchWhereModel;
	@Inject
	private ConfigurationService configService;

	@Component(name = "c", parameter = "in", supported = true)
	private SingleSelectionList<WhereEntry> collectionList;
	@Component
	private Button editQueryButton;
	@Component
	@PlugKey("query.clearquery")
	private Button clearQueryButton;
	@Component
	@PlugKey("query.doadvanced")
	private Button doAdvancedButton;
	@Component
	private TextField currentHidden;
	@Component(name = "inl", parameter = "nonlive", supported = true)
	private Checkbox includeNonLive;

	@TreeLookup
	private SearchResultsSection searchResults;
	@AjaxFactory
	private AjaxGenerator ajax;

	private JSCallable reloadFunction;
	private JSCallable updateInterface;
	private JSCallable restoreSkinnySearchActions;

	private OnSearchExtensionHandler onSearchCollector;

	@Override
	protected String getAjaxDiv()
	{
		return DIV_QUERY;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final SearchQueryModel model = getModel(context);

		if( isPowerSelected(context) )
		{
			// power search hides the skinny sortandfilter div, so add show the
			// div to search buttons
			doAdvancedButton.addClickStatements(context, new FunctionCallStatement(restoreSkinnySearchActions));
			searchButton.addClickStatements(context, new FunctionCallStatement(restoreSkinnySearchActions));

			WizardPage powerPage = model.ensurePowerPage();
			// If its a advanced search build the page
			if( model.isEditQuery() )
			{
				showControls(context, powerPage);
			}
			else
			{
				model.setSubmitWizard(false);
				model.setShowWhere(true);
				if( model.getXml() != null )
				{
					model.setCriteria(powerPage.getCriteriaList());
				}
			}

		}

		editQueryButton.setLabel(context, Check.isEmpty(model.getCriteria()) ? ADD_QUERY_LABEL : EDIT_QUERY_LABEL);

		if( !model.isEditQuery() )
		{
			renderQueryActions(context, getModel(context));
		}

		return viewFactory.createResult("query.ftl", this);
	}

	private boolean isPowerSelected(SectionInfo info)
	{
		SearchQueryModel model = getModel(info);
		WhereEntry entry = model.getSelectedEntry();
		currentHidden.setValue(info, entry != null ? entry.getValue() : "");
		return (entry != null && entry.getType() == WithinType.POWER);
	}

	private void showControls(RenderEventContext context, WizardPage powerPage)
	{
		SearchQueryModel model = getModel(context);
		final Map<String, List<ControlResult>> results = powerPage.renderPage(context, model.getUpdateData(),
			"wizard-controls");
		model.setAdvancedControls(results);
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "sq";
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		onSearchCollector = new OnSearchExtensionHandler();
		tree.addRegistrationHandler(onSearchCollector);

		searchButton.setLabel(LABEL_SEARCH);
		searchButton.setComponentAttribute(ButtonTrait.class, ButtonTrait.PRIMARY);

		collectionList.setListModel(searchWhereModel);
		collectionList.setDefaultRenderer("richdropdown");
		collectionList.setGrouped(true);

		reloadFunction = PageUpdateCallback.getReloadFunction(ajax.getAjaxFunction("reloadControls"));

		queryField.setAutoCompleteCallback(ajax.getAjaxFunction("updateSearchTerms"));
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		JSCallable resultsUpdater = searchResults.getResultsUpdater(tree, events.getEventHandler("changedWhere"),
			DIV_QUERY);

		UpdateDomFunction changedWhere = ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("changedWhere"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), DIV_QUERY);

		UpdateDomFunction editQuery = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("editQuery"),
			ajax.getEffectFunction(EffectType.FADEOUTIN_ONRESULTS), "wherecontainer", "wizardcontainer");

		JSCallable clearQuery = searchResults.getResultsUpdater(tree, events.getEventHandler("clearQuery"), DIV_QUERY);

		restoreSkinnySearchActions = new ExternallyDefinedFunction("restoreSkinnySearchActions", UPDATE_INCLUDE);

		List<JSCallAndReference> onSearchFunctions = Lists.newArrayList();

		onSearchFunctions.add(CallAndReferenceFunction
			.get(searchResults.getResultsUpdater(tree, events.getEventHandler("saveQuery"), DIV_QUERY), searchButton));
		List<OnSearchExtension> onSearchExtensions = onSearchCollector.getAllImplementors(tree);
		for( OnSearchExtension ose : onSearchExtensions )
		{
			onSearchFunctions.add(ose.getOnSearchCallable());
			onSearchFunctions.add(new ExternallyDefinedFunction("fadeResultsContainerIn", UPDATE_INCLUDE));
		}

		ExternallyDefinedFunction searchFunction = new ExternallyDefinedFunction("searchFunction", UPDATE_INCLUDE);

		// collect search listeners
		JSHandler searchHandler = new OverrideHandler(searchFunction, new ArrayExpression(onSearchFunctions.toArray()));

		doAdvancedButton.setClickHandler(searchHandler);
		searchButton.setClickHandler(searchHandler);
		queryField.addEventStatements("autoselect", searchHandler);

		updateInterface = new ExternallyDefinedFunction("changedWhere", UPDATE_INCLUDE);

		ObjectExpression changedWhereUpdaters = new ObjectExpression();
		changedWhereUpdaters.put("collectionSelected", resultsUpdater);
		changedWhereUpdaters.put("powerSelected", changedWhere);
		changedWhereUpdaters.put("remoteSelected", events.getSubmitValuesFunction("forwardToRemote"));

		collectionList.addChangeEventHandler(new OverrideHandler(updateInterface, changedWhereUpdaters,
			collectionList.createGetExpression(), currentHidden.createGetExpression()));

		clearQueryButton.setClickHandler(new OverrideHandler(clearQuery));

		editQueryButton.setClickHandler(
			new StatementHandler(new ExternallyDefinedFunction("editQuery", UPDATE_INCLUDE), editQuery));

		includeNonLive.setClickHandler(new StatementHandler(searchResults.getResultsUpdater(tree, null)));

	}

	@AjaxMethod
	public AutoCompleteResult[] updateSearchTerms(SectionInfo info)
	{
		FreetextSearchEvent fte = searchResults.createSearchEvent(info);
		fte.setExcludeKeywords(true);
		info.processEvent(fte);

		return autoCompleteService.getAutoCompleteResults(fte.getFinalSearch(), queryField.getValue(info));
	}

	@EventHandlerMethod
	public void forwardToRemote(SectionInfo info)
	{
		if( isRemoteSelected(info) )
		{
			changedWhere(info);
			remoteRepoWebService.forwardToSearch(info,
				federatedSearchService.getByUuid(collectionList.getSelectedValue(info).getUuid()), true);
			collectionList.setSelectedStringValue(info, "all");
		}
	}

	@EventHandlerMethod
	public void changedWhere(SectionInfo info)
	{
		SearchQueryModel model = getModel(info);
		model.changedSelection();
		model.setEditQuery(isPowerSelected(info));
	}

	private boolean isRemoteSelected(SectionInfo info)
	{
		WhereEntry entry = getModel(info).getSelectedEntry();
		currentHidden.setValue(info, entry != null ? entry.getValue() : "");
		return entry != null && entry.getType() == WithinType.REMOTE;
	}

	@EventHandlerMethod
	public void editQuery(SectionInfo info)
	{
		// The editQuery javascript hides the skinny sortandfilter div (where it
		// exists). Here we
		// undo the hide by calling show on same (where it exists).
		doAdvancedButton.addClickStatements(info, new FunctionCallStatement(restoreSkinnySearchActions));
		searchButton.addClickStatements(info, new FunctionCallStatement(restoreSkinnySearchActions));
		SearchQueryModel model = getModel(info);
		model.setEditQuery(true);
	}

	@EventHandlerMethod
	public void clearQuery(SectionInfo info)
	{
		SearchQueryModel model = getModel(info);
		model.clearXml();
	}

	@EventHandlerMethod
	public void saveQuery(SectionInfo info)
	{
		SearchQueryModel model = getModel(info);
		model.setSubmitWizard(model.isEditQuery());
		model.setEditQuery(false);
	}

	@AjaxMethod
	public JSONResponseCallback reloadControls(AjaxRenderContext context, AjaxUpdateData reloadData)
	{
		return new QueryCallback(context, reloadData);
	}

	public DefaultSearch createDefaultSearch(SectionInfo info, boolean includeQuery)
	{
		boolean nonLive = getSearchSettings().isSearchingShowNonLiveCheckbox() ? includeNonLive.isChecked(info) : false;
		boolean dynamicCollection = false;

		String queryText = null;
		FreeTextQuery freetext = null;
		Collection<String> collectionUuids = null;
		Set<String> mimeTypes = null;

		SelectionSession session = selectionService.getCurrentSession(info);

		if( session != null )
		{
			// XXX: hack to make sure default collection can only be set once
			if( session.getAttribute("setDefaultCollection") == null )
			{
				String defaultCollectionUuid = session.getDefaultCollectionUuid();
				if( defaultCollectionUuid != null )
				{
					this.setCollection(info, defaultCollectionUuid);
				}
				session.setAttribute("setDefaultCollection", true);
			}
		}

		SearchQueryModel model = getModel(info);
		WhereEntry selCollection = model.getSelectedEntry();
		if( selCollection != null )
		{
			switch( selCollection.getType() )
			{
				case COLLECTION:
					ItemDefinition entity = selCollection.getEntity();
					this.setCollection(info, entity.getUuid());
					collectionUuids = Arrays.asList(entity.getUuid());
					break;

				case DYNAMIC:
					dynamicCollection = true;
					DynaCollection dc = selCollection.getEntity();
					queryText = dc.getFreetextQuery();
					freetext = dynaCollectionService.getSearchClause(dc, selCollection.getVirt());
					break;

				case POWER:
					PowerSearch powerSearch = selCollection.getEntity();
					collectionUuids = collectionService.convertToUuids(powerSearch.getItemdefs());

					model.ensurePowerPage();
					if( model.getXml() != null )
					{
						freetext = model.getPowerPage().getPowerSearchQuery();
						model.setAdvancedQuery(freetext != null);
					}
					break;
				default:
					break; // no further alteration to search
			}
		}
		if( session != null )
		{
			if( !session.isAllCollections() )
			{
				// Would be better if we could use security filtering on
				// UUID return results
				final Collection<ItemDefinition> collections = collectionService
					.getMatchingSearchableUuid(session.getCollectionUuids());
				Collection<String> convertToUuids = collectionService.convertToUuids(collections);

				if( collectionUuids != null )
				{
					collectionUuids.retainAll(convertToUuids);
				}
				else
				{
					collectionUuids = convertToUuids;
				}
			}
			mimeTypes = session.getMimeTypes();
		}

		PresetSearch search = new PresetSearch(queryText, freetext, !nonLive, dynamicCollection);

		if( mimeTypes != null && !mimeTypes.isEmpty() )
		{
			search.setMimeTypes(mimeTypes);
		}
		if( collectionUuids != null )
		{
			search.setCollectionUuids(collectionUuids);
		}
		return search;
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event) throws Exception
	{
		super.prepareSearch(info, event);
		SearchQueryModel model = getModel(info);
		event.setQueryFiltered(event.isQueryFiltered() || model.isAdvancedQuery());
	}

	@Override
	public void processResults(SectionInfo info, FreetextSearchResultEvent results)
	{
		FreetextSearchEvent searchEvent = results.getSearchEvent();
		final boolean loggable = searchEvent.isLoggable();

		if( loggable )
		{
			String in = null;
			SearchQueryModel model = getModel(info);
			WhereEntry selCollection = model.getSelectedEntry();
			String type = "STANDARD";
			if( selCollection != null )
			{
				in = selCollection.getUuid();
				switch( selCollection.getType() )
				{
					case POWER:
						type = "POWER";
						break;
					case DYNAMIC:
						type = "DYNAMIC";
						break;
					default:
						break; // "STANDARD" applies
				}
			}
			final String query = searchEvent.getSearchedText();
			final long resultCount = results.getResults().getAvailable();

			if( !isAlreadySearched(type, query, in) )
			{
				auditLogService.logSearch(type, query, in, resultCount);
				registerSearched(type, query, in);
			}
		}
	}

	private boolean isAlreadySearched(String type, String query, @Nullable String in)
	{
		Boolean val = sessionService.getAttribute(key(type, query, in));
		return (val != null && val);
	}

	private void registerSearched(String type, String query, @Nullable String in)
	{
		sessionService.setAttribute(key(type, query, in), Boolean.TRUE);
	}

	private String key(String type, String query, @Nullable String in)
	{
		return KEY_SEARCHED + type + in + query;
	}

	public void setCollection(SectionInfo info, String collectionUuid)
	{
		SearchWhereModel whereModel = (SearchWhereModel) collectionList.getListModel();
		collectionList.setSelectedStringValue(info,
			whereModel.createWhere(collectionUuid, WithinType.COLLECTION).getValue());
		final SearchQueryModel model = getModel(info);
		model.setXml(null);
		model.setSubmitWizard(false);
	}

	public void setPowerSearch(SectionInfo info, String uuid, String xml)
	{
		SearchWhereModel whereModel = (SearchWhereModel) collectionList.getListModel();
		collectionList.setSelectedStringValue(info, whereModel.createWhere(uuid, WithinType.POWER).getValue());

		final SearchQueryModel model = getModel(info);
		model.setXml(xml);
		model.setSubmitWizard(false);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new SearchQueryModel(info);
	}

	@Override
	public Class<SearchQueryModel> getModelClass()
	{
		return SearchQueryModel.class;
	}

	public Button getDoAdvancedButton()
	{
		return doAdvancedButton;
	}

	public SingleSelectionList<WhereEntry> getCollectionList()
	{
		return collectionList;
	}

	public Button getEditQueryButton()
	{
		return editQueryButton;
	}

	@Nullable
	public String getCriteriaText(SectionInfo info)
	{
		SearchQueryModel model = getModel(info);
		WhereEntry entry = model.getSelectedEntry();
		if( entry == null || entry.getType() != WithinType.POWER )
		{
			return null;
		}

		WizardPage powerPage = model.ensurePowerPage();
		if( powerPage != null )
		{
			StringBuilder sbuf = new StringBuilder();
			List<String> criteriaList = powerPage.getCriteriaList();
			for( String criteria : criteriaList )
			{
				sbuf.append(criteria);
				sbuf.append('\n');
			}
			return sbuf.toString();
		}
		return null;
	}

	public static DefaultSearch createDefaultSearch()
	{
		return new LiveItemSearch();
	}

	private void doBasicSearch(SectionInfo info, String query)
	{
		setQuery(info, query);
		collectionList.setSelectedStringValue(info, null);
		changedWhere(info);
		searchResults.startSearch(info);
	}

	public static void basicSearch(SectionInfo from, String query)
	{
		SectionInfo info = RootSearchSection.createForward(from);
		SearchQuerySection sqs = info.lookupSection(SearchQuerySection.class);
		sqs.doBasicSearch(info, query);
		from.forwardAsBookmark(info);
	}

	public static void powerSearch(SectionInfo from, String uuid)
	{
		SectionInfo info = RootSearchSection.createForward(from);
		SearchQuerySection sqs = info.lookupSection(SearchQuerySection.class);
		sqs.setPowerSearch(info, uuid, null);
		from.forwardAsBookmark(info);
	}

	public static void skinnyPowerSearch(SectionInfo from, String uuid)
	{
		SectionInfo info = RootSkinnySearchSection.createForward(from);
		SearchQuerySection sqs = info.lookupSection(SearchQuerySection.class);
		sqs.setPowerSearch(info, uuid, null);
		from.forwardAsBookmark(info);
	}

	public class QueryCallback extends PageUpdateCallback
	{
		public QueryCallback(AjaxRenderContext context, AjaxUpdateData reloadData)
		{
			super(context, reloadData);
			SearchQueryModel model = getModel(context);
			model.setUpdateData(reloadData);
		}

		@Override
		public Map<String, List<ControlResult>> getPageResults()
		{
			return getModel(context).getAdvancedControls();
		}

		@Override
		public Map<String, String> getHiddenState()
		{
			return null;
		}
	}

	@NonNullByDefault(false)
	public class SearchQueryModel extends AbstractResetFiltersQuerySection.AbstractQuerySectionModel
	{
		@Bookmarked(name = "sw")
		private boolean submitWizard;

		@Bookmarked(name = "editquery", parameter = "editquery", supported = true)
		private boolean editQuery;
		@Bookmarked(parameter = "doc", rendered = true, supported = true)
		private String doc;

		private AjaxUpdateData updateData;
		private List<String> criteria;
		private WhereEntry selectedEntry;
		private WizardPage powerPage;
		private boolean showWhere;
		private boolean advancedQuery;

		private Map<String, List<ControlResult>> advancedControls;
		private final SectionInfo info;

		public SearchQueryModel(SectionInfo info)
		{
			this.info = info;
		}

		public WizardPage ensurePowerPage()
		{
			if( powerPage == null )
			{
				loadPowerPage();
			}
			return powerPage;
		}

		public void changedSelection()
		{
			setXml(null);
			setSubmitWizard(false);
		}

		public void setXml(String xml)
		{
			if( !info.getBooleanAttribute(SectionInfo.KEY_FOR_URLS_ONLY) )
			{
				if( xml == null )
				{
					sessionService.removeAttribute(KEY_XML);
				}
				else
				{
					sessionService.setAttribute(KEY_XML, xml);
				}
			}
			doc = xml;
		}

		public String getXml()
		{
			if( doc == null && !info.getBooleanAttribute(SectionInfo.KEY_FOR_URLS_ONLY) )
			{
				doc = sessionService.getAttribute(KEY_XML);
			}
			return doc;
		}

		public Map<String, List<ControlResult>> getAdvancedControls()
		{
			return advancedControls;
		}

		public void setAdvancedControls(Map<String, List<ControlResult>> advancedControls)
		{
			setSubmitWizard(true);
			this.advancedControls = advancedControls;
		}

		public void clearXml()
		{
			setXml(null);
			setSubmitWizard(false);
		}

		public WizardPage getPowerPage()
		{
			return powerPage;
		}

		public void loadPowerPage()
		{
			WhereEntry entry = getSelectedEntry();
			if( entry == null || entry.getType() != WithinType.POWER )
			{
				throw new Error("No power search");
			}

			PowerSearch powerSearch = entry.getEntity();
			String docXml = getXml();
			PropBagEx itemXml = null;
			if( docXml != null )
			{
				itemXml = new PropBagEx(docXml);
			}
			else
			{
				itemXml = getIntegrationXml(powerSearch);
			}
			powerPage = wizardPageService.createSimplePage(powerSearch.getWizard(), itemXml, new WebWizardPageState(),
				true);
			powerPage.setReloadFunction(reloadFunction);
			try
			{
				powerPage.createPage();
				powerPage.loadFromDocument(info);
				powerPage.saveDefaults();
			}
			catch( Exception e )
			{
				Throwables.propagate(e);
			}
			powerPage.ensureTreeAdded(info, isSubmitWizard());

			if( isSubmitWizard() )
			{
				info.queueEvent(new AbstractDirectEvent(SectionEvent.PRIORITY_AFTER_EVENTS, getSectionId())
				{
					@NonNullByDefault
					@Override
					public void fireDirect(SectionId sectionId, SectionInfo info2) throws Exception
					{
						powerPage.saveToDocument(info2);
						PropBagEx newXml = powerPage.getDocBag();
						setXml(newXml.toString());
					}
				});
			}
		}

		private boolean isSubmitWizard()
		{
			return submitWizard;
		}

		private PropBagEx getIntegrationXml(final PowerSearch powerSearch)
		{
			String docXml;
			// Not pretty...
			final SelectionSession session = selectionService.getCurrentSession(info);
			if( session != null )
			{
				docXml = session.getInitialPowerXml();
				if( !Check.isEmpty(docXml) )
				{
					// wrap this in a friendly error catcher.
					// don't let people's dodgy XML screw us up.
					try
					{
						for( PropBagEx powersearch : new PropBagEx(docXml).iterateAll("powersearch") )
						{
							final String powerUuid = powersearch.getNode("@uuid");
							if( powerUuid != null && powerUuid.equals(powerSearch.getUuid()) )
							{
								PropBagEx fullXml = new PropBagEx(powersearch.getRootElement());
								PropBagEx itemXml = fullXml.getSubtree("xml");
								setXml(itemXml.toString());
								return itemXml;
							}
						}
					}
					catch( Exception pex )
					{
						LOGGER.error("Error setting provided powersearch XML", pex);
					}
				}
			}
			return null;
		}

		public boolean isShowWhere()
		{
			return showWhere;
		}

		public void setShowWhere(boolean whereShowing)
		{
			this.showWhere = whereShowing;
		}

		public boolean isEditQuery()
		{
			return editQuery;
		}

		public void setEditQuery(boolean editQuery)
		{
			this.editQuery = editQuery;
		}

		public WhereEntry getSelectedEntry()
		{
			if( selectedEntry == null )
			{
				selectedEntry = collectionList.getSelectedValue(info);
			}
			return selectedEntry;
		}

		public LanguageBundle getAdvancedTitle()
		{
			PowerSearch entity = getSelectedEntry().getEntity();
			return entity.getName();
		}

		public List<String> getCriteria()
		{
			return criteria;
		}

		public void setCriteria(List<String> criteria)
		{
			this.criteria = criteria;
		}

		public String getDoc()
		{
			return getXml(); // return doc;
		}

		public void setDoc(String doc)
		{
			setXml(doc);
		}

		public void setSubmitWizard(boolean submitWizard)
		{
			this.submitWizard = submitWizard;
		}

		public AjaxUpdateData getUpdateData()
		{
			return updateData;
		}

		public void setUpdateData(AjaxUpdateData updateData)
		{
			this.updateData = updateData;
		}

		public boolean isAdvancedQuery()
		{
			return advancedQuery;
		}

		public void setAdvancedQuery(boolean advancedQuery)
		{
			this.advancedQuery = advancedQuery;
		}
	}

	public SearchResultsSection getSearchResults()
	{
		return searchResults;
	}

	public Button getClearQueryButton()
	{
		return clearQueryButton;
	}

	public TextField getCurrentHidden()
	{
		return currentHidden;
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_AFTER_EVENTS)
	public void ensurePowerPage(SectionInfo info)
	{
		SearchQueryModel model = getModel(info);
		if( model.isSubmitWizard() )
		{
			model.loadPowerPage();
		}
	}

	@Override
	public void bookmark(SectionInfo info, BookmarkEvent event)
	{
		SearchQueryModel model = getModel(info);

		WizardPage powerPage = model.getPowerPage();
		if( event.isPartial() && powerPage != null )
		{
			List<SectionId> allChildIds = Lists.newArrayList(info.getAllChildIds(this));
			allChildIds.addAll(powerPage.getRootIds());
			event.setChildren(allChildIds);
		}
	}

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		// nothing
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		if( getSearchSettings().isSearchingShowNonLiveCheckbox() )
		{
			event.addScreenOptions(
				new SettingsRenderer(LABEL_NONLIVE, renderSection(context, includeNonLive), "screen-option")); //$NON-NLS-1$
		}
	}

	private SearchSettings getSearchSettings()
	{
		return configService.getProperties(new SearchSettings());
	}

	public Label getQueryHintLabel()
	{
		return LABEL_QUERY_HINT;
	}
}
