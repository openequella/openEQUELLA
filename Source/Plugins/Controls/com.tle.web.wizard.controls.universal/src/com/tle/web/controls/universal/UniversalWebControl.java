package com.tle.web.controls.universal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.google.common.collect.Lists;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.i18n.LangUtils;
import com.tle.common.wizard.controls.universal.UniversalSettings;
import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.SelectionsTable;
import com.tle.web.sections.equella.component.model.DynamicSelectionsTableModel;
import com.tle.web.sections.equella.component.model.SelectionsTableSelection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.viewurl.ViewItemUrl;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentNode;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.viewurl.attachments.AttachmentTreeService;
import com.tle.web.wizard.controls.AbstractWebControl;
import com.tle.web.wizard.controls.CCustomControl;
import com.tle.web.wizard.controls.WebControlModel;
import com.tle.web.wizard.impl.WebRepository;
import com.tle.web.wizard.render.WizardFreemarkerFactory;

@Bind
@SuppressWarnings("nls")
public class UniversalWebControl extends AbstractWebControl<WebControlModel>
{
	@PlugKey("list.empty")
	private static Label LABEL_EMPTY_LIST;
	@PlugKey("list.edit")
	private static Label EDIT_LINK;
	@PlugKey("list.replace")
	private static Label REPLACE_LINK;
	@PlugKey("list.delete")
	private static Label DELETE_LINK;
	@PlugKey("list.delete.confirm")
	private static Label DELETE_CONFIRM;
	@PlugKey("list.preview")
	private static Label PREVIEW;
	@PlugKey("list.hidden.from.summary")
	private static String KEY_HIDDEN_FROM_SUMMARY_NOTE;

	@Inject
	private AttachmentResourceService attachmentResourceService;

	private CCustomControl storageControl;
	private UniversalSettings definition;
	private WebRepository repository;

	@ViewFactory(name = "wizardFreemarkerFactory")
	private WizardFreemarkerFactory viewFactory;
	@EventFactory
	private EventGenerator events;
	@AjaxFactory
	private AjaxGenerator ajax;

	@Component
	@PlugKey("list.add")
	private Link addLink;
	@Inject
	@Component
	private UniversalResourcesDialog dialog;
	@Component(name = "a")
	private SelectionsTable attachmentsTable;
	@Inject
	private AttachmentTreeService attachmentTreeService;

	private JSCallable deleteFunc;

	@Override
	public void setWrappedControl(final HTMLControl control)
	{
		storageControl = (CCustomControl) control;
		definition = new UniversalSettings((CustomControl) control.getControlBean());
		repository = (WebRepository) control.getRepository();
		dialog.setRepository(repository);
		dialog.setDefinition(definition);
		dialog.setStorageControl(storageControl);
		super.setWrappedControl(control);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		deleteFunc = ajax.getAjaxUpdateDomFunction(tree, null, events.getEventHandler("delete"),
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), id);
		addLink.setClickHandler(new OverrideHandler(dialog.getOpenFunction(), "", ""));
		addLink.setDisablable(true);
		dialog.setOkHandler(new ReloadHandler());
		dialog.setReloadFunction(getReloadFunction(true, events.getEventHandler("runMetadataMappers")));

		attachmentsTable.setNothingSelectedText(LABEL_EMPTY_LIST);
		attachmentsTable.setAddAction(addLink);
		attachmentsTable.setSelectionsModel(new AttachmentsModel());
		attachmentsTable.setFilterable(false);
	}

	@EventHandlerMethod
	public void runMetadataMappers(SectionInfo info)
	{
		repository.getState().getWizardMetadataMapper().setMapNow(true);
	}

	@Override
	public void validate()
	{
		int uploadedAttachments = getAttachments().size();
		if( definition.isMaxFilesEnabled() && uploadedAttachments > definition.getMaxFiles() )
		{
			setInvalid(true, LangUtils.createTempLangugageBundle("wizard.controls.file.toomanyattachments",
				definition.getMaxFiles(), uploadedAttachments - definition.getMaxFiles()));
		}

	}

	@Override
	public SectionResult renderHtml(final RenderEventContext context) throws Exception
	{
		int uploadedAttachments = getAttachments().size();
		if( !definition.isMultipleSelection()
			&& !attachmentsTable.getSelectionsModel().getSelections(context).isEmpty() )
		{
			addLink.setDisplayed(context, false);
		}
		else if( definition.isMaxFilesEnabled() && uploadedAttachments > 0
			&& uploadedAttachments >= definition.getMaxFiles() )
		{
			addLink.setDisplayed(context, false);
		}
		else
		{
			addDisabler(context, addLink);
		}

		return viewFactory.createResult("universalattachmentlist.ftl", context);
	}

	private List<Attachment> getAttachments()
	{
		final Map<String, IAttachment> mua = repository.getAttachments().convertToMapUuid();

		List<Attachment> fas = new ArrayList<Attachment>();
		for( Iterator<String> iter = storageControl.getValues().iterator(); iter.hasNext(); )
		{
			Attachment a = (Attachment) mua.get(iter.next());
			// HACK Sanity check - itemXml being puffed out with duplicates (ie
			// duplicate references not merely duplicated values), when page
			// refreshed. #7625 - Mangled but technically valid controls mapping
			// to metadata is a precondition to produce the aberrant behavior
			// which this HACK overcomes.
			boolean duplicatedReferenceExists = false;
			if( a != null )
			{
				for( Attachment attachmentThusFar : fas )
				{
					if( attachmentThusFar == a ) // duplicated reference
					{
						duplicatedReferenceExists = true;
						break; // from inner for( Attachment ...)
					}
				}
				if( !duplicatedReferenceExists )
				{
					fas.add(a);
				}
			}
			if( a == null || duplicatedReferenceExists )
			{
				// Removing UUIDs for non-existent attachments simplifies local
				// processing.
				iter.remove();
			}
		}
		return fas;
	}

	@Override
	public void deletedFromParent(SectionInfo info)
	{
		/*
		 * List<String> values = new
		 * ArrayList<String>(storageControl.getValues()); final
		 * ModifiableAttachments as = repository.getAttachments(); for( String
		 * attachUuid : values ) { dialog.removeAttachment(info,
		 * as.getAttachmentByUuid(attachUuid)); }
		 */
	}

	@EventHandlerMethod
	public void delete(final SectionInfo info, final String attachmentUuid)
	{
		dialog.deleteAttachment(info, attachmentUuid);
	}

	@Override
	public boolean isEmpty()
	{
		return storageControl.getValues().isEmpty();
	}

	@Override
	public void doEdits(final SectionInfo info)
	{
		// Nothing yet
	}

	public UniversalResourcesDialog getDialog()
	{
		return dialog;
	}

	@Override
	public Class<WebControlModel> getModelClass()
	{
		return WebControlModel.class;
	}

	public SelectionsTable getAttachmentsTable()
	{
		return attachmentsTable;
	}

	private class AttachmentsModel extends DynamicSelectionsTableModel<AttachmentNode>
	{

		@Override
		public List<SelectionsTableSelection> getSelections(SectionInfo info)
		{
			List<Attachment> dattachmnts = getAttachments();
			final List<AttachmentNode> source = attachmentTreeService.getTreeStructure(dattachmnts, false);
			final List<SelectionsTableSelection> selections = Lists.newArrayList();
			addAttachments(info, source, selections, 0);
			return selections;
		}

		private void addAttachments(SectionInfo info, List<AttachmentNode> nodes,
			List<SelectionsTableSelection> selections, int indent)
		{
			for( AttachmentNode attachmentNode : nodes )
			{
				IAttachment attachment = attachmentNode.getAttachment();
				List<SectionRenderable> actions = Lists.newArrayList();
				SelectionsTableSelection selection = new SelectionsTableSelection();
				final WebRepository webRepos = (WebRepository) getRepository();
				final ViewableResource viewableResource = attachmentResourceService.getViewableResource(info,
					webRepos.getViewableItem(), attachment);

				final AttachmentHandler attachmentHandler = dialog.findHandlerForAttachment(attachment);
				final boolean hiddenFromSummary = attachmentHandler != null
					&& attachmentHandler.isHiddenFromSummary(attachment);

				final ViewItemUrl vurl = viewableResource.createDefaultViewerUrl();
				final HtmlLinkState view = new HtmlLinkState(vurl);
				final LinkRenderer viewLink = new LinkRenderer(view);
				viewLink.setTarget("_blank");

				if( hiddenFromSummary )
				{
					//selection.setViewAction(null);
					viewLink.setDisabled(true);
				}

				if( attachment.isPreview() )
				{
					selection.setViewAction(CombinedRenderer.combineResults(viewLink,
						new SpanRenderer(PREVIEW).addClass("preview-tag")));
				}
				else
				{
					selection.setViewAction(viewLink);
				}

				Label descriptionLabel = new TextLabel(attachment.getDescription());
				final String uuid = attachment.getUuid();

				if( storageControl.isEnabled() )
				{
					// This is bollocks in order to not refactor the whole
					// universal control
					if( indent == 0 )
					{
						actions.add(makeAction(EDIT_LINK, new OverrideHandler(dialog.getOpenFunction(), "", uuid)));
						actions.add(makeAction(REPLACE_LINK, new OverrideHandler(dialog.getOpenFunction(), uuid, "")));
					}
					else
					{
						viewLink.addClass("indent");
					}

					actions.add(makeAction(DELETE_LINK,
						new OverrideHandler(deleteFunc, uuid).addValidator(new Confirm(DELETE_CONFIRM))));
				}
				selection.setActions(actions);
				if( hiddenFromSummary )
				{
					descriptionLabel = new KeyLabel(KEY_HIDDEN_FROM_SUMMARY_NOTE, descriptionLabel);
				}
				viewLink.setLabel(descriptionLabel);
				selections.add(selection);
				addAttachments(info, attachmentNode.getChildren(), selections, indent + 1);

			}
		}

		@Override
		protected void transform(SectionInfo info, SelectionsTableSelection selection, AttachmentNode thing,
			List<SectionRenderable> actions, int index)
		{
			throw new UnsupportedOperationException();
		}

		@Override
		protected List<AttachmentNode> getSourceList(SectionInfo info)
		{
			throw new UnsupportedOperationException();
		}
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return addLink;
	}
}
