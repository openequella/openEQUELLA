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

package com.tle.web.itemadmin.section;

import static com.tle.web.sections.js.generic.statement.FunctionCallStatement.jscall;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema.CloneDefinition;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class ItemAdminXPathDialog extends AbstractOkayableDialog<ItemAdminXPathDialog.ItemAdminXPathDialogModel>
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private ItemDefinitionService itemdefService;

	@Component
	private Tree treeView;

	@PlugKey("whereclause.xpath.dialog.title")
	private static Label TITLE;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		treeView.setModel(new SchemaTreeModel());
		treeView.setLazyLoad(true);
		setAjax(true);
	}

	@Override
	public String getWidth()
	{
		return "500px";
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("dialog/xpathdialog.ftl", this);
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return null;
	}

	@EventHandlerMethod
	public void showDialog(SectionInfo info, String collectionId)
	{
		ItemAdminXPathDialogModel model = getModel(info);
		model.setCollectionId(collectionId);
		super.showDialog(info);
	}

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("showDialog");
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "xpathdialog";
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		return TITLE;
	}

	@Override
	public ItemAdminXPathDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new ItemAdminXPathDialogModel();
	}

	public static class ItemAdminXPathDialogModel extends DialogModel
	{
		@Bookmarked
		private String collectionId;
		private String schema;

		public String getSchema()
		{
			return schema;
		}

		public void setSchema(String schema)
		{
			this.schema = schema;
		}

		public String getCollectionId()
		{
			return collectionId;
		}

		public void setCollectionId(String collectionId)
		{
			this.collectionId = collectionId;
		}
	}

	public class SchemaTreeModel implements HtmlTreeModel
	{
		@Override
		public List<HtmlTreeNode> getChildNodes(SectionInfo info, @Nullable String xpath)
		{
			ItemDefinition collection = itemdefService.getByUuid(getModel(info).getCollectionId());
			if( collection.getSchema() == null )
			{
				return Collections.emptyList();
			}
			if( xpath == null )
			{
				xpath = "";
			}
			List<HtmlTreeNode> list = new ArrayList<HtmlTreeNode>();

			final PropBagEx schemaXml = collection.getSchema().withDefinition(new CloneDefinition());
			for( PropBagEx child : schemaXml.iterator(xpath + "/*") )
			{
				String name = child.getNodeName();
				if( isAttribute(child) )
				{
					name = "@" + name;
				}
				String fullpath = Check.isEmpty(xpath) ? name : MessageFormat.format("{0}/{1}", xpath, name);

				list.add(new SchemaTreeNode(name, fullpath, isLeaf(child), isSelectable(child)));
			}

			return list;
		}

		private boolean isAttribute(PropBagEx xml)
		{
			return xml.isNodeTrue("@attribute");
		}

		private boolean isSelectable(PropBagEx xml)
		{
			if( !xml.isNodeTrue("@field") )
			{
				return false;
			}

			for( PropBagEx subxml : xml.iterator() )
			{
				if( !isAttribute(subxml) )
				{
					return false;
				}
			}

			return true;
		}

		// If there are no nodes under the current node then it is a leaf
		// this could be expanded to hide nodes that have no selectable
		// nodes under it.
		private boolean isLeaf(PropBagEx xml)
		{
			return !xml.nodeExists("*");
		}
	}

	public class SchemaTreeNode implements HtmlTreeNode
	{
		private String name; // Node name
		private String id; // Full xpath
		private boolean leaf; // No children
		private boolean selectable;

		public SchemaTreeNode(String name, String xpath, boolean isLeaf, boolean selectable)
		{
			this.name = name;
			this.id = xpath;
			this.leaf = isLeaf;
			this.selectable = selectable;
		}

		@Override
		public String getId()
		{
			return id;
		}

		@Override
		public Label getLabel()
		{
			return null;
		}

		@Override
		public SectionRenderable getRenderer()
		{
			if( selectable )
			{
				HtmlLinkState link = new HtmlLinkState();
				link.setClickHandler(
					new OverrideHandler(jscall(getCloseFunction()), jscall(getOkCallback(), "/xml/" + id)));
				link.setLabel(new TextLabel(name));
				return new LinkRenderer(link);
			}
			else
			{
				return new LabelRenderer(new TextLabel(name));
			}
		}

		@Override
		public boolean isLeaf()
		{
			return leaf;
		}
	}

	public Tree getTreeView()
	{
		return treeView;
	}
}