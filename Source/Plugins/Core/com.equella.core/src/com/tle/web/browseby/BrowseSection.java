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

package com.tle.web.browseby;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.common.searching.Field;
import com.tle.core.remoting.MatrixResults;
import com.tle.core.remoting.MatrixResults.MatrixEntry;
import com.tle.freetext.FreetextIndex;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.event.FreetextSearchEvent;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.search.event.SearchEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.template.Breadcrumbs;

public class BrowseSection extends AbstractPrototypeSection<BrowseSection.Model>
	implements
		SearchEventListener<FreetextSearchEvent>,
		HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;
	@Inject
	private FreetextIndex freetextIndex;
	@EventFactory
	private EventGenerator events;
	@PlugKey("browseby.pagetitle")
	private static String KEY_TITLE;
	@PlugKey("alltitle")
	private static Label LABEL_ROOT;
	@TreeLookup
	private BrowseSearchResults browseResults;

	public Label getTitle(SectionInfo info)
	{
		Model model = getModel(info);
		Breadcrumbs crumbs = Breadcrumbs.get(info);
		BrowseRow row = getBrowseRows(info);
		BrowseRow rootDisplayRow = model.getRootDisplayRow();
		boolean useTitle = true;
		while( !row.equals(rootDisplayRow) )
		{
			crumbs.add(row.getViewLink());
			row = row.getChildren().get(0);
			useTitle = false;
		}
		if( useTitle )
		{
			String pageName = model.getPageName();
			if( pageName == null )
			{
				pageName = "??"; //$NON-NLS-1$
			}
			return new KeyLabel(KEY_TITLE, pageName);
		}
		else
		{
			return row.getTitle();
		}
	}

	@Override
	public void prepareSearch(SectionInfo info, FreetextSearchEvent event)
	{
		BrowseSearch search = event.getDefaultSeach();
		Model model = getModel(info);
		List<String> fields = getFields(info);
		List<String> values = model.getValues();
		int i = 0;
		List<Field> musts = new ArrayList<Field>();
		if( values != null )
		{
			for( String value : values )
			{
				musts.add(new Field(fields.get(i), value));
				i++;
			}
		}
		search.setMustFields(musts);
		Collection<String> collectionUuids = model.getCollections();
		if( !Check.isEmpty(collectionUuids) )
		{
			search.setCollectionUuids(collectionUuids);
		}
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getBrowseRows(context);
		return viewFactory.createResult("browsepage.ftl", this); //$NON-NLS-1$
	}

	@SuppressWarnings("nls")
	private BrowseRow getBrowseRows(SectionInfo info)
	{
		Model model = getModel(info);
		if( model.getRootRow() != null )
		{
			return model.getRootRow();
		}
		BrowseSearch search = new BrowseSearch();
		Collection<String> collectionUuids = model.getCollections();
		if( !Check.isEmpty(collectionUuids) )
		{
			search.setCollectionUuids(collectionUuids);
		}
		MatrixResults results = freetextIndex.matrixSearch(search, getFields(info), true);
		BrowseRow rootNode = new BrowseRow(null, 0);
		BrowseRow displayRootNode = rootNode;
		rootNode.setTitle(LABEL_ROOT);
		rootNode.setViewLink(new HtmlLinkState(LABEL_ROOT, events.getNamedHandler("view", Collections.emptyList())));
		List<MatrixEntry> entries = results.getEntries();
		List<String> selectedValues = model.getValues();
		boolean showAll = Check.isEmpty(selectedValues);
		int selSize = !showAll ? selectedValues.size() : 0;
		for( MatrixEntry matrixEntry : entries )
		{
			List<String> values = matrixEntry.getFieldValues();
			BrowseRow parentNode = rootNode;
			List<String> valForView = new ArrayList<String>();
			int ind = 0;
			for( String value : values )
			{
				valForView.add(value);
				boolean matches = false;
				if( !showAll && selSize > ind )
				{
					matches = selectedValues.get(ind).equals(value);
				}
				if( value.isEmpty() || !showAll && (!matches && selSize - 1 >= ind) )
				{
					break;
				}
				HtmlLinkState linkState = new HtmlLinkState();
				linkState.setLabel(new TextLabel(value));
				BrowseRow row = parentNode.findChild(value);
				if( row == null )
				{
					row = new BrowseRow(value, matrixEntry.getCount());
					parentNode.addChild(row);
				}
				else
				{
					row.addCount(matrixEntry.getCount());
				}
				parentNode = row;
				if( !linkState.isDisabled() )
				{
					linkState.setClickHandler(events.getNamedHandler("view", new ArrayList<String>(valForView)));
				}
				row.setViewLink(linkState);
				if( matches && selSize - 1 == ind )
				{
					displayRootNode = row;
				}
				ind++;
			}
		}
		displayRootNode.setHide(true);
		model.setRootDisplayRow(displayRootNode);
		model.setRootRow(rootNode);
		return rootNode;
	}

	@EventHandlerMethod
	public void view(SectionInfo info, List<String> nodeValues)
	{
		Model model = getModel(info);
		if( !nodeValues.isEmpty() )
		{
			browseResults.startSearch(info);
		}
		model.setValues(nodeValues);
	}

	private List<String> getFields(SectionInfo info)
	{
		Model model = getModel(info);
		String nodes = model.getNodes();
		List<String> fields = new ArrayList<String>();
		String[] nodeList = nodes.split(","); //$NON-NLS-1$
		for( String node : nodeList )
		{
			fields.add(node.trim());
		}
		return fields;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return ""; //$NON-NLS-1$
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model();
	}

	public static class Model
	{
		@Bookmarked(parameter = "nodes", supported = true)
		private String nodes;
		@Bookmarked(parameter = "values")
		private List<String> values;
		@Bookmarked(parameter = "pageName", supported = true)
		private String pageName;
		@Bookmarked(parameter = "collections", supported = true)
		private Collection<String> collections;

		private BrowseRow rootRow;
		private BrowseRow rootDisplayRow;

		public String getPageName()
		{
			return pageName;
		}

		public void setPageName(String pageName)
		{
			this.pageName = pageName;
		}

		public String getNodes()
		{
			return nodes;
		}

		public void setNodes(String nodes)
		{
			this.nodes = nodes;
		}

		public List<String> getValues()
		{
			return values;
		}

		public void setValues(List<String> values)
		{
			this.values = values;
		}

		public BrowseRow getRootRow()
		{
			return rootRow;
		}

		public void setRootRow(BrowseRow rootRow)
		{
			this.rootRow = rootRow;
		}

		public Collection<String> getCollections()
		{
			return collections;
		}

		public void setCollections(Collection<String> collections)
		{
			this.collections = collections;
		}

		public BrowseRow getRootDisplayRow()
		{
			return rootDisplayRow;
		}

		public void setRootDisplayRow(BrowseRow rootDisplayRow)
		{
			this.rootDisplayRow = rootDisplayRow;
		}

	}

	public static class BrowseRow
	{
		private Label title;
		private final String id;
		private int count;
		private boolean hide;
		private List<BrowseRow> children;
		private Map<String, BrowseRow> childMap;
		private HtmlLinkState viewLink;

		public BrowseRow(String id, int count)
		{
			this.id = id;
			if( id != null )
			{
				title = new TextLabel(id);
			}
			this.count = count;
		}

		public void addCount(int count)
		{
			this.count += count;
		}

		public BrowseRow findChild(String value)
		{
			if( childMap != null )
			{
				return childMap.get(value);
			}
			return null;
		}

		public void addChild(BrowseRow child)
		{
			if( children == null )
			{
				children = new ArrayList<BrowseRow>();
				childMap = new HashMap<String, BrowseRow>();
			}
			children.add(child);
			childMap.put(child.getId(), child);
		}

		public int getCount()
		{
			return count;
		}

		public void setCount(int count)
		{
			this.count = count;
		}

		public List<BrowseRow> getChildren()
		{
			return children;
		}

		public void setChildren(List<BrowseRow> children)
		{
			this.children = children;
		}

		public boolean isHide()
		{
			return hide;
		}

		public void setHide(boolean hide)
		{
			this.hide = hide;
		}

		public Label getTitle()
		{
			return title;
		}

		public void setTitle(Label title)
		{
			this.title = title;
		}

		public String getId()
		{
			return id;
		}

		public HtmlLinkState getViewLink()
		{
			return viewLink;
		}

		public void setViewLink(HtmlLinkState viewLink)
		{
			this.viewLink = viewLink;
		}

	}

	public BrowseSearchResults getBrowseResults()
	{
		return browseResults;
	}

	public boolean isSearching(SectionInfo info)
	{
		return !Check.isEmpty(getModel(info).getValues());
	}

	public boolean isContextSpecified(SectionInfo info)
	{
		return getModel(info).getNodes() != null;
	}

}
