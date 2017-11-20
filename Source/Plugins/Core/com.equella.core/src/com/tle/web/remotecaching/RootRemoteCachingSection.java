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

package com.tle.web.remotecaching;

import static com.tle.web.remotecaching.service.RemoteCachingWebService.KEY_ROOT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.Check;
import com.tle.common.activecache.settings.CacheSettings.Node;
import com.tle.common.activecache.settings.CacheSettings.Query;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.i18n.BundleCache;
import com.tle.core.i18n.BundleNameValue;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.remotecaching.service.RemoteCachingWebService;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.layout.OneColumnLayout;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.utils.SelectUserDialog;
import com.tle.web.sections.equella.utils.SelectedUser;
import com.tle.web.sections.equella.utils.UserLinkSection;
import com.tle.web.sections.equella.utils.UserLinkService;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.Tree;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.HtmlTreeModel;
import com.tle.web.sections.standard.model.HtmlTreeNode;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.list.CheckListRenderer;
import com.tle.web.settings.menu.SettingsUtils;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;

/**
 * @author larry
 * @author Aaron
 */
@SuppressWarnings("nls")
public class RootRemoteCachingSection extends OneColumnLayout<RootRemoteCachingSection.RemoteCachingModel>
{
	// private static final String AJAX_LEFTPANEL = "leftpanelajaxdiv";
	private static final String AJAX_INCLUDESEXCLUDES = "includesexcludesajaxdiv";
	private static final String AJAX_BUTTONS = "buttonajaxdiv";
	private static final String AJAX_ALL = "overallajaxdiv";

	private static final PluginResourceHelper urlHelper = ResourcesService
		.getResourceHelper(RootRemoteCachingSection.class);
	private static final IncludeFile INCLUDE = new IncludeFile(urlHelper.url("scripts/usrgrptree.js"));
	private static final ExternallyDefinedFunction SELECT = new ExternallyDefinedFunction("select", 3, INCLUDE);

	/**
	 * The value of the breadcrumb
	 */
	@PlugKey("remotecaching.title")
	private static Label TITLE_LABEL;
	@PlugKey("remotecaching.settings.save.receipt")
	private static Label SAVE_RECEIPT_LABEL;
	@PlugKey("label.group")
	private static Label LABEL_GROUP;
	@PlugKey("label.user")
	private static Label LABEL_USER;
	@PlugKey("confirm.remove")
	private static Label LABEL_CONFIRM_REMOVE;
	@PlugURL("images/group.png")
	private static String URL_GROUP_ICON;
	@PlugURL("images/user.png")
	private static String URL_USER_ICON;

	@Inject
	private ReceiptService receiptService;
	@Inject
	private ItemDefinitionService itemDefinitionService;
	@Inject
	private RemoteCachingPrivilegeTreeProvider securityProvider;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private RemoteCachingWebService remoteCachingWebService;
	@Inject
	private UserLinkService userLinkService;

	@PlugKey("remotecaching.enableuse")
	@Component(name = "eu")
	private Checkbox allowUse;
	@Component(name = "ut")
	private Tree usrGrpTree;
	@Component(name = "ic", ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
	private CollectionsList includesTable;
	@Component(name = "ec", ignoreForContext = BookmarkEvent.CONTEXT_BROWSERURL)
	private CollectionsList excludesTable;
	@PlugKey("button.adduser")
	@Component(name = "au")
	private Button addUserButton;
	@PlugKey("button.addgroup")
	@Component(name = "ag")
	private Button addGroupButton;
	@PlugKey("button.removeusergroup")
	@Component(name = "r")
	private Button removeUserGroupButton;
	@PlugKey("settings.save.button")
	@Component(name = "s")
	private Button saveButton;
	@Inject
	@Component(name = "aud")
	private RemoteCachingSelectUserDialog addUsersDialog;
	@Inject
	@Component(name = "agd")
	private RemoteCachingAddGroupDialog addGroupDialog;

	@AjaxFactory
	protected AjaxGenerator ajax;
	@EventFactory
	protected EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	private JSCallable selectNodeFunc;
	private UserLinkSection userLinkSection;

	@Override
	protected TemplateResult setupTemplate(RenderEventContext info)
	{
		securityProvider.checkAuthorised();

		final RemoteCachingModel model = getModel(info);

		if( !model.isLoaded() )
		{
			remoteCachingWebService.abandonCurrentChanges();

			final boolean enabled = remoteCachingWebService.getCacheSettings().isEnabled();
			model.setShowControls(enabled);

			allowUse.setChecked(info, enabled);

			if( enabled )
			{
				final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
				final Node rootNode = nodeCache.get(KEY_ROOT);
				model.setSelectedNode(rootNode.getUuid());
			}

			model.setLoaded(true);
		}
		else
		{
			model.setShowControls(allowUse.isChecked(info));
		}

		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
		final Node rootNode = nodeCache.get(KEY_ROOT);

		Node selectedNode = nodeCache.get(model.getSelectedNode());
		if( selectedNode == null )
		{
			// select the root
			model.setSelectedNode(rootNode.getUuid());
			selectedNode = rootNode;
		}
		model.setSelectedNodeName(getText(info, selectedNode));

		// Includes and excludes
		loadQueries(info, includesTable, selectedNode.getIncludes());
		loadQueries(info, excludesTable, selectedNode.getExcludes());

		removeUserGroupButton.setDisabled(info, rootNode.equals(selectedNode));
		addGroupButton.setDisabled(info, !selectedNode.isGroup());
		addUserButton.setDisabled(info, !selectedNode.isGroup());

		return new GenericTemplateResult(view.createNamedResult(BODY, "remotecaching.ftl", this));
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		allowUse.setClickHandler(getAjaxUpdate(tree, "toggleEnabled", AJAX_ALL));

		usrGrpTree.setModel(new RemoteCachingTreeModel());
		usrGrpTree.setAllowMultipleOpenBranches(true);

		includesTable.setListModel(new CollectionsListModel());
		excludesTable.setListModel(new CollectionsListModel());

		// Tree does not need an Ajax update when simply switching nodes
		selectNodeFunc = getAjaxUpdate(tree, "selectNode", AJAX_INCLUDESEXCLUDES, AJAX_BUTTONS);

		final JSCallable removeFunc = getAjaxUpdate(tree, "removeNodeFromTree", AJAX_ALL);
		removeUserGroupButton
			.setClickHandler(Js.handler(Js.call_s(removeFunc)).addValidator(new Confirm(LABEL_CONFIRM_REMOVE)));

		addUsersDialog.setOkCallback(getAjaxUpdate(tree, "addUserIntoTree", AJAX_ALL));
		addUserButton.setClickHandler(addUsersDialog.getOpenFunction());

		addGroupDialog.setOkCallback(getAjaxUpdate(tree, "addGroupIntoTree", AJAX_ALL));
		addGroupButton.setClickHandler(addGroupDialog.getOpenFunction());

		saveButton.setClickHandler(events.getNamedHandler("save"));

		userLinkSection = userLinkService.register(tree, id);
	}

	private UpdateDomFunction getAjaxUpdate(SectionTree tree, String eventHandlerName, String... ajaxIds)
	{
		return ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler(eventHandlerName),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), ajaxIds);
	}

	@Override
	protected void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs)
	{
		decorations.setTitle(TITLE_LABEL);
		crumbs.addToStart(SettingsUtils.getBreadcrumb());
	}

	@DirectEvent(priority = SectionEvent.PRIORITY_AFTER_EVENTS)
	public void updateUserExclusions(SectionInfo info)
	{
		final Set<String> exclusions = Sets.newHashSet();
		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
		for( Node node : nodeCache.values() )
		{
			if( !Check.isEmpty(node.getId()) )
			{
				exclusions.add(node.getId());
			}
		}
		addUsersDialog.setUserExclusions(info, exclusions);
	}

	@EventHandlerMethod
	public void toggleEnabled(SectionInfo info)
	{
		boolean show = allowUse.isChecked(info);
		getModel(info).setShowControls(show);
	}

	@EventHandlerMethod
	public void addUserIntoTree(SectionInfo info, String userJson)
	{
		final RemoteCachingModel model = getModel(info);
		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
		final Node node = nodeCache.get(model.getSelectedNode());

		// save the current node selections
		// TODO: Could possibly remove this in future. Need some sort of editing
		// session management
		doSave(info);

		final List<SelectedUser> users = SelectUserDialog.usersFromJsonString(userJson);
		final List<Node> children = node.getNodes();
		String newSelection = null;
		for( SelectedUser user : users )
		{
			final Node userNode = new Node(user.getUuid(), false);
			children.add(userNode);
			final String uuid = userNode.getUuid();
			nodeCache.put(uuid, userNode);
			newSelection = uuid;
		}

		if( newSelection != null )
		{
			model.setSelectedNode(newSelection);
		}

		// Unclear the selected collections and save the new node(s)
		loadQueries(info, includesTable, new ArrayList<Query>());
		loadQueries(info, excludesTable, new ArrayList<Query>());

		// save *this* edited node cache
		remoteCachingWebService.save(allowUse.isChecked(info), nodeCache.get(KEY_ROOT));
	}

	@EventHandlerMethod
	public void addGroupIntoTree(SectionInfo info, String groupName)
	{
		final RemoteCachingModel model = getModel(info);
		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
		final Node node = nodeCache.get(model.getSelectedNode());

		// save the current node selections
		// TODO: Could possibly remove this in future. Need some sort of editing
		// session management
		doSave(info);

		final List<Node> children = node.getNodes();

		final Node groupNode = new Node(groupName, true);
		children.add(groupNode);
		final String uuid = groupNode.getUuid();
		nodeCache.put(uuid, groupNode);

		model.setSelectedNode(uuid);

		// Unclear the selected collections and save the new node
		loadQueries(info, includesTable, new ArrayList<Query>());
		loadQueries(info, excludesTable, new ArrayList<Query>());

		// save *this* edited node cache
		remoteCachingWebService.save(allowUse.isChecked(info), nodeCache.get(KEY_ROOT));
	}

	@EventHandlerMethod
	public void removeNodeFromTree(SectionInfo info)
	{
		final RemoteCachingModel model = getModel(info);
		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
		final Node node = nodeCache.get(model.getSelectedNode());
		if( node != null )
		{
			final Node parent = findParent(nodeCache, node);
			if( parent == null )
			{
				throw new RuntimeException("Cannot remove the root node!");
			}
			parent.getNodes().remove(node);
			nodeCache.remove(model.getSelectedNode());

			// select the parent
			model.setSelectedNode(parent.getUuid());
			model.setSelectedNodeName(getText(info, parent));

			// Reload the Includes and excludes tables with the parent's values
			loadQueries(info, includesTable, parent.getIncludes());
			loadQueries(info, excludesTable, parent.getExcludes());

			// TODO: Could possibly remove this in future. Need some sort of
			// editing session management
			doSave(info);
		}
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		doSave(info);
		receiptService.setReceipt(SAVE_RECEIPT_LABEL);
	}

	@EventHandlerMethod
	public void selectNode(SectionInfo info, String uuid)
	{
		final RemoteCachingModel model = getModel(info);
		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
		final Node oldSelection = nodeCache.get(model.getSelectedNode());

		// save the current collection selections
		saveQueries(info, includesTable, oldSelection.getIncludes());
		saveQueries(info, excludesTable, oldSelection.getExcludes());

		// sanity check
		final Node newSelection = nodeCache.get(uuid);
		if( newSelection == null )
		{
			throw new RuntimeException("Unexpected failure to find node with id = " + uuid);
		}
		model.setSelectedNode(uuid);
	}

	@EventHandlerMethod
	public void removeCollection(SectionInfo info, long collectionId, boolean inclusions)
	{
		final RemoteCachingModel model = getModel(info);
		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
		final Node node = nodeCache.get(model.getSelectedNode());

		final List<Query> incExList = (inclusions ? node.getIncludes() : node.getExcludes());
		final Iterator<Query> incExIterator = incExList.iterator();
		while( incExIterator.hasNext() )
		{
			final Query q = incExIterator.next();
			if( q.getItemdef() == collectionId )
			{
				incExIterator.remove();
				return;
			}
		}
	}

	private void doSave(SectionInfo info)
	{
		final RemoteCachingModel model = getModel(info);
		final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();

		// save the queries
		final Node selectedNode = nodeCache.get(model.getSelectedNode());
		if( selectedNode != null )
		{
			saveQueries(info, includesTable, selectedNode.getIncludes());
			saveQueries(info, excludesTable, selectedNode.getExcludes());
		}
		remoteCachingWebService.save(allowUse.isChecked(info), nodeCache.get(KEY_ROOT));
	}

	private void saveQueries(SectionInfo info, MultiSelectionList<ItemDefinition> collections, List<Query> queries)
	{
		queries.clear();
		for( ItemDefinition collection : collections.getSelectedValues(info) )
		{
			final Query q = new Query();
			q.setUuid(collection.getUuid());
			queries.add(q);
		}
	}

	private void loadQueries(SectionInfo info, MultiSelectionList<ItemDefinition> collections, List<Query> queries)
	{
		final List<String> uuids = Lists.newArrayList();
		for( Query q : queries )
		{
			String uuid = q.getUuid();
			if( Check.isEmpty(uuid) )
			{
				uuid = itemDefinitionService.get(q.getItemdef()).getUuid();
			}
			uuids.add(uuid);
		}
		collections.setSelectedStringValues(info, uuids);
	}

	private Node findParent(Map<String, Node> nodeCache, Node node)
	{
		Node root = nodeCache.get(KEY_ROOT);
		if( root.equals(node) )
		{
			return null;
		}
		return recurseFindParent(root, node);
	}

	private Node recurseFindParent(Node n, Node find)
	{
		for( Node child : n.getNodes() )
		{
			if( child == find )
			{
				return n;
			}
			Node p = recurseFindParent(child, find);
			if( p != null )
			{
				return p;
			}
		}
		return null;
	}

	private String getText(SectionInfo info, Node node)
	{
		if( Check.isEmpty(node.getName()) && !Check.isEmpty(node.getId()) )
		{
			return userLinkSection.createLink(info, node.getId()).getLabel().getText();
		}
		return node.getName();
	}

	@Override
	public Class<RemoteCachingModel> getModelClass()
	{
		return RemoteCachingModel.class;
	}

	public Checkbox getAllowUse()
	{
		return allowUse;
	}

	public Tree getUsrGrpTree()
	{
		return usrGrpTree;
	}

	public MultiSelectionList<ItemDefinition> getIncludesTable()
	{
		return includesTable;
	}

	public MultiSelectionList<ItemDefinition> getExcludesTable()
	{
		return excludesTable;
	}

	public Button getAddUserButton()
	{
		return addUserButton;
	}

	public Button getAddGroupButton()
	{
		return addGroupButton;
	}

	public Button getRemoveUserGroupButton()
	{
		return removeUserGroupButton;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public class RemoteCachingTreeModel implements HtmlTreeModel
	{
		@Override
		public List<HtmlTreeNode> getChildNodes(SectionInfo info, String id)
		{
			final List<HtmlTreeNode> list = Lists.newArrayList();
			final Map<String, Node> nodeCache = remoteCachingWebService.getNodeCache();
			final RemoteCachingModel model = getModel(info);

			// empty id applies to the root. We presume not otherwise
			if( id == null )
			{
				final Node node = nodeCache.get(KEY_ROOT);
				final String uuid = node.getUuid();
				final RemoteCachingTreeNode nctn = new RemoteCachingTreeNode(uuid, getText(info, node),
					node.getNodes().isEmpty(), model.getSelectedNode().equals(uuid), true);
				list.add(nctn);
			}
			else
			{
				final Node node = nodeCache.get(id);
				if( node != null )
				{
					for( Node n2 : node.getNodes() )
					{
						final String uuid = n2.getUuid();
						final RemoteCachingTreeNode nctn = new RemoteCachingTreeNode(uuid, getText(info, n2),
							n2.getNodes().isEmpty(), model.getSelectedNode().equals(uuid), n2.getId() == null);
						list.add(nctn);
					}
				}
				else
				{
					// BANG!
					throw new Error();
				}
			}

			return list;
		}
	}

	public class RemoteCachingTreeNode implements HtmlTreeNode
	{
		private final String id;
		private final String textName;
		private final boolean leaf;
		private final boolean selected;
		private final boolean group;

		public RemoteCachingTreeNode(String id, String textName, boolean leaf, boolean selected, boolean group)
		{
			this.id = id;
			this.textName = textName;
			this.leaf = leaf;
			this.selected = selected;
			this.group = group;
		}

		@Override
		public String getId()
		{
			return id;
		}

		/**
		 * A little manip to ensure the link id's are unique. The Js call is to
		 * enable javascript to set a class identifiable by the stylesheet as a
		 * highlight colour
		 */
		@Override
		public SectionRenderable getRenderer()
		{
			final ImageRenderer icon = (group ? new ImageRenderer(URL_GROUP_ICON, LABEL_GROUP)
				: new ImageRenderer(URL_USER_ICON, LABEL_USER));
			icon.addClass("usergroupicon");

			final HtmlLinkState link = new HtmlLinkState();
			link.setLabel(new TextLabel(textName));
			final LinkRenderer linkRenderer = new LinkRenderer(link);
			linkRenderer.setNestedRenderable(
				CombinedRenderer.combineMultipleResults(icon, new LabelRenderer(new TextLabel(textName))));

			final DivRenderer divRenderer = new DivRenderer("");
			divRenderer.setId("ctn_" + id);
			divRenderer.addClass("cachingNode");
			if( selected )
			{
				divRenderer.addClass("selected");
			}

			link.setClickHandler(
				new StatementHandler(Js.call_s(SELECT, Jq.$(usrGrpTree), Jq.$(divRenderer), selectNodeFunc, id)));

			return divRenderer.setNestedRenderable(linkRenderer);
		}

		@Override
		public Label getLabel()
		{
			// This method shouldn't be executed if we are returning a
			// SectionRenderable from getRenderer().
			throw new Error();
		}

		@Override
		public boolean isLeaf()
		{
			return leaf;
		}
	}

	public static class CollectionsList extends MultiSelectionList<ItemDefinition>
	{
		public CollectionsList()
		{
			super();
			setDefaultRenderer("checklist"); //$NON-NLS-1$
		}

		@Override
		public void rendererSelected(RenderContext info, SectionRenderable renderer)
		{
			CheckListRenderer clrenderer = (CheckListRenderer) renderer;
			clrenderer.setAsList(true);
		}
	}

	protected class CollectionsListModel extends DynamicHtmlListModel<ItemDefinition>
	{
		public CollectionsListModel()
		{
			setSort(true);
		}

		@Override
		protected Iterable<ItemDefinition> populateModel(SectionInfo info)
		{
			return itemDefinitionService.enumerate();
		}

		@Override
		protected Option<ItemDefinition> convertToOption(SectionInfo info, ItemDefinition collection)
		{
			return new NameValueOption<ItemDefinition>(
				new BundleNameValue(collection.getName(), collection.getUuid(), bundleCache), collection);
		}
	}

	public static class RemoteCachingModel extends OneColumnLayout.OneColumnLayoutModel
	{
		boolean showControls;
		@Bookmarked(name = "l")
		private boolean loaded;
		@Bookmarked(name = "csn")
		private String selectedNode;
		private String selectedNodeName;

		public boolean isShowControls()
		{
			return showControls;
		}

		public void setShowControls(boolean showControls)
		{
			this.showControls = showControls;
		}

		public boolean isLoaded()
		{
			return loaded;
		}

		public void setLoaded(boolean loaded)
		{
			this.loaded = loaded;
		}

		public String getSelectedNode()
		{
			return selectedNode;
		}

		public void setSelectedNode(String selectedNode)
		{
			this.selectedNode = selectedNode;
		}

		public String getSelectedNodeName()
		{
			return selectedNodeName;
		}

		public void setSelectedNodeName(String selectedNodeName)
		{
			this.selectedNodeName = selectedNodeName;
		}
	}
}