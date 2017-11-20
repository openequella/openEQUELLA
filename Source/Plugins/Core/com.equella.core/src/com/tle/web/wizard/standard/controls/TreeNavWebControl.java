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

package com.tle.web.wizard.standard.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import com.dytech.edge.common.Constants;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.beans.item.attachments.ModifiableAttachments;
import com.tle.beans.item.attachments.NavigationSettings;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.NameValue;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.FileSystemConstants;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.handler.AjaxEventCreator.SimpleResult;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.template.DialogTemplate;
import com.tle.web.viewitem.treeviewer.AbstractTreeViewerSection;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.attachments.ItemNavigationService;
import com.tle.web.viewurl.attachments.ItemNavigationService.NodeAddedCallback;
import com.tle.web.wizard.controls.AbstractHTMLControl;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CTreeNav;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.impl.WebRepository;
import com.tle.web.wizard.render.WizardFreemarkerFactory;
import com.tle.web.wizard.standard.controls.js.TreeNavControlLibrary;

import net.sf.json.JSONArray;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class TreeNavWebControl extends AbstractWebControl<TreeNavWebControl.TreeNavModel>
{
	@PlugKey("showsplit")
	private static Label SPLIT_LABEL;
	@PlugKey("showunassignedattachments")
	private static Label UNASSIGNED_ATTACHMENTS_LABEL;
	@PlugKey("tabs.defaultviewer")
	private static String TABS_DEFAULT;
	@PlugKey("tabs.noattachment")
	private static String TABS_NOATTACH;
	@PlugKey("tabs.remove")
	private static Label LABEL_TABS_REMOVE;
	@PlugKey("tabdialog.title")
	private static Label TAB_DIALOG_TITLE;
	@PlugKey("prepopconfirm")
	private static Label CONFIRM;

	@Component(name = "pp")
	@PlugKey("prepop")
	private Button prepopButton;
	@Component(name = "ac")
	@PlugKey("newchild")
	private Button addChildNodeButton;
	@Component(name = "as")
	@PlugKey("newsibling")
	private Button addSiblingNodeButton;
	@Component(name = "r")
	@PlugKey(value = "remove")
	private Button removeNodeButton;
	@Component(name = "mu")
	@PlugKey("alt.moveup")
	private Button moveUpButton;
	@Component(name = "md")
	@PlugKey("alt.movedown")
	private Button moveDownButton;
	@Component(name = "ta")
	@PlugKey("tabs.new")
	private Button tabAddButton;

	@Component(name = "ps")
	@PlugKey("save")
	private Button popupSaveButton;
	@Component(name = "pc")
	@PlugKey("cancel")
	private Button popupCancelButton;

	@Component(name = "ss")
	private Checkbox showSplit;
	@Component(name = "mn")
	private Checkbox showUnassignedAttachments;
	@Component(name = "nd")
	private TextField nodeDisplayName;
	@Component(name = "vl")
	private SingleSelectionList<Void> viewerList;
	@Component(name = "al")
	private SingleSelectionList<IAttachment> attachmentList;

	@Inject
	private DialogTemplate template;
	@Component
	private final TabDialog tabDialog = new TabDialog();

	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajaxCalls;
	@ViewFactory(name="wizardFreemarkerFactory")
	private WizardFreemarkerFactory factory;

	@Inject
	private AttachmentResourceService attachmentResourceService;
	@Inject
	private ViewItemService viewItemService;
	@Inject
	private ItemNavigationService itemNavService;

	private WebRepository repository;
	private CTreeNav treeControl;
	private Set<String> openNodes = new HashSet<String>();
	private AnonymousFunction viewerListAjaxCall;
	private ObjectExpression callbacks;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final TreeNavModel model = getModel(context);
		model.setFormName(getFormName());
		NavigationSettings nav = repository.getItem().getNavigationSettings();
		showSplit.setChecked(context, nav.isShowSplitOption());
		showUnassignedAttachments.setChecked(context, !nav.isManualNavigation());

		ObjectExpression controls = tabDialog.getControls();
		controls.put("tabDialogOpen", tabDialog.getOpenFunction());
		controls.put("tabDialogClose", tabDialog.getCloseFunction());
		TreeNavControlLibrary.setupTree(context, getTreeDef(), !isEnabled(), nodeDisplayName, tabAddButton,
			tabDialog.getPopupTabName(), tabDialog.getOk(), tabDialog.getCancel(), callbacks, controls,
			LABEL_TABS_REMOVE);

		addDisablers(context, addChildNodeButton, addSiblingNodeButton, removeNodeButton, moveUpButton, moveDownButton,
			prepopButton, tabAddButton, showSplit, nodeDisplayName, viewerList, attachmentList,
			tabDialog.getTabAttachmentList(), tabDialog.getTabViewerList(), tabDialog.getPopupTabName(),
			popupSaveButton);

		return factory.createResult("treenavwebcontrol.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		JSExpression prepopconfirm = new JSExpression()
		{
			@Override
			public String getExpression(RenderContext info)
			{
				return "tctl.confirmPrepop('" //$NON-NLS-1$
					+ CONFIRM.getText() //$NON-NLS-1$
					+ "')"; //$NON-NLS-1$
			}

			@Override
			public void preRender(PreRenderContext info)
			{
				// NOOP
			}
		};
		prepopButton
			.setClickHandler(new OverrideHandler(Js.iff(prepopconfirm, events.getSubmitValuesHandler("prepopulate")))); //$NON-NLS-1$
		attachmentList.setListModel(new AttachmentListModel(true));
		viewerList.setListModel(new ViewerListModel(attachmentList));

		attachmentList.registerUse();
		viewerList.registerUse();

		tabDialog.setInline(true);
		tabDialog.setTemplate(template);
		tabDialog.setStyleClass("tabdialog");

		// Ajax stuff
		final JSCallable optionsAjaxCall = ajaxCalls.getAjaxFunction("ajaxOptionsForAttachment"); //$NON-NLS-1$
		final ScriptVariable attachUuid = new ScriptVariable("attachmentUuid"); //$NON-NLS-1$
		final ScriptVariable callback = new ScriptVariable("callback"); //$NON-NLS-1$
		viewerListAjaxCall = new AnonymousFunction(new FunctionCallStatement(optionsAjaxCall, callback, attachUuid),
			attachUuid, callback);

		addChildNodeButton.setClickHandler(
			new OverrideHandler(TreeNavControlLibrary.CREATE_NODE, TreeNavControlLibrary.THE_TREE, false));
		addSiblingNodeButton.setClickHandler(
			new OverrideHandler(TreeNavControlLibrary.CREATE_NODE, TreeNavControlLibrary.THE_TREE, true));
		removeNodeButton
			.setClickHandler(new OverrideHandler(TreeNavControlLibrary.REMOVE_NODE, TreeNavControlLibrary.THE_TREE));
		moveUpButton
			.setClickHandler(new OverrideHandler(TreeNavControlLibrary.MOVE_NODE_UP, TreeNavControlLibrary.THE_TREE));
		moveDownButton
			.setClickHandler(new OverrideHandler(TreeNavControlLibrary.MOVE_NODE_DOWN, TreeNavControlLibrary.THE_TREE));

		showSplit.setLabel(SPLIT_LABEL);
		showUnassignedAttachments.setLabel(UNASSIGNED_ATTACHMENTS_LABEL);

		callbacks = new ObjectExpression();
		callbacks.put("tabViewerCallback", tabDialog.getTabViewerList().getDefaultAjaxUpdateCallback());
		callbacks.put("singleAttachmentSetterCallback", attachmentList.createSetFunction());
		callbacks.put("multiAttachmentSetterCallback", tabDialog.getTabAttachmentList().createSetFunction());
		callbacks.put("singleViewerSetterCallback", viewerList.createSetFunction());
		callbacks.put("multiViewerSetterCallback", tabDialog.getTabViewerList().createSetFunction());
	}

	@Override
	public Class<TreeNavModel> getModelClass()
	{
		return TreeNavModel.class;
	}

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		this.treeControl = (CTreeNav) control;
		this.repository = (WebRepository) control.getRepository();
		super.setWrappedControl(control);
	}

	protected Map<String, IAttachment> getAttachments()
	{
		final ModifiableAttachments attachments = repository.getAttachments();
		return UnmodifiableAttachments.convertToMapUuid(attachments.getList());
	}

	protected String getTreeDef()
	{
		return JSONArray.fromObject(AbstractTreeViewerSection.addChildNodes(treeControl.getRootNodes(),
			treeControl.getItemNavigationTree(), openNodes, null)).toString();
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		final TreeNavModel model = getModel(info);
		final Item item = repository.getItem();
		if( !model.isWasInitialised() )
		{
			Map<String, ItemNavigationNode> navMap = treeControl.getNavigationMap();
			Map<String, IAttachment> attachMap = getAttachments();

			// save model values
			HttpServletRequest request = info.getRequest();

			// TODO: remove getting of request vars and use model instead
			String[] edits = request.getParameterValues(getSectionId() + "edit"); //$NON-NLS-1$
			if( edits != null )
			{
				for( String id : edits )
				{
					ItemNavigationNode node;
					if( request.getParameter("new-" + id) != null ) //$NON-NLS-1$
					{
						node = new ItemNavigationNode(item);
						treeControl.getAllNavigation().add(node);
						navMap.put(id, node);
					}
					else
					{
						node = navMap.get(id);
					}
					String displayName = request.getParameter("dis-" + id); //$NON-NLS-1$
					String parentId = request.getParameter("par-" + id); //$NON-NLS-1$
					int index = Integer.parseInt(request.getParameter("ind-" + id)); //$NON-NLS-1$
					ItemNavigationNode pareNode = navMap.get(parentId);
					node.setIndex(index);
					node.setParent(pareNode);
					node.setName(displayName);

					List<ItemNavigationTab> tabs = new ArrayList<ItemNavigationTab>();
					String[] reqTabs = request.getParameterValues("tab-" + id); //$NON-NLS-1$
					if( reqTabs != null )
					{
						for( String tabValue : reqTabs )
						{
							String[] parts = tabValue.split("\\|"); //$NON-NLS-1$

							ItemNavigationTab tab = new ItemNavigationTab();
							tab.setNode(node);
							tab.setName(AbstractHTMLControl.urlDecode(parts[0]));
							tab.setViewer(parts.length < 2 ? null : AbstractHTMLControl.urlDecode(parts[1]));
							tab.setAttachment(parts.length < 3 ? null
								: (Attachment) attachMap.get(AbstractHTMLControl.urlDecode(parts[2])));

							tabs.add(tab);
						}
					}
					node.setTabs(tabs);
				}
			}
			String[] deletes = request.getParameterValues(getSectionId() + "deleted"); //$NON-NLS-1$
			if( deletes != null )
			{
				for( String id : deletes )
				{
					ItemNavigationNode node = navMap.get(id);
					if( node != null )
					{
						treeControl.getAllNavigation().remove(node);
					}
				}
			}
			String[] opens = request.getParameterValues(getSectionId() + "open"); //$NON-NLS-1$
			if( opens == null )
			{
				opens = new String[]{};
			}
			openNodes = new HashSet<String>(Arrays.asList(opens));
			common(info);
		}
	}

	@EventHandlerMethod
	public void prepopulate(SectionInfo info) throws Exception
	{
		Item item = repository.getItem();
		final Map<String, ItemNavigationNode> navMap = treeControl.getNavigationMap();
		navMap.clear();
		item.getTreeNodes().clear();

		Map<String, IAttachment> attachmentsToAdd = new HashMap<String, IAttachment>(getAttachments());
		prepopulateIms(info, item, attachmentsToAdd);

		itemNavService.populateTreeNavigationFromAttachments(item, item.getTreeNodes(),
			sortAttachments(attachmentsToAdd.values()), new NodeAddedCallback()
			{
				@NonNullByDefault(false)
				@Override
				public void execute(int index, ItemNavigationNode node)
				{
					navMap.put(Integer.toString(index), node);
				}
			});
		getModel(info).setWasInitialised(true);
		showUnassignedAttachments.setChecked(info, true);
		common(info);
	}

	protected List<IAttachment> sortAttachments(Collection<IAttachment> attachments)
	{
		List<IAttachment> attachList = new ArrayList<IAttachment>(attachments);
		Collections.sort(attachList, new Comparator<IAttachment>()
		{
			@NonNullByDefault(false)
			@Override
			public int compare(IAttachment arg0, IAttachment arg1)
			{
				String description0 = arg0.getDescription();
				String description1 = arg1.getDescription();
				if( description0 == null )
				{
					return -1;
				}
				if( description1 == null )
				{
					return 1;
				}
				return description0.compareToIgnoreCase(description1);
			}
		});
		return attachList;
	}

	private void prepopulateIms(SectionInfo info, Item item, Map<String, IAttachment> attachmentsToAdd) throws Exception
	{
		// remove existing IMSResource/METS attachments
		for( Attachment attachment : repository.getPackageAttachments() )
		{
			if( attachment.getAttachmentType() == AttachmentType.IMS && ((ImsAttachment) attachment).isExpand() )
			{
				attachmentsToAdd.remove(attachment.getUuid());
				item.getAttachments().remove(attachment);
			}
		}

		Attachments attachments = new UnmodifiableAttachments(item);
		List<ImsAttachment> imsAttachments = attachments.getList(AttachmentType.IMS);
		for( ImsAttachment imsAttachment : imsAttachments )
		{
			if( imsAttachment.isExpand() )
			{
				attachmentsToAdd.remove(imsAttachment.getUuid());
				repository.createPackageNavigation(info, imsAttachment.getUrl(),
					PathUtils.filePath(FileSystemConstants.IMS_FOLDER, imsAttachment.getUrl()), imsAttachment.getUrl(),
					true);
			}

			// remove this attachment from the list of attachments to add
			attachmentsToAdd.remove(imsAttachment.getUuid());
			repository.createPackageNavigation(info, imsAttachment.getUrl(),
				PathUtils.filePath(FileSystemConstants.IMS_FOLDER, imsAttachment.getUrl()), imsAttachment.getUrl(), true);
		}
	}

	protected void common(SectionInfo info)
	{
		Item item = repository.getItem();
		NavigationSettings nav = item.getNavigationSettings();
		nav.setShowSplitOption(showSplit.isChecked(info));
		nav.setManualNavigation(!showUnassignedAttachments.isChecked(info));
	}

	public static class TreeNavModel extends WebControlModel
	{
		protected String formName;
		private boolean wasInitialised;

		public TreeNavModel()
		{
			// nothing
		}

		public String getFormName()
		{
			return formName;
		}

		public void setFormName(String formName)
		{
			this.formName = formName;
		}

		public boolean isWasInitialised()
		{
			return wasInitialised;
		}

		public void setWasInitialised(boolean wasInitialised)
		{
			this.wasInitialised = wasInitialised;
		}
	}

	@AjaxMethod
	public SimpleResult ajaxOptionsForAttachment(SectionInfo info, String attachmentUuid)
	{
		return new SimpleResult(getOptionsForAttachment(info, attachmentUuid));
	}

	public List<Option<Void>> getOptionsForAttachment(SectionInfo info, String attachmentUuid)
	{
		List<Option<Void>> options = new ArrayList<Option<Void>>();
		options.add(new VoidKeyOption(TABS_DEFAULT, Constants.BLANK));
		Map<String, IAttachment> attachments = getAttachments();
		IAttachment attachment = attachments.get(attachmentUuid);
		if( attachment != null )
		{
			ViewableResource resource = attachmentResourceService.getViewableResource(info,
				getWebRepository().getViewableItem(), attachment);
			resource.setAttribute(ViewableResource.KEY_TARGETS_FRAME, true);
			List<NameValue> viewersList = viewItemService.getEnabledViewers(info, resource);
			for( NameValue viewerName : viewersList )
			{
				options.add(new SimpleOption<Void>(viewerName.getName(), viewerName.getValue(), null));
			}
		}
		return options;
	}

	public class ViewerListModel extends DynamicHtmlListModel<Void>
	{
		private final SingleSelectionList<IAttachment> attachList;

		public ViewerListModel(SingleSelectionList<IAttachment> attachmentList)
		{
			this.attachList = attachmentList;
		}

		@Override
		protected Iterable<Option<Void>> populateOptions(SectionInfo info)
		{
			String attachmentUuid = attachList.getSelectedValueAsString(info);
			return getOptionsForAttachment(info, attachmentUuid);
		}

		@Nullable
		@Override
		protected Iterable<Void> populateModel(SectionInfo info)
		{
			return null;
		}
	}

	public class AttachmentListModel extends DynamicHtmlListModel<IAttachment>
	{
		private final boolean includeNoneOption;

		public AttachmentListModel(boolean includeNoneOption)
		{
			this.includeNoneOption = includeNoneOption;
		}

		@Override
		protected Option<IAttachment> getTopOption()
		{
			if( includeNoneOption )
			{
				return new NameValueOption<IAttachment>(new BundleNameValue(TABS_NOATTACH, Constants.BLANK), null);
			}
			return null;
		}

		@Override
		protected Iterable<IAttachment> populateModel(SectionInfo info)
		{
			return sortAttachments(getAttachments().values());
		}

		@Override
		protected Option<IAttachment> convertToOption(SectionInfo info, IAttachment attachment)
		{
			String desc = attachment.getDescription();
			String uuid = attachment.getUuid();
			if( desc == null )
			{
				desc = uuid;
			}
			return new SimpleOption<IAttachment>(desc, uuid, attachment);
		}
	}

	public Button getPrepopButton()
	{
		return prepopButton;
	}

	public Button getAddChildNodeButton()
	{
		return addChildNodeButton;
	}

	public Button getAddSiblingNodeButton()
	{
		return addSiblingNodeButton;
	}

	public Button getRemoveNodeButton()
	{
		return removeNodeButton;
	}

	public Button getMoveUpButton()
	{
		return moveUpButton;
	}

	public Button getMoveDownButton()
	{
		return moveDownButton;
	}

	public Button getTabAddButton()
	{
		return tabAddButton;
	}

	public Button getPopupSaveButton()
	{
		return popupSaveButton;
	}

	public Button getPopupCancelButton()
	{
		return popupCancelButton;
	}

	public Checkbox getShowSplit()
	{
		return showSplit;
	}

	public Checkbox getShowUnassignedAttachments()
	{
		return showUnassignedAttachments;
	}

	public TextField getNodeDisplayName()
	{
		return nodeDisplayName;
	}

	public SingleSelectionList<Void> getViewerList()
	{
		return viewerList;
	}

	public SingleSelectionList<IAttachment> getAttachmentList()
	{
		return attachmentList;
	}

	public AnonymousFunction getViewerListAjaxCall()
	{
		return viewerListAjaxCall;
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}

	public class TabDialog extends EquellaDialog<DialogModel>
	{
		@PlugKey("tabdialog.ok")
		@Component
		private Button ok;

		@PlugKey("tabdialog.cancel")
		@Component
		private Button cancel;

		@Component(name = "tvl")
		private SingleSelectionList<Void> tabViewerList;
		@Component(name = "tal")
		private SingleSelectionList<IAttachment> tabAttachmentList;
		@Component(name = "ptn")
		private TextField popupTabName;

		@Override
		public void registered(String id, SectionTree tree)
		{
			super.registered(id, tree);
			ok.setComponentAttribute(ButtonType.class, ButtonType.SAVE);
			tabAttachmentList.setListModel(new AttachmentListModel(false));
			tabViewerList.setListModel(new ViewerListModel(tabAttachmentList));
			tabViewerList.registerUse();
			tabAttachmentList.registerUse();
		}

		@Override
		protected SectionRenderable getRenderableContents(RenderContext context)
		{
			return factory.createNormalResult("tabdialog.ftl", this);
		}

		@Override
		protected Collection<Button> collectFooterActions(RenderContext context)
		{
			return Lists.newArrayList(ok, cancel);
		}

		public void setTemplate(DialogTemplate template)
		{
			this.template = template;
		}

		public ObjectExpression getControls()
		{
			return new ObjectExpression("tabname", Jq.$(popupTabName), "tabviewer", Jq.$(tabViewerList), "tabatt",
				Jq.$(tabAttachmentList));
		}

		@Override
		protected String getContentBodyClass(RenderContext context)
		{
			return "tcd";
		}

		@Override
		protected Label getTitleLabel(RenderContext context)
		{
			return TAB_DIALOG_TITLE;
		}

		@Override
		public DialogModel instantiateDialogModel(SectionInfo info)
		{
			return new DialogModel();
		}

		public Button getOk()
		{
			return ok;
		}

		public Button getCancel()
		{
			return cancel;
		}

		public SingleSelectionList<Void> getTabViewerList()
		{
			return tabViewerList;
		}

		public SingleSelectionList<IAttachment> getTabAttachmentList()
		{
			return tabAttachmentList;
		}

		public TextField getPopupTabName()
		{
			return popupTabName;
		}
	}

	public TabDialog getTabDialog()
	{
		return tabDialog;
	}
}
