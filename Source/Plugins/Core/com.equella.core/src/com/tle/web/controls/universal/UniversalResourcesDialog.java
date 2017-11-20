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

package com.tle.web.controls.universal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;

import org.java.plugin.registry.Extension;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.dialog.AbstractOkayableDialog;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryUICore;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PassThroughFunction;
import com.tle.web.sections.js.generic.function.RuntimeFunction;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableRow;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.wizard.WizardService;
import com.tle.web.wizard.WizardState;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.impl.WebRepository;
import com.tle.web.wizard.section.WizardSectionInfo;

@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class UniversalResourcesDialog
	extends
		AbstractOkayableDialog<UniversalResourcesDialog.UniversalResourcesDialogModel> implements UniversalControlState
{
	private static final String WIDTH = "810px";
	private static final CssInclude CSS = CssInclude
		.include(ResourcesService.getResourceHelper(UniversalResourcesDialog.class).url("css/universalresource.css"))
		.hasRtl().make();

	@PlugKey("handlers.abstract.addaction")
	private static Label ADD_LABEL;
	@PlugKey("handlers.abstract.replaceaction")
	private static Label REPLACE_LABEL;

	@PlugKey("pick.notypes")
	private static Label NO_RESOURCE_TYPES;
	@PlugKey("pick.title")
	private static Label PICK_RESOURCE_TYPE_TITLE;
	@PlugURL("scripts/universalresource.js")
	private static String SCRIPT_URL;

	@Inject
	private WizardService wizardService;
	@Inject
	private PluginTracker<AttachmentHandler> handlersTracker;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "sh")
	private TextField selectedHandler;

	@Component(name = "pr")
	private Table pickResourceTypeTable;

	@Component
	@PlugKey("dialog.save")
	private Button saveButton;
	@Component
	@PlugKey("dialog.back")
	private Button backButton;
	@Component
	@PlugKey("dialog.next")
	private Button resourceTypePicked;

	private Map<String, AttachmentHandler> handlers;
	private List<String> orderedHandlerIds;
	private WebRepository repository;
	private UniversalSettings settings;
	private CCustomControl storageControl;
	private JSCallable reloadFunction;

	private boolean singular;
	private Map<Object, Object> attributes;

	@Override
	protected ParameterizedEvent getAjaxShowEvent()
	{
		return events.getEventHandler("openDialog");
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		setAjax(true);
		setAlwaysShowFooter(true);

		handlers = new HashMap<String, AttachmentHandler>();
		orderedHandlerIds = new ArrayList<String>();

		Set<String> attachmentTypes = settings.getAttachmentTypes();
		final List<Extension> exts = handlersTracker.getExtensions();
		for( Extension ext : exts )
		{
			String extid = ext.getId();
			if( attachmentTypes.contains(extid) )
			{
				AttachmentHandler handler = handlersTracker.getNewBeanByExtension(ext);
				handlers.put(extid, handler);
				if( handler.show() )
				{
					orderedHandlerIds.add(extid);
				}
				handler.onRegister(tree, id, this);
			}
		}
		if( orderedHandlerIds.size() == 1 )
		{
			handlers.get(orderedHandlerIds.get(0)).setSingular(true);
			singular = true;
		}

		resourceTypePicked.setClickHandler(events.getNamedHandler("pickedHandler"));
		resourceTypePicked.setDisabled(true);

		saveButton.setClickHandler(events.getNamedHandler("saveClicked"));
		saveButton.setComponentAttribute(ButtonType.class, ButtonType.SAVE);

		backButton.setClickHandler(events.getNamedHandler("backClicked"));

		resourceTypePicked.addReadyStatements(new ExternallyDefinedFunction("setupPickResourcesType", new IncludeFile(
			SCRIPT_URL, JQueryUICore.PRERENDER)), Jq.$(pickResourceTypeTable), resourceTypePicked);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		setCancelHandler(new StatementHandler(Js.call_s(new RuntimeFunction()
		{
			@Override
			protected JSCallable createFunction(RenderContext info)
			{
				return new PassThroughFunction("cl", events.getSubmitValuesFunction("cancelled"));
			}
		})));
		super.treeFinished(id, tree);
	}

	@Override
	protected SectionRenderable getDialogContents(RenderContext context)
	{
		AttachmentHandler handler = getHandler(context);
		UniversalResourcesDialogModel model = getModel(context);

		if( handler == null )
		{
			TableState tableState = pickResourceTypeTable.getState(context);
			tableState.setFilterable(false);
			tableState.addClass("pick-resource-type");

			if( !Check.isEmpty(orderedHandlerIds) )
			{
				for( String handlerId : orderedHandlerIds )
				{
					AttachmentHandler attachmentHandler = handlers.get(handlerId);
					if( attachmentHandler.show() )
					{
						TableRow tableRow = tableState.addRow(attachmentHandler.getLabel());
						tableRow.setClickHandler(new OverrideHandler(selectedHandler.createSetFunction(), handlerId));
					}
				}
				model.setBody(viewFactory.createResult("pickresourcetype.ftl", this));
				model.addAction(resourceTypePicked);

			}
			else
			{
				model.setBody(new LabelRenderer(NO_RESOURCE_TYPES));
			}
		}
		else
		{
			DialogRenderOptions renderOptions = new DialogRenderOptions();
			final SectionRenderable body = handler.render(context, renderOptions);

			if( renderOptions.isFullscreen() )
			{
				return body;
			}

			model.setBody(body);

			if( renderOptions.isShowSave() )
			{
				if( renderOptions.isShowAddReplace() )
				{
					saveButton.setLabel(context, isReplacing(context) ? REPLACE_LABEL : ADD_LABEL);
				}

				JSHandler saveClickHandler = renderOptions.getSaveClickHandler();
				if( saveClickHandler != null )
				{
					saveButton.setClickHandler(context, saveClickHandler);
				}

				renderOptions.addAction(saveButton);
			}

			model.addActions(renderOptions.getActions());

			if( !singular && !isEditing(context) )
			{
				model.getActions().add(0, backButton);
			}
		}

		// Let the super method do all the work so that actions get rendered
		// correctly, etc...
		return super.getDialogContents(context);
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return CombinedRenderer.combineMultipleResults(CSS, getModel(context).getBody());
	}

	@Override
	protected Collection<Button> collectFooterActions(RenderContext context)
	{
		return getModel(context).getActions();
	}

	@Override
	protected Label getTitleLabel(RenderContext context)
	{
		UniversalResourcesDialogModel model = getModel(context);
		final AttachmentHandler handler = getHandler(context);
		if( handler == null )
		{
			return PICK_RESOURCE_TYPE_TITLE;
		}
		return handler.getTitleLabel(context, !Check.isEmpty(model.getEditUuid()));
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		final AttachmentHandler handler = getHandler(context);
		return "universalresourcedialog" + (handler == null ? "" : " " + handler.getHandlerId());
	}

	@EventHandlerMethod
	public void setHandler(SectionInfo info, String handlerId)
	{
		selectedHandler.setValue(info, handlerId);
	}

	@Nullable
	private AttachmentHandler getHandler(SectionInfo info)
	{
		String handlerId = selectedHandler.getValue(info);
		if( !Check.isEmpty(handlerId) )
		{
			return handlers.get(handlerId);
		}
		// User hasn't chosen anything - no handler
		return null;
	}

	@Nullable
	public AttachmentHandler findHandlerForAttachment(IAttachment attachment)
	{
		for( Entry<String, AttachmentHandler> entry : handlers.entrySet() )
		{
			AttachmentHandler handler = entry.getValue();
			if( handler.supports(attachment) )
			{
				return handler;
			}
		}
		return null;
	}

	@EventHandlerMethod
	public void openDialog(SectionInfo info, String replaceUuid, String editUuid)
	{
		UniversalResourcesDialogModel model = getModel(info);
		model.setReplaceUuid(replaceUuid);
		model.setEditUuid(editUuid);
		attributes = Maps.newHashMap();
		if( !Check.isEmpty(editUuid) )
		{
			final Attachment attachment = (Attachment) repository.getAttachments().getAttachmentByUuid(editUuid);
			for( Entry<String, AttachmentHandler> entry : handlers.entrySet() )
			{
				AttachmentHandler handler = entry.getValue();
				if( handler.supports(attachment) )
				{
					selectedHandler.setValue(info, entry.getKey());
					handler.loadForEdit(info, attachment);
					break;
				}
			}
		}
		else if( orderedHandlerIds.size() == 1 )
		{
			selectedHandler.setValue(info, orderedHandlerIds.get(0));
			getHandler(info).createNew(info);
		}
		super.showDialog(info);
	}

	@EventHandlerMethod
	public void saveClicked(SectionInfo info)
	{
		AttachmentHandler handler = getHandler(info);
		if( handler.validate(info) )
		{
			String replacedUuid = null;
			if( isReplacing(info) )
			{
				replacedUuid = getReplaceAttachmentUuid(info);
				Attachment replacedAttachment = getAttachmentByUuid(info, replacedUuid);
				AttachmentHandler replacedHandler = findHandlerForAttachment(replacedAttachment);
				replacedHandler.remove(info, replacedAttachment, true);
			}
			if( isEditing(info) )
			{
				Attachment attachmentToEdit = getAttachmentToEdit(info);
				handler.saveEdited(info, attachmentToEdit);
			}
			else
			{
				handler.saveChanges(info, replacedUuid);
			}
			closeAndReload(info);
		}
	}

	@Nullable
	@Override
	public Attachment getReplacedAttachment(SectionInfo info)
	{
		String replaceUuid = getReplaceAttachmentUuid(info);
		if( replaceUuid != null )
		{
			return getAttachmentByUuid(info, replaceUuid);
		}
		return null;
	}

	@Nullable
	private String getReplaceAttachmentUuid(SectionInfo info)
	{
		return getModel(info).getReplaceUuid();
	}

	@EventHandlerMethod
	public void backClicked(SectionInfo info)
	{
		final AttachmentHandler handler = getHandler(info);
		if( handler != null )
		{
			handler.cancelled(info);
		}
		selectedHandler.setValue(info, null);
	}

	@EventHandlerMethod
	public void cancelled(SectionInfo info)
	{
		final AttachmentHandler handler = getHandler(info);
		if( handler != null )
		{
			handler.cancelled(info);
		}
		closeDialog(info);

	}

	@EventHandlerMethod
	public void pickedHandler(SectionInfo info)
	{
		getHandler(info).createNew(info);
	}

	public WizardState getWizardState(SectionInfo info)
	{
		return info.getAttributeForClass(WizardSectionInfo.class).getWizardState();
	}

	@Override
	public boolean isEditing(SectionInfo info)
	{
		return !Check.isEmpty(getModel(info).getEditUuid());
	}

	@Override
	public boolean isReplacing(SectionInfo info)
	{
		return !Check.isEmpty(getReplaceAttachmentUuid(info));
	}

	@Override
	public String getWidth()
	{
		return WIDTH;
	}

	public void setRepository(WebRepository repository)
	{
		this.repository = repository;
	}

	@Override
	public WebRepository getRepository()
	{
		return repository;
	}

	public void setDefinition(UniversalSettings def)
	{
		settings = def;
	}

	@Override
	public UniversalResourcesDialogModel instantiateDialogModel(SectionInfo info)
	{
		return new UniversalResourcesDialogModel();
	}

	public static class UniversalResourcesDialogModel extends DialogModel
	{
		@Bookmarked(name = "r")
		private String replaceUuid;
		@Bookmarked(name = "e")
		private String editUuid;
		@Nullable
		private List<Button> actions;
		private SectionRenderable body;

		public String getReplaceUuid()
		{
			return replaceUuid;
		}

		public void setReplaceUuid(String replaceUuid)
		{
			this.replaceUuid = replaceUuid;
		}

		public String getEditUuid()
		{
			return editUuid;
		}

		public void setEditUuid(String editUuid)
		{
			this.editUuid = editUuid;
		}

		public List<Button> getActions()
		{
			if( actions == null )
			{
				actions = Lists.newArrayList();
			}
			return actions;
		}

		public void addAction(Button action)
		{
			getActions().add(action);
		}

		public void addActions(Collection<Button> actions)
		{
			getActions().addAll(actions);
		}

		public SectionRenderable getBody()
		{
			return body;
		}

		public void setBody(SectionRenderable body)
		{
			this.body = body;
		}
	}

	public void setStorageControl(CCustomControl storageControl)
	{
		this.storageControl = storageControl;

	}

	public CCustomControl getStorageControl()
	{
		return storageControl;
	}

	public void setReloadFunction(JSCallable reloadFunction)
	{
		this.reloadFunction = addParentCallable(reloadFunction);
	}

	public void closeAndReload(SectionInfo info)
	{
		closeDialog(info, reloadFunction);
	}

	public Table getPickResourceTypeTable()
	{
		return pickResourceTypeTable;
	}

	public TextField getSelectedHandler()
	{
		return selectedHandler;
	}

	@Override
	public CustomControl getControlConfiguration()
	{
		return settings.getWrapped();
	}

	@Override
	public void setAttribute(SectionInfo info, Object key, Object value)
	{
		attributes.put(key, value);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(SectionInfo info, Object key)
	{
		return (T) attributes.get(key);
	}

	private Attachment getAttachmentByUuid(SectionInfo info, String uuid)
	{
		return (Attachment) repository.getAttachments().getAttachmentByUuid(uuid);
	}

	private Attachment getAttachmentToEdit(SectionInfo info)
	{
		return getAttachmentByUuid(info, getModel(info).getEditUuid());
	}

	@Override
	public void addAttachment(SectionInfo info, Attachment attachment)
	{
		repository.getAttachments().addAttachment(attachment);
	}

	@Override
	public void addMetadataUuid(SectionInfo info, String uuid)
	{
		storageControl.getValues().add(uuid);
	}

	@Override
	public ViewableItem getViewableItem(SectionInfo info)
	{
		return wizardService.createViewableItem(getWizardState(info));
	}

	@Override
	public void removeAttachment(SectionInfo info, Attachment attachment)
	{
		repository.getAttachments().removeAttachment(attachment);
		repository.removeNavigationNodes(Collections.singleton(attachment));
	}

	@Override
	public void removeAttachments(SectionInfo info, Collection<Attachment> attachments)
	{
		repository.getAttachments().removeAll(attachments);
		repository.removeNavigationNodes(attachments);
	}

	@Override
	public void removeMetadataUuid(SectionInfo info, String uuid)
	{
		storageControl.getValues().remove(uuid);
	}

	public void deleteAttachment(SectionInfo info, String attachmentUuid)
	{
		Attachment attachment = getAttachmentByUuid(info, attachmentUuid);
		AttachmentHandler handler = findHandlerForAttachment(attachment);
		handler.remove(info, attachment, false);
	}

	@Override
	public void save(SectionInfo info)
	{
		saveClicked(info);
	}

	@Override
	public void cancel(SectionInfo info)
	{
		cancelled(info);
	}

	@Override
	public Collection<Attachment> getAttachments()
	{
		final Map<String, IAttachment> mua = repository.getAttachments().convertToMapUuid();

		Collection<Attachment> fas = new ArrayList<Attachment>();
		for( String uuid : storageControl.getValues() )
		{
			Attachment a = (Attachment) mua.get(uuid);
			if( a != null )
			{
				fas.add(a);
			}
		}
		return fas;
	}

	@Override
	public UniversalResourcesDialog getDialog()
	{
		return this;
	}
}
