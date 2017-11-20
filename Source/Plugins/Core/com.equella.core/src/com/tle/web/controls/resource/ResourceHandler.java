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

package com.tle.web.controls.resource;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.wizard.controls.resource.ResourceSettings;
import com.tle.common.wizard.controls.resource.ResourceSettings.AllowedSelection;
import com.tle.core.guice.Bind;
import com.tle.core.i18n.BundleCache;
import com.tle.core.item.service.ItemService;
import com.tle.core.mimetypes.MimeTypeConstants;
import com.tle.core.services.item.relation.RelationModify;
import com.tle.core.services.item.relation.RelationOperationState;
import com.tle.core.services.item.relation.RelationService;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.generic.function.PassThroughFunction;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.selection.ParentFrameSelectionCallback;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectedResourceDetails;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.SelectionHomeSelectable;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;
import com.tle.web.wizard.WizardState;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class ResourceHandler
	extends
		AbstractDetailsAttachmentHandler<ResourceHandler.ResourceHandlerModel, ResourceUniversalAttachment>
{
	public static final String TYPE_RESOURCE = "resource";
	public static final String DATA_TYPE = "type";
	public static final String DATA_UUID = "uuid";
	public static final String DATA_VERSION = "version";

	@PlugKey("label.handler.name")
	private static Label LABEL_NAME;
	@PlugKey("label.handler.description")
	private static Label LABEL_DESCRIPTION;
	@PlugKey("label.title.edit")
	private static Label LABEL_TITLE_EDIT;

	@PlugKey("ressel.details.item.desc")
	private static Label ITEM_DESC;

	@PlugKey("ressel.details.item.viewlink")
	private static Label VIEW_LINK_LABEL;

	@Inject
	private SelectionService selectionService;
	@Inject
	private RelationService relationService;
	@Inject
	private SelectionHomeSelectable homeSelectable;
	@Inject
	private ItemService itemService;
	@Inject
	private BundleCache bundleCache;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	private JSCallAndReference resultsCallback;

	private ResourceSettings resourceSettings;
	private String selectionsKey;

	@Override
	public String getHandlerId()
	{
		return "resourceHandler";
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(LABEL_NAME, LABEL_DESCRIPTION);
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		// Common details
		ResourceHandlerModel model = getModel(context);
		final CustomAttachment attachment = (CustomAttachment) getDetailsAttachment(context);
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		final ViewableResource viewable = attachmentResourceService.getViewableResource(context,
			itemInfo.getViewableItem(), attachment);
		List<AttachmentDetail> commonAttachmentDetails = viewable.getCommonAttachmentDetails();
		addAttachmentDetails(context, commonAttachmentDetails);

		// Thumbnail (set thumb)
		ImageRenderer thumbRenderer = viewable.createStandardThumbnailRenderer(new TextLabel(attachment
			.getDescription()));
		model.setThumbnail(thumbRenderer.addClass("file-thumbnail"));

		// Additional details
		String type = (String) attachment.getData(DATA_TYPE);
		if( type.equals(Character.toString(SelectedResource.TYPE_PATH)) && attachment.getUrl().isEmpty() )
		{
			int version = (Integer) attachment.getData(DATA_VERSION);
			String uuid = (String) attachment.getData(DATA_UUID);
			if( version == 0 )
			{
				version = itemService.getLiveItemVersion(uuid);
			}
			ItemId itemId = new ItemId(uuid, version);
			Map<String, Object> allInfo = itemService.getItemInfo(itemId);

			if( !Check.isEmpty(allInfo) )
			{
				BundleLabel desc = new BundleLabel(allInfo.get("description_id"), "", bundleCache);
				model.addSpecificDetail("itemdesc", new Pair<Label, Object>(ITEM_DESC, desc)); // Description
			}
		}

		HtmlLinkState linkState = new HtmlLinkState(VIEW_LINK_LABEL, viewable.createCanonicalUrl());
		linkState.setTarget(HtmlLinkState.TARGET_BLANK);
		model.setViewlink(new LinkRenderer(linkState));
		return viewFactory.createResult("resource/resource-edit.ftl", this);
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		SelectionSession session = new SelectionSession(new ParentFrameSelectionCallback(resultsCallback, false));

		final AllowedSelection as = resourceSettings.getAllowedSelection();
		session.setHomeSelectable("home");
		session.setSelectAttachments(as.isAttachments());
		session.setSelectItem(as.isItems());
		// session.setSelectPackage(as.isPackages());
		session.setSelectMultiple(isMultipleAllowed(context));
		session.setSelectDraft(true);

		session.setAllCollections(!resourceSettings.isRestricted(ResourceSettings.KEY_RESTRICT_COLLECTIONS));

		if( !session.isAllCollections() )
		{
			Set<String> collections = resourceSettings.getRestrictedTo(ResourceSettings.KEY_RESTRICT_COLLECTIONS);
			session.setCollectionUuids(collections);
		}

		session.setAllPowerSearches(!resourceSettings.isRestricted(ResourceSettings.KEY_RESTRICT_POWERSEARCHES));
		if( !session.isAllPowerSearches() )
		{
			Set<String> powerSearches = resourceSettings.getRestrictedTo(ResourceSettings.KEY_RESTRICT_POWERSEARCHES);
			session.setPowerSearchIds(powerSearches);
		}

		session.setAllContributionCollections(!resourceSettings
			.isRestricted(ResourceSettings.KEY_RESTRICT_CONTRIBUTION));
		if( !session.isAllContributionCollections() )
		{
			Set<String> contributeCol = resourceSettings.getRestrictedTo(ResourceSettings.KEY_RESTRICT_CONTRIBUTION);
			session.setContributionCollectionIds(contributeCol);
		}

		session.setAllDynamicCollections(!resourceSettings.isRestricted(ResourceSettings.KEY_RESTRICT_DYNACOLLECTION));
		if( !session.isAllDynamicCollections() )
		{
			Set<String> contributeCol = resourceSettings.getRestrictedTo(ResourceSettings.KEY_RESTRICT_DYNACOLLECTION);
			session.setDynamicCollectionIds(contributeCol);
		}

		SectionInfo forward = homeSelectable.createSectionInfo(context, session);
		selectionService.setupSelectionSession(forward, session);
		getModel(context).setIntegrationUrl(forward.getPublicBookmark().getHref());
		renderOptions.setFullscreen(true);
		return viewFactory.createResult("resource/resource-add.ftl", this);
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return LABEL_TITLE_EDIT;
	}

	@Override
	public void onRegister(SectionTree tree, String parentId, UniversalControlState state)
	{
		super.onRegister(tree, parentId, state);
		resourceSettings = new ResourceSettings(state.getControlConfiguration());
		selectionsKey = getSectionId() + ":" + getHandlerId();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		resultsCallback = new PassThroughFunction("r" + id, events.getSubmitValuesFunction("results"));
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			CustomAttachment custom = (CustomAttachment) attachment;
			return custom.getType().equals(TYPE_RESOURCE);
		}
		return false;
	}

	@Override
	public void remove(SectionInfo info, Attachment attachment, boolean willBeReplaced)
	{
		super.remove(info, attachment, willBeReplaced);
		RelationModify relOp = getRelationModifier();
		relOp.getState().deleteByResourceId(attachment.getUuid());
	}

	private void addRelation(SelectedResourceDetails resource, String resourceId)
	{
		String relationType = resourceSettings.getRelationType();
		if( !Check.isEmpty(relationType) )
		{
			RelationModify relOp = getRelationModifier();
			final RelationOperationState relationState = relOp.getState();
			relationState.deleteByType(relationType);
			relationState.add(new ItemId(resource.getUuid(), resource.getVersion()), relationType, resourceId);
		}
	}

	@EventHandlerMethod
	public void results(SectionInfo info, List<SelectedResourceDetails> selectedResources)
	{
		dialogState.setAttribute(info, selectionsKey, selectedResources);
		if( selectedResources.size() == 0 )
		{
			dialogState.cancel(info);
		}
		else
		{
			dialogState.save(info);
		}
	}

	@Override
	protected void addOrReplace(SectionInfo info, ResourceUniversalAttachment attachment, String replacementUuid)
	{
		super.addOrReplace(info, attachment, replacementUuid);
		SelectedResourceDetails selection = attachment.getSelection();
		if( selection != null )
		{
			addRelation(selection, attachment.getAttachment().getUuid());
		}
	}

	@Override
	protected ResourceUniversalAttachment createUniversalAttachmentForEdit(SectionInfo info, Attachment attachment)
	{
		return new ResourceUniversalAttachment(null, (CustomAttachment) attachment);
	}

	@Override
	protected List<ResourceUniversalAttachment> createUniversalAttachments(SectionInfo info)
	{
		List<ResourceUniversalAttachment> attachments = Lists.newArrayList();
		List<SelectedResourceDetails> selectedResources = dialogState.getAttribute(info, selectionsKey);
		for( SelectedResourceDetails resource : selectedResources )
		{
			attachments.add(new ResourceUniversalAttachment(resource, makeAttachment(resource)));
		}
		return attachments;
	}

	private CustomAttachment makeAttachment(SelectedResourceDetails resource)
	{
		final char type = resource.getType();
		final int version = Check.isEmpty(resourceSettings.getRelationType()) && resource.isLatest() ? 0 : resource
			.getVersion();
		final String attachmentUuid = resource.getAttachmentUuid();

		final CustomAttachment attachment = new CustomAttachment();
		if( type == SelectedResource.TYPE_ATTACHMENT )
		{
			attachment.setUrl(attachmentUuid);
		}
		else
		{
			attachment.setUrl(resource.getUrl());
		}
		attachment.setType(TYPE_RESOURCE);
		attachment.setDescription(resource.getTitle());
		attachment.setData(DATA_UUID, resource.getUuid());
		attachment.setData(DATA_VERSION, version);
		attachment.setData(DATA_TYPE, Character.toString(type));
		return attachment;
	}

	private RelationModify getRelationModifier()
	{
		final WizardState state = dialogState.getRepository().getState();
		RelationModify relOp = (RelationModify) state.getWizardSaveOperation(RelationModify.NAME);
		if( relOp == null )
		{
			final RelationOperationState relState = new RelationOperationState();
			if( !state.isNewItem() )
			{
				relState.initForCurrent(relationService.getAllByFromItem(state.getItem()));
			}
			relOp = new RelationModify(relState);
			state.setWizardSaveOperation(RelationModify.NAME, relOp);
		}
		return relOp;
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new ResourceHandlerModel();
	}

	public JSCallAndReference getResultsCallback()
	{
		return resultsCallback;
	}

	public static class ResourceHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		private String integrationUrl;

		public String getIntegrationUrl()
		{
			return integrationUrl;
		}

		public void setIntegrationUrl(String integrationUrl)
		{
			this.integrationUrl = integrationUrl;
		}
	}

	@Override
	protected boolean validateAddPage(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return MimeTypeConstants.MIME_ITEM;
	}

	@Override
	public boolean canRestrictAttachments()
	{
		return false;
	}
}
