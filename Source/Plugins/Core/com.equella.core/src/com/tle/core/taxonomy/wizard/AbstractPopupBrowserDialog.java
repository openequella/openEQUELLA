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

package com.tle.core.taxonomy.wizard;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.taxonomy.SelectionRestriction;
import com.tle.common.taxonomy.wizard.TermSelectorControl.TermStorageFormat;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.core.taxonomy.wizard.AbstractPopupBrowserDialog.AbstractPopupBrowserModel;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.JQuerySelector;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQueryTabs;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ScriptExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.HiddenState;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.js.modules.JSONModule;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;

@NonNullByDefault
@SuppressWarnings("nls")
public abstract class AbstractPopupBrowserDialog<M extends AbstractPopupBrowserModel> extends AbstractOkayableDialog<M>
{
	@PlugKey("currentlyselectedstuff.remove")
	private static Label REMOVE_TERM_LABEL;
	@PlugKey("wizard.popupbrowser.showallresults")
	private static String SHOW_ALL_LABEL;
	@PlugKey("currentlyselectedstuff.nothing")
	private static Label NO_SELECTIONS_LABEL;

	private static final int MAX_SEARCH_RESULTS = 50;

	@Inject
	private TaxonomyService taxonomyService;

	@Component
	private Tree treeView;
	@Component
	private TextField searchQuery;
	@Component
	@PlugKey("wizard.popupbrowser.searchbutton")
	private Button searchButton;
	@Component
	private HiddenState selectedTerms;
	@Component
	private SelectionsTable selectedTermsTable;

	protected String taxonomyUuid;
	private String taxonomyName;
	private boolean multiple;
	private boolean searchEnabled = true;
	private boolean browseEnabled = true;
	private TermStorageFormat storageFormat;
	private SelectionRestriction restriction;

	protected SimpleFunction selectTermFunc;
	private JSCallable removeTermFunc;
	private JSCallable searchFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);

		// The following function is used by pretty much every node in the tree
		// and for each search result. Because it's used so much, we refactor it
		// out into a function so that we minimise the amount of JS being
		// output.
		ScriptVariable selectTermVar = new ScriptVariable("term");
		selectTermFunc = new SimpleFunction("selectTerm", new FunctionCallStatement(
			ajaxEvents.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("selectTerm"),
				ajaxEvents.getEffectFunction(EffectType.FADEOUTIN), "selectedTerms"), selectTermVar), selectTermVar);

		removeTermFunc = ajaxEvents.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("removeTerm"),
			ajaxEvents.getEffectFunction(EffectType.FADEOUTIN), "selectedTerms");
		searchFunc = ajaxEvents.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("searchTerms"),
			ajaxEvents.getEffectFunction(EffectType.FADEOUTIN), "searchResults");

		searchButton.setClickHandler(new OverrideHandler(searchFunc, false));

		treeView.setModel(new TermSelectorTreeModel());
		treeView.setLazyLoad(true);

		final ScriptExpression se = new ScriptExpression(
			"tabs({fx: {opacity:'toggle', duration: 'fast'}}).tabs('option', 'disabled', false);");
		se.setPreRenderers(JQueryTabs.PRERENDER);
		treeView.addReadyStatements(new JQueryStatement(new JQuerySelector("#termChooser"), se));

		selectedTermsTable.setFilterable(false);
		selectedTermsTable.setNothingSelectedText(NO_SELECTIONS_LABEL);
		selectedTermsTable.setSelectionsModel(new DynamicSelectionsTableModel<String>()
		{
			@Override
			protected List<String> getSourceList(SectionInfo info)
			{
				return selectedTerms.getValues(info);
			}

			@Override
			protected void transform(SectionInfo info, SelectionsTableSelection selection, String term,
				List<SectionRenderable> actions, int index)
			{
				selection.setViewAction(new LabelRenderer(new TextLabel(term)));
				actions.add(makeRemoveAction(REMOVE_TERM_LABEL, new OverrideHandler(removeTermFunc, term)));
			}
		});

		taxonomyName = CurrentLocale.get(taxonomyService.getByUuid(taxonomyUuid).getName());
	}

	@Override
	protected JSHandler createOkHandler(SectionTree tree)
	{
		JSExpression termList = JSONModule.getStringifyExpression(selectedTerms.createGetExpression());
		return new OverrideHandler(jscall(getOkCallback(), termList), jscall(getCloseFunction()));
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return new TextLabel(taxonomyName);
	}

	@EventHandlerMethod
	public void searchTerms(SectionInfo info, boolean showAllResults)
	{
		String query = searchQuery.getValue(info);
		if( !Check.isEmpty(query) )
		{
			final AbstractPopupBrowserModel model = getModel(info);
			model.setSearchExecuted(true);

			// For this search type, we want to find stuff anywhere in the term,
			// so always put wild-cards on it.
			query = '*' + query + '*';

			Pair<Long, List<TermResult>> results = taxonomyService.searchTerms(taxonomyUuid, query, restriction,
				showAllResults ? -1 : MAX_SEARCH_RESULTS, true);

			List<SectionRenderable> srs = new ArrayList<SectionRenderable>();
			for( TermResult tr : results.getSecond() )
			{
				srs.add(getTermClickTarget(tr));
			}
			model.setSearchResults(srs);

			final long totalResults = results.getFirst();
			model.setSearchTotalResults(totalResults);

			if( srs.size() < totalResults )
			{
				HtmlComponentState button = new HtmlComponentState(new OverrideHandler(searchFunc, true));
				button.setLabel(new KeyLabel(SHOW_ALL_LABEL, totalResults));
				model.setShowAllResults(new ButtonRenderer(button));
			}
		}
	}

	protected abstract SectionRenderable getTermClickTarget(TermResult tr);

	@EventHandlerMethod
	public void selectTerm(SectionInfo info, String termFullPath)
	{
		if( storageFormat == TermStorageFormat.LEAF_ONLY )
		{
			termFullPath = taxonomyService.getTerm(taxonomyUuid, termFullPath).getTerm();
		}

		final Collection<String> terms = selectedTerms.getValues(info);
		if( !terms.contains(termFullPath) )
		{
			if( !isMultiple() )
			{
				terms.clear();
			}
			terms.add(termFullPath);
			selectedTerms.setValues(info, terms);
		}
	}

	@EventHandlerMethod
	public void removeTerm(SectionInfo info, String selectedTermValue)
	{
		List<String> values = selectedTerms.getValues(info);
		if( values.remove(selectedTermValue) )
		{
			selectedTerms.setValues(info, values);
		}
	}

	@Override
	public String getWidth()
	{
		return "95%";
	}

	protected boolean isSelectable(TermResult term)
	{
		switch( restriction )
		{
			case UNRESTRICTED:
				return true;
			case LEAF_ONLY:
				return term.isLeaf();
			case TOP_LEVEL_ONLY:
				return term.getTerm().equals(term.getFullTerm());
			default:
				return false;
		}
	}

	public boolean isMultiple()
	{
		return multiple;
	}

	public void setMultiple(boolean multiple)
	{
		this.multiple = multiple;
	}

	public void setTaxonomyUuid(String taxonomyUuid)
	{
		this.taxonomyUuid = taxonomyUuid;
	}

	public void setRestriction(SelectionRestriction restriction)
	{
		this.restriction = restriction;
	}

	public void setStorageFormat(TermStorageFormat storageFormat)
	{
		this.storageFormat = storageFormat;
	}

	public boolean isSearchEnabled()
	{
		return searchEnabled;
	}

	public void setSearchEnabled(boolean searchEnabled)
	{
		this.searchEnabled = searchEnabled;
	}

	public boolean isBrowseEnabled()
	{
		return browseEnabled;
	}

	public void setBrowseEnabled(boolean browseEnabled)
	{
		this.browseEnabled = browseEnabled;
	}

	public Tree getTreeView()
	{
		return treeView;
	}

	public TextField getSearchQuery()
	{
		return searchQuery;
	}

	public Button getSearchButton()
	{
		return searchButton;
	}

	public SelectionsTable getSelectedTermsTable()
	{
		return selectedTermsTable;
	}

	public boolean isShowTabs()
	{
		return searchEnabled && browseEnabled;
	}

	public SimpleFunction getSelectTermFunc()
	{
		return selectTermFunc;
	}

	public class TermSelectorTreeModel implements HtmlTreeModel
	{
		@Override
		public List<HtmlTreeNode> getChildNodes(SectionInfo info, String id)
		{
			return Lists.transform(taxonomyService.getChildTerms(taxonomyUuid, id),
				new Function<TermResult, HtmlTreeNode>()
				{
					@Override
					public HtmlTreeNode apply(TermResult tr)
					{
						return new TermSelectorTreeNode(tr);
					}
				});
		}
	}

	public class TermSelectorTreeNode implements HtmlTreeNode
	{
		private final TermResult tr;

		public TermSelectorTreeNode(TermResult tr)
		{
			this.tr = tr;
		}

		@Override
		public String getId()
		{
			return tr.getFullTerm();
		}

		@Override
		public boolean isLeaf()
		{
			return tr.isLeaf();
		}

		@Override
		public SectionRenderable getRenderer()
		{
			return getTermClickTarget(tr);
		}

		@Override
		public Label getLabel()
		{
			// This method shouldn't be executed if we are returning a
			// SectionRenderable from getRenderer().
			throw new RuntimeException("We should not reach here");
		}
	}

	public static abstract class AbstractPopupBrowserModel extends DialogModel
	{
		private boolean searchExecuted;
		private long searchTotalResults;
		private List<SectionRenderable> searchResults;
		private SectionRenderable showAllResults;

		public boolean isSearchExecuted()
		{
			return searchExecuted;
		}

		public void setSearchExecuted(boolean searchExecuted)
		{
			this.searchExecuted = searchExecuted;
		}

		public long getSearchTotalResults()
		{
			return searchTotalResults;
		}

		public void setSearchTotalResults(long searchTotalResults)
		{
			this.searchTotalResults = searchTotalResults;
		}

		public List<SectionRenderable> getSearchResults()
		{
			return searchResults;
		}

		public void setSearchResults(List<SectionRenderable> searchResults)
		{
			this.searchResults = searchResults;
		}

		public SectionRenderable getShowAllResults()
		{
			return showAllResults;
		}

		public void setShowAllResults(SectionRenderable showAllResults)
		{
			this.showAllResults = showAllResults;
		}
	}
}
