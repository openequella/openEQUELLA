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

package com.tle.web.viewitem.treeviewer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.edge.common.Constants;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.beans.item.attachments.LinkAttachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.i18n.BundleCache;
import com.tle.core.url.URLCheckerService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SimpleBookmarkModifier;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.NavBar;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.BookmarkAndModify;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.generic.CachedData.CacheFiller;
import com.tle.web.sections.jquery.libraries.JQueryTabs;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.ItemNameLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.template.Decorations;
import com.tle.web.template.Decorations.FullScreen;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.treeviewer.js.TreeLibrary;
import com.tle.web.viewurl.ViewItemResource;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewItemViewer;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

import net.sf.json.JSONArray;

@SuppressWarnings("nls")
@NonNullByDefault
public abstract class AbstractTreeViewerSection<M extends AbstractTreeViewerModel> extends AbstractPrototypeSection<M>
	implements
		ViewItemViewer
{
	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private IntegrationService integrationService;
	@Inject
	private URLCheckerService urlCheckerService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private RootItemFileSection rootSection;

	@Inject
	@Component
	private NavBar navBar;
	@Component
	private Link title;
	@Component
	private Link split;

	@Component
	@PlugKey(value = "navbar.first", icon = Icon.JUMP_TO_FIRST)
	private Button first;
	@Component
	@PlugKey(value = "navbar.prev", icon = Icon.PREV)
	private Button prev;
	@Component
	@PlugKey(value = "navbar.next", icon = Icon.NEXT)
	private Button next;
	@Component
	@PlugKey(value = "navbar.last", icon = Icon.JUMP_TO_LAST)
	private Button last;

	protected interface NodeUrlGenerator
	{
		@Nullable
		String getUrlForNode(ItemNavigationNode node);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		split.setStyleClass("splitview");

		navBar.setTitle(title);
		navBar.buildRight().divider().action(split);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		registerPathMappings(rootSection);
	}

	protected abstract void registerPathMappings(RootItemFileSection rootSection);

	@Override
	public Collection<String> ensureOnePrivilege()
	{
		return VIEW_ITEM_AND_VIEW_ATTACHMENTS_PRIV;
	}

	@Nullable
	@Override
	public SectionResult view(RenderContext info, ViewItemResource resource)
	{
		SectionUtils.preRender(info, TreeLibrary.INCLUDE);
		AbstractTreeViewerModel model = getModel(info);
		model.setResource(resource);

		// dispatch to view
		String method = model.getMethod();
		if( method != null )
		{
			return (SectionResult) SectionUtils.dispatchToMethod(method, this, info);
		}

		Decorations decorations = Decorations.getDecorations(info);
		decorations.setFullscreen(FullScreen.YES_WITH_TOOLBAR);
		decorations.clearAllDecorations();

		final ViewableItem<Item> vitem = resource.getViewableItem();
		final Item item = vitem.getItem();
		final BundleLabel itemName = new ItemNameLabel(item, bundleCache);
		decorations.setTitle(itemName);
		prepareTitle(info, title, itemName, resource);

		// Redmine #5519 - we don't want any navigation back to EQUELLA summary
		// if we're looking at a resource from an external LMS
		if( integrationService.isInIntegrationSession(info) )
		{
			model.setHideNavBar(true);
			decorations.setFullscreen(FullScreen.YES);
		}

		split.setDisplayed(info, item.getNavigationSettings().isShowSplitOption());

		ItemNavigationTree tree = getNavigationTree(info);
		boolean oneAttachment = !moreThanOneNodeWithAttachment(tree.getNavigationMap().values());
		// hide tree if only one attachment
		model.setHideTree(oneAttachment);
		model.setHideNavControls(oneAttachment);
		model.setDefinition(getTreeDef(info, tree, new HashSet<String>()));
		return viewFactory.createTemplateResult("treeviewer.ftl", this);
	}

	protected abstract void prepareTitle(SectionInfo info, Link title, BundleLabel itemName, ViewItemResource resource);

	private boolean moreThanOneNodeWithAttachment(@Nullable Collection<ItemNavigationNode> treeNodes)
	{
		boolean foundFirst = false;
		if( treeNodes != null )
		{
			for( ItemNavigationNode node : treeNodes )
			{
				if( nodeHasAttachment(node) )
				{
					if( foundFirst )
					{
						return true;
					}
					else
					{
						foundFirst = true;
					}
				}
			}
		}
		return false;
	}

	@EventHandlerMethod(preventXsrf = false)
	public void viewNode(SectionContext context, String node)
	{
		AbstractTreeViewerModel model = getModel(context);
		model.setMethod("view");
		model.setNode(node);
	}

	private ItemNavigationNode getNodeToView(SectionInfo info)
	{
		AbstractTreeViewerModel model = getModel(info);
		ItemNavigationTree tree = getNavigationTree(info);
		ItemNavigationNode node = tree.getNavigationMap().get(model.getNode());
		if( node == null && tree.getRootNodes().size() > 0 )
		{
			node = tree.getRootNodes().get(0);
		}
		return node;
	}

	@Nullable
	public SectionResult view(RenderContext info) throws Exception
	{
		ItemNavigationNode node = getNodeToView(info);
		viewingNode(node);

		Object result = getRendererForNode(info, node);
		if( result instanceof Bookmark )
		{
			info.forwardToUrl(((Bookmark) result).getHref());
			return null;
		}
		return (SectionResult) result;
	}

	protected void viewingNode(ItemNavigationNode node)
	{
		// nothing - extend for scorm
	}

	public NavBar getNavBar()
	{
		return navBar;
	}

	public Button getFirst()
	{
		return first;
	}

	public Button getPrev()
	{
		return prev;
	}

	public Button getNext()
	{
		return next;
	}

	public Button getLast()
	{
		return last;
	}

	public ItemNavigationTree getNavigationTree(SectionInfo info)
	{
		return getModel(info).getNavigationTree().get(info, new CacheFiller<ItemNavigationTree>()
		{
			@Override
			public ItemNavigationTree get(SectionInfo ifo)
			{
				return new ItemNavigationTree(getTreeNodes(ifo));
			}
		});
	}

	protected boolean nodeHasAttachment(ItemNavigationNode node)
	{
		if( node.getTabs() != null )
		{
			for( ItemNavigationTab tab : node.getTabs() )
			{
				Attachment attachment = tab.getAttachment();
				if( attachment != null )
				{
					// if it's a Link, check if it's disabled
					if( attachment instanceof LinkAttachment
						&& urlCheckerService.isUrlDisabled(((LinkAttachment) attachment).getUrl()) )
					{
						continue;
					}
					return true;
				}
			}
		}
		return false;
	}

	public static List<TreeNode> addChildNodes(List<ItemNavigationNode> nodes, ItemNavigationTree tree,
		Set<String> openNodes, @Nullable NodeUrlGenerator urlGen)
	{
		List<TreeNode> treeNodes = new ArrayList<TreeNode>();
		for( final ItemNavigationNode node : nodes )
		{
			TreeNode treeNode = new TreeNode();
			treeNode.setUuid(node.getUuid());
			treeNode.setName(node.getName());
			treeNode.setOpen(openNodes.contains(node.getUuid()));
			treeNode.setIcon(node.getIcon());
			if( urlGen != null )
			{
				treeNode.setUrl(urlGen.getUrlForNode(node));
			}
			List<ItemNavigationTab> tabs = node.getTabs();
			List<TreeTab> tabList = new ArrayList<TreeTab>();
			for( ItemNavigationTab tab : tabs )
			{
				TreeTab treeTab = new TreeTab();
				treeTab.setIndex(tab.getNode().getIndex());
				treeTab.setName(tab.getName());
				treeTab.setViewer(tab.getViewer());
				if( tab.getAttachment() != null )
				{
					treeTab.setAttachment(tab.getAttachment().getUuid());
				}
				tabList.add(treeTab);
			}
			treeNode.setTabs(tabList);
			List<ItemNavigationNode> children = tree.getChildMap().get(node);
			if( !Check.isEmpty(children) )
			{
				treeNode.setChildren(addChildNodes(children, tree, openNodes, urlGen));
			}
			else
			{
				treeNode.setNokids(true);
			}
			treeNodes.add(treeNode);
		}
		return treeNodes;
	}

	protected String getTreeDef(final SectionInfo info, ItemNavigationTree tree, Set<String> openNodes)
	{
		List<TreeNode> nodes = addChildNodes(tree.getRootNodes(), tree, openNodes, new NodeUrlGenerator()
		{
			@Nullable
			@Override
			public String getUrlForNode(ItemNavigationNode node)
			{
				if( nodeHasAttachment(node) )
				{
					return new BookmarkAndModify(info, events.getNamedModifier("viewNode", node.getUuid())).getHref();
				}
				return null;
			}
		});
		return JSONArray.fromObject(nodes).toString();
	}

	@Nullable
	protected Object getRendererForNode(final RenderContext info, ItemNavigationNode node) throws Exception
	{
		List<ItemNavigationTab> tabs = node.getTabs();
		if( tabs == null || tabs.size() == 0 )
		{
			throw new RuntimeException(CurrentLocale.get("com.tle.web.viewitem.treeviewer.error.notabs"));
		}

		final int count = tabs.size();
		if( count == 1 )
		{
			return getRealUrlForTab(info, tabs.get(0));
		}
		else
		{
			AbstractTreeViewerModel model = getModel(info);
			model.setTabs(Lists.transform(tabs, new Function<ItemNavigationTab, NameValue>()
			{
				@NonNullByDefault(false)
				@Override
				public NameValue apply(ItemNavigationTab tab)
				{
					Bookmark tabUrl = getRealUrlForTab(info, tab);
					String href = Constants.BLANK;
					if( tabUrl != null )
					{
						href = tabUrl.getHref();
					}
					return new NameValue(tab.getName(), href);
				}
			}));

			Decorations decorations = Decorations.getDecorations(info);
			decorations.setFullscreen(FullScreen.YES_WITH_TOOLBAR);
			decorations.clearAllDecorations();

			info.preRender(JQueryTabs.PRERENDER);

			return viewFactory.createTemplateResult("viewtabs.ftl", this);
		}
	}

	@Nullable
	public Bookmark getRealUrlForTab(SectionInfo info, ItemNavigationTab tab)
	{
		Attachment attachment = tab.getAttachment();
		if( attachment != null )
		{
			AbstractTreeViewerModel model = getModel(info);
			ViewItemResource resource = model.getResource();
			ViewableItem viewableItem = resource.getViewableItem();

			ViewableResource viewableResource = attachmentResourceService.getViewableResource(info, viewableItem,
				attachment);
			ViewItemUrl viewItemUrl = viewableResource.createDefaultViewerUrl();
			if( !Check.isEmpty(tab.getViewer()) )
			{
				viewItemUrl.setViewer(tab.getViewer());
			}
			else if( !Check.isEmpty(attachment.getViewer()) )
			{
				viewItemUrl.setViewer(attachment.getViewer());
			}
			viewItemUrl.addFlag(ViewItemUrl.FLAG_FULL_URL | ViewItemUrl.FLAG_PRESERVE_PARAMS);
			viewItemUrl.setShowNav(false);
			viewItemUrl.add(new SimpleBookmarkModifier("hideNavBar", "true"));
			return viewItemUrl;
		}
		return null;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "tree";
	}

	protected List<ItemNavigationNode> getTreeNodes(SectionInfo info)
	{
		// Default implementation. Subclasses to override.
		return getModel(info).getResource().getViewableItem().getItem().getTreeNodes();
	}
}
