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

package com.tle.web.selection.section;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.core.item.service.ItemResolver;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.AjaxMethod;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.layout.ContentLayout;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryDraggable;
import com.tle.web.sections.jquery.libraries.JQueryDroppable;
import com.tle.web.sections.jquery.libraries.effects.JQueryUIEffects;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.AbstractCallable;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.ReloadFunction;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.MappedBooleans;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.selection.CourseListFolderAjaxUpdateData;
import com.tle.web.selection.CourseListFolderUpdateCallback;
import com.tle.web.selection.SelectedResourceKey;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.TargetFolder;
import com.tle.web.selection.TargetStructure;
import com.tle.web.selection.event.AllAttachmentsSelectorEvent;
import com.tle.web.selection.event.AllAttachmentsSelectorEventListener;
import com.tle.web.selection.event.AttachmentSelectorEvent;
import com.tle.web.selection.event.AttachmentSelectorEventListener;
import com.tle.web.selection.event.ItemSelectorEvent;
import com.tle.web.selection.event.ItemSelectorEventListener;
import com.tle.web.selection.event.PackageSelectorEvent;
import com.tle.web.selection.event.PackageSelectorEventListener;
import com.tle.web.selection.section.RootSelectionSection.Layout;

@SuppressWarnings("nls")
@NonNullByDefault
public class CourseListSection extends AbstractPrototypeSection<CourseListSection.CourseListModel>
	implements
		HtmlRenderer,
		AttachmentSelectorEventListener,
		AllAttachmentsSelectorEventListener,
		ItemSelectorEventListener,
		PackageSelectorEventListener
{
	private static IncludeFile JS = new IncludeFile(
		ResourcesService.getResourceHelper(CourseListSection.class).url("scripts/courselist.js"),
		JQueryDraggable.PRERENDER, JQueryDroppable.PRERENDER, JQueryUIEffects.TRANSFER);
	private static final JSCallAndReference COURSE_LIST_CLASS = new ExternallyDefinedFunction("CourseList", JS);
	private static ExternallyDefinedFunction SETUP_FUNCTION = new ExternallyDefinedFunction(COURSE_LIST_CLASS,
		"setupDraggables", 4);

	private static final String COURSE_LIST_AJAX = "courselistajax";
	@Inject
	private SelectionService selectionService;
	@Inject
	private ItemResolver itemResolver;

	@PlugKey("courselist.button.save")
	@Component(name = "s")
	private Button saveButton;
	@PlugKey("courselist.button.cancel")
	@Component(name = "c")
	private Button cancelButton;
	@Component(name = "f")
	private MappedBooleans selection;
	@Inject
	@Component
	private SelectionsDialog versionDialog;
	@PlugKey("courselist.link.review")
	@Component
	private Link viewSelections;

	@ViewFactory
	private FreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	private JSCallable selectAttachmentFunction;
	private JSCallable selectAllAttachmentsFunction;
	private JSCallable selectItemFunction;
	private JSCallable selectPackageFunction;
	private JSCallable onDropAjaxUpdateFunction;

	@AjaxMethod
	public JSONResponseCallback reloadFolder(AjaxRenderContext context, CourseListFolderAjaxUpdateData controlData)
	{
		CourseListFolderUpdateCallback callback = new CourseListFolderUpdateCallback(context, controlData);
		context.addAjaxDivs(COURSE_LIST_AJAX);
		return callback;
	}

	/**
	 * Wraps the event supplied with the reloadFolder+updateTargetFolder code
	 * 
	 * @param drop
	 * @param event
	 * @param ajaxIds
	 * @return
	 */
	public JSCallable getReloadFunction(final boolean drop, @Nullable final ParameterizedEvent event,
		final String... ajaxIds)
	{
		final JSCallable reloadFolderFunction = CourseListFolderUpdateCallback
			.getReloadFunction(ajax.getAjaxFunction("reloadFolder"));
		return new AbstractCallable()
		{
			@Override
			public void preRender(PreRenderContext info)
			{
				info.preRender(reloadFolderFunction);
			}

			@Override
			public int getNumberOfParams(RenderContext context)
			{
				if( event != null )
				{
					return event.getParameterCount();
				}
				return 0;
			}

			@Override
			protected String getCallExpression(RenderContext info, JSExpression[] params)
			{
				ArrayExpression eventArray = new ArrayExpression();
				if( event != null )
				{
					eventArray.add(event.getEventId());
					eventArray.addAll(params);
				}
				// FIXME: don't use p2? p2 is the second param of the supplied
				// event
				return new FunctionCallExpression(reloadFolderFunction, drop ? new ScriptVariable("p2") : null,
					eventArray, new ArrayExpression((Object[]) ajaxIds)).getExpression(info);
			}
		};
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( isVisible(context) )
		{
			final SelectionSession ss = selectionService.getCurrentSession(context);
			final TargetStructure structure = ss.getStructure();
			final CourseListModel model = getModel(context);

			if( structure.isNoTargets() )
			{
				saveButton.setDisplayed(context, false);
				cancelButton.setDisplayed(context, false);
				viewSelections.setDisplayed(context, false);
				model.setNoTargets(true);
			}
			else
			{
				saveButton.addReadyStatements(context, SETUP_FUNCTION, onDropAjaxUpdateFunction,
					ajax.getAjaxFunction("selectFolder"), ss.isSelectItem(), ss.isSelectAttachments());
				if( ss.isCancelDisabled() )
				{
					saveButton.getState(context).addClass("single");
					cancelButton.setDisplayed(context, false);
				}

				if( Check.isEmpty(getSelectedFolders(context)) )
				{
					selection.setCheckedSet(context, Collections.singleton(ss.getTargetFolder()));
				}

				viewSelections.setClickHandler(context, new OverrideHandler(versionDialog.getOpenFunction(), "", true));
			}

			model.setRoot(convertStructure(context, structure));

			return viewFactory.createNamedResult("courses", "selection/courselist.ftl", this);
		}
		return null;
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.addListener(null, AllAttachmentsSelectorEventListener.class, this);
		tree.addListener(null, AttachmentSelectorEventListener.class, this);
		tree.addListener(null, ItemSelectorEventListener.class, this);
		tree.addListener(null, PackageSelectorEventListener.class, this);

		// Maybe this could be similar to search updates, ie. scattered AJAX
		// updates (so results could be highlighted when already selected?)
		onDropAjaxUpdateFunction = getReloadFunction(true, events.getEventHandler("onDrop"), COURSE_LIST_AJAX);
		// ajax.getAjaxUpdateDomFunction(tree, null,
		// events.getEventHandler("onDrop"),
		// ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE),
		// "courselistajax");

		// TODO: use the getReloadFunction as well
		selectAllAttachmentsFunction = ajax.getAjaxUpdateDomFunction(tree, null,
			events.getEventHandler("selectAllAttachments"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE),
			COURSE_LIST_AJAX);

		selectAttachmentFunction = getReloadFunction(false, events.getEventHandler("selectAttachment"),
			COURSE_LIST_AJAX);
		selectItemFunction = getReloadFunction(false, events.getEventHandler("selectItem"), COURSE_LIST_AJAX);
		selectPackageFunction = getReloadFunction(false, events.getEventHandler("selectPackage"), COURSE_LIST_AJAX);

		saveButton.addClickStatements(events.getNamedHandler("save"));
		cancelButton.addClickStatements(events.getNamedHandler("cancel"));

		versionDialog.setOkCallback(new ReloadFunction(false));
		versionDialog.setCancelCallback(new ReloadFunction(false));
	}

	public SectionInfo createSearchForward(SectionInfo info)
	{
		return info.createForward("/access/course/searching.do");
	}

	public boolean isApplicable(SectionInfo info)
	{
		final CourseListModel model = getModel(info);
		final Boolean applicable = model.getApplicable();
		if( applicable != null )
		{
			return applicable;
		}
		if( ContentLayout.getLayout(info) == ContentLayout.ONE_COLUMN )
		{
			return false;
		}

		boolean applies = false;
		final SelectionSession ss = selectionService.getCurrentSession(info);
		if( ss != null )
		{
			if( ss.getLayout().equals(Layout.COURSE) )
			{
				applies = true;
			}

		}

		model.setApplicable(applies);
		return applies;
	}

	private boolean isVisible(SectionInfo info)
	{
		final CourseListModel model = getModel(info);
		Boolean visible = model.getVisible();
		if( visible == null )
		{
			if( isApplicable(info) )
			{
				// visible if not doing a contribution or remote repo search etc
				final List<CourseListVetoSection> vetos = info.lookupSections(CourseListVetoSection.class);
				visible = vetos.isEmpty();
			}
			else
			{
				visible = false;
			}

			model.setVisible(visible);
		}
		return visible && !info.isErrored();
	}

	@Override
	public void supplyFunction(SectionInfo info, PackageSelectorEvent event)
	{
		if( isVisible(info) )
		{
			event.setFunction(selectPackageFunction);
			event.stopProcessing();
		}
	}

	@Override
	public void supplyFunction(SectionInfo info, ItemSelectorEvent event)
	{
		if( isVisible(info) )
		{
			final SelectionSession ss = selectionService.getCurrentSession(info);
			final TargetStructure structure = ss.getStructure();

			event.setFunction(structure.isNoTargets() ? null : selectItemFunction);
			event.stopProcessing();
		}
	}

	@Override
	public void supplyFunction(SectionInfo info, AttachmentSelectorEvent event)
	{
		if( isApplicable(info) )
		{
			final SelectionSession ss = selectionService.getCurrentSession(info);
			final TargetStructure structure = ss.getStructure();

			event.setHandler(structure.isNoTargets() ? null : this);
			event.stopProcessing();

			//visible encompasses applicable
			if( isVisible(info) )
			{
				event.setFunction(structure.isNoTargets() ? null : selectAttachmentFunction);
				event.stopProcessing();
			}
		}
	}

	@Override
	public void supplyFunction(SectionInfo info, AllAttachmentsSelectorEvent event)
	{
		if( isApplicable(info) )
		{
			final SelectionSession ss = selectionService.getCurrentSession(info);
			final TargetStructure structure = ss.getStructure();

			event.setFunction(structure.isNoTargets() ? null : selectAllAttachmentsFunction);
			event.stopProcessing();
		}
	}

	@Override
	public void handleAttachmentSelection(SectionInfo info, ItemId itemId, IAttachment attachment, String extensionType)
	{
		selectAttachment(info, attachment.getUuid(), itemId, extensionType);
	}

	private CourseFolderModel convertStructure(SectionInfo info, TargetFolder folder)
	{
		CourseFolderModel cfm = new CourseFolderModel();
		cfm.setId(folder.getId());
		final HtmlLinkState nameLink = new HtmlLinkState(new TextLabel(folder.getName()));

		nameLink.setClickHandler(new OverrideHandler(versionDialog.getOpenFunction(), folder.getId(), false));
		cfm.setName(nameLink);

		HtmlBooleanState selectionState = selection.getBooleanState(info, folder.getId());
		cfm.setSelect(selectionState);
		cfm.setSelected(selectionState.isChecked());
		cfm.setDefaultFolder(folder.isDefaultFolder());
		cfm.setTargetable(folder.isTargetable());
		if( !folder.isTargetable() )
		{
			nameLink.setDisabled(true);
		}
		cfm.setResourceCount(folder.getResourceCount());

		List<CourseFolderModel> children = Lists.newArrayList();
		for( TargetFolder subFolder : folder.getFolders() )
		{
			children.add(convertStructure(info, subFolder));
		}
		cfm.setFolders(children);
		return cfm;
	}

	@EventHandlerMethod
	public void onDrop(SectionInfo info, SelectedResourceKey resourceKey, String folderId)
	{
		final TargetFolder folder = selectionService.findTargetFolder(info, folderId);
		if( resourceKey.getType() == 'a' )
		{
			addAttachment(info, resourceKey.getItemKey(), resourceKey.getAttachmentUuid(), folder,
				resourceKey.getExtensionType());
		}
		else
		{
			selectionService.addSelectedResource(info,
				selectionService.createItemSelection(info,
					itemResolver.getItem(new ItemId(resourceKey.getUuid(), resourceKey.getVersion()),
						resourceKey.getExtensionType()),
					folder, resourceKey.getExtensionType()),
				false);
		}
	}

	@EventHandlerMethod
	public void selectAttachment(SectionInfo info, String attachmentUuid, ItemId itemId, String extensionType)
	{
		final String et = sanitiseExtensionType(extensionType);
		final String folderId = selectionService.getCurrentSession(info).getTargetFolder();
		addAttachment(info, itemId, attachmentUuid, selectionService.findTargetFolder(info, folderId), extensionType);
	}

	@EventHandlerMethod
	public void selectAllAttachments(SectionInfo info, List<String> uuids, ItemId itemId,
		@Nullable String extensionType)
	{
		final String et = sanitiseExtensionType(extensionType);
		for( String uuid : uuids )
		{
			selectAttachment(info, uuid, itemId, et);
		}
	}

	@EventHandlerMethod
	public void selectItem(SectionInfo info, ItemId itemId, String extensionType)
	{
		final String et = sanitiseExtensionType(extensionType);
		final String folderId = selectionService.getCurrentSession(info).getTargetFolder();
		final TargetFolder folder = selectionService.findTargetFolder(info, folderId);
		selectionService.addSelectedResource(info,
			selectionService.createItemSelection(info, itemResolver.getItem(itemId, et), folder, et), false);
	}

	@EventHandlerMethod
	public void selectPackage(SectionInfo info, ItemId itemId, String extensionType, String attachmentControlId)
	{
		final String et = sanitiseExtensionType(extensionType);
		final String folderId = selectionService.getCurrentSession(info).getTargetFolder();
		final TargetFolder folder = selectionService.findTargetFolder(info, folderId);
		final IItem<?> item = itemResolver.getItem(itemId, et);
		final ImsAttachment attachment = new UnmodifiableAttachments(item).getIms();
		String resourcePath = "viewcontent/" + attachmentControlId;

		if( attachment == null )
		{
			selectionService.addSelectedPath(info, item, resourcePath, folder, et);
		}
		else
		{
			if( !Check.isEmpty(item.getTreeNodes()) )
			{
				selectionService.addSelectedPath(info, item, resourcePath, folder, et);
			}
			else
			{
				selectionService.addSelectedPath(info, item, "treenav.jsp", folder, et);
			}
		}
	}

	@Nullable
	private String sanitiseExtensionType(@Nullable String extensionType)
	{
		return (extensionType == null || "null".equals(extensionType) ? null : extensionType);
	}

	private void addAttachment(SectionInfo info, ItemKey itemId, String attachmentUuid, TargetFolder folder,
		String extensionType)
	{
		final IItem<?> item = itemResolver.getItem(itemId, extensionType);
		final IAttachment attachment = new UnmodifiableAttachments(item).getAttachmentByUuid(attachmentUuid);
		if( attachment != null )
		{
			selectionService.addSelectedResource(info,
				selectionService.createAttachmentSelection(info, itemId, attachment, folder, extensionType), false);
		}
	}

	@EventHandlerMethod
	public void save(SectionInfo info)
	{
		selectionService.returnFromSession(info);
	}

	@EventHandlerMethod
	public void cancel(SectionInfo info)
	{
		selectionService.getCurrentSession(info).clearResources();
		selectionService.returnFromSession(info);
	}

	@Nullable
	@AjaxMethod
	public Object selectFolder(SectionInfo info)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		// There is only supposed to be one
		for( String checked : getSelectedFolders(info) )
		{
			session.setTargetFolder(checked);
		}
		return null;
	}

	public Set<String> getSelectedFolders(SectionInfo info)
	{
		return selection.getCheckedSet(info);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new CourseListModel();
	}

	public JSCallable getOnDropAjaxUpdateFunction()
	{
		return onDropAjaxUpdateFunction;
	}

	public Button getSaveButton()
	{
		return saveButton;
	}

	public Button getCancelButton()
	{
		return cancelButton;
	}

	public Link getViewSelections()
	{
		return viewSelections;
	}

	@NonNullByDefault(false)
	public static class CourseListModel
	{
		private CourseFolderModel root;
		private Boolean applicable;
		private Boolean visible;
		private boolean noTargets;

		public CourseFolderModel getRoot()
		{
			return root;
		}

		public void setRoot(CourseFolderModel root)
		{
			this.root = root;
		}

		public Boolean getApplicable()
		{
			return applicable;
		}

		public void setApplicable(Boolean applicable)
		{
			this.applicable = applicable;
		}

		public Boolean getVisible()
		{
			return visible;
		}

		public void setVisible(Boolean visible)
		{
			this.visible = visible;
		}

		public boolean isNoTargets()
		{
			return noTargets;
		}

		public void setNoTargets(boolean noTargets)
		{
			this.noTargets = noTargets;
		}
	}

	@NonNullByDefault(false)
	public static class CourseFolderModel
	{
		private HtmlBooleanState select;
		private HtmlLinkState name;
		private String id;
		private List<CourseFolderModel> folders;
		private int resourceCount;
		private boolean targetable;
		private boolean selected;
		private boolean defaultFolder;

		public HtmlBooleanState getSelect()
		{
			return select;
		}

		public void setSelect(HtmlBooleanState select)
		{
			this.select = select;
		}

		public boolean isSelected()
		{
			return selected;
		}

		public void setSelected(boolean selected)
		{
			this.selected = selected;
		}

		public HtmlLinkState getName()
		{
			return name;
		}

		public void setName(HtmlLinkState name)
		{
			this.name = name;
		}

		public String getId()
		{
			return id;
		}

		public void setId(String id)
		{
			this.id = id;
		}

		public List<CourseFolderModel> getFolders()
		{
			return folders;
		}

		public void setFolders(List<CourseFolderModel> folders)
		{
			this.folders = folders;
		}

		public int getResourceCount()
		{
			return resourceCount;
		}

		public void setResourceCount(int resourceCount)
		{
			this.resourceCount = resourceCount;
		}

		public boolean isTargetable()
		{
			return targetable;
		}

		public void setTargetable(boolean targetable)
		{
			this.targetable = targetable;
		}

		public boolean isDefaultFolder()
		{
			return defaultFolder;
		}

		public void setDefaultFolder(boolean defaultFolder)
		{
			this.defaultFolder = defaultFolder;
		}
	}
}
