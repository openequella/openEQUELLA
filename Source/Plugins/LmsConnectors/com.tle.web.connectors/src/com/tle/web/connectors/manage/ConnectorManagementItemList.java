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

package com.tle.web.connectors.manage;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import com.tle.beans.item.Item;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.connectors.ConnectorContent;
import com.tle.common.connectors.ConnectorContent.ConnectorContentAttribute;
import com.tle.common.connectors.ConnectorTerminology;
import com.tle.common.connectors.entity.Connector;
import com.tle.common.connectors.service.ConnectorItemKey;
import com.tle.core.connectors.service.ConnectorRepositoryService;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.web.connectors.manage.ConnectorManagementItemList.ConnectorManagementListEntry;
import com.tle.web.itemlist.StandardListSection;
import com.tle.web.itemlist.item.AbstractItemListEntry;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.SimpleElementId;
import com.tle.web.sections.js.validators.Confirm;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.BundleLabel;
import com.tle.web.sections.result.util.HighlightableBundleLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;
import com.tle.web.viewurl.ViewItemService;
import com.tle.web.viewurl.ViewItemUrlFactory;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@Bind
@SuppressWarnings("nls")
public class ConnectorManagementItemList
	extends
		StandardListSection<ConnectorManagementListEntry, StandardListSection.Model<ConnectorManagementListEntry>>
{
	public static final String DIV_PFX = "cm_";

	@PlugKey("manage.label.item")
	private static Label LABEL_ITEM;
	@PlugKey("manage.label.attachment")
	private static Label LABEL_ATTACHMENT;
	@PlugKey("finduses.dateadded")
	private static Label LABEL_DATE_ADDED;
	@PlugKey("finduses.datemodified")
	private static Label LABEL_DATE_MODIFIED;

	@PlugKey("manage.button.edit")
	private static Label LABEL_BUTTON_EDIT;
	@PlugKey("manage.button.remove")
	private static Label LABEL_BUTTON_DELETE;
	@PlugKey("manage.button.select")
	private static Label LABEL_BUTTON_SELECT;
	@PlugKey("manage.button.unselect")
	private static Label LABEL_BUTTON_UNSELECT;
	@PlugKey("manage.remove.confirm")
	private static Label LABEL_DELETE_CONFIRM;

	private JSCallable selectCall;
	private JSCallable removeCall;

	@TreeLookup
	private ConnectorBulkSelectionSection selectionSection;
	@TreeLookup
	protected ConnectorManagementResultsSection resultsSection;
	@TreeLookup
	protected ConnectorManagementQuerySection querySection;

	@Inject
	private ConnectorService connectorService;
	@Inject
	private ConnectorRepositoryService repositoryService;

	@EventFactory
	private EventGenerator events;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		selectCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("selectItem"));
		removeCall = selectionSection.getUpdateSelection(tree, events.getEventHandler("removeItem"));
	}

	@Override
	protected void customiseListEntries(RenderContext context, List<ConnectorManagementListEntry> entries)
	{
		super.customiseListEntries(context, entries);
		for( ConnectorManagementListEntry entry : entries )
		{
			// only add the select button if EXPORT is enabled for this
			// connector, otherwise
			// it's pointless
			Connector connector = querySection.getConnector(context);
			if( connectorService.canExport(connector) && repositoryService.supportsExport(connector.getLmsType()) )
			{
				final ConnectorContent content = entry.getContent();
				final String uuid = content.getUuid();
				if( uuid != null )
				{
					final ConnectorItemKey itemId = new ConnectorItemKey(content,
						querySection.getConnectorList().getSelectedValue(context).getId());
					entry.getTag().setElementId(new SimpleElementId(DIV_PFX + itemId.toKeyString()));

					if( !selectionSection.isSelected(context, itemId) )
					{
						final HtmlLinkState link = new HtmlLinkState(LABEL_BUTTON_SELECT,
							new OverrideHandler(selectCall, itemId));
						entry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.SELECT));
					}
					else
					{
						final HtmlLinkState link = new HtmlLinkState(LABEL_BUTTON_UNSELECT,
							new OverrideHandler(removeCall, itemId));
						entry.addRatingAction(new ButtonRenderer(link).showAs(ButtonType.UNSELECT));
						entry.setSelected(true);
					}
				}
			}
		}
	}

	@EventHandlerMethod
	public void selectItem(SectionInfo info, ConnectorItemKey itemId)
	{
		selectionSection.addSelection(info, itemId);
		addAjaxDiv(info, itemId);
	}

	@EventHandlerMethod
	public void removeItem(SectionInfo info, ConnectorItemKey itemId)
	{
		selectionSection.removeSelection(info, itemId);
		addAjaxDiv(info, itemId);
	}

	private void addAjaxDiv(SectionInfo info, ConnectorItemKey itemId)
	{
		AjaxRenderContext renderContext = info.getAttributeForClass(AjaxRenderContext.class);
		if( renderContext != null )
		{
			renderContext.addAjaxDivs(DIV_PFX + itemId.toKeyString());
		}
	}

	@Bind
	public static class ConnectorManagementListEntry extends AbstractItemListEntry
	{
		@TreeLookup
		protected ConnectorManagementItemList itemList;

		@Inject
		private ConnectorService connectorService;
		@Inject
		private ConnectorRepositoryService repositoryService;
		@Inject
		private DateRendererFactory dateRendererFactory;
		@Inject
		private ViewItemUrlFactory itemUrls;
		@Inject
		private AttachmentResourceService attachmentResourceService;
		@Inject
		private ViewItemService viewItemService;

		private ConnectorContent content;
		private Connector connector;
		private JSCallable deleteFunction;
		private JSCallable editFunction;

		@Override
		protected void setupMetadata(RenderContext context)
		{
			// Target resource
			final Item item = getItem();
			if( item != null )
			{
				final HtmlLinkState equellaResourceLink = new HtmlLinkState(
					new BundleLabel(item.getName(), item.getUuid(), bundleCache),
					itemUrls.createItemUrl(context, item.getItemId()));
				addDelimitedMetadata(LABEL_ITEM, equellaResourceLink);

				// Target attachment (if any)
				final String equellaAttachmentUuid = content.getAttachmentUuid();
				if( !Check.isEmpty(equellaAttachmentUuid) )
				{
					final IAttachment attachment = new UnmodifiableAttachments(item)
						.getAttachmentByUuid(equellaAttachmentUuid);
					if( attachment != null )
					{
						final ViewableResource viewableResource = attachmentResourceService.getViewableResource(context,
							getViewableItem(), attachment);
						if( !viewableResource.getBooleanAttribute(ViewableResource.KEY_HIDDEN) )
						{
							String viewerId = Check.isEmpty(attachment.getViewer())
								? viewItemService.getDefaultViewerId(viewableResource) : attachment.getViewer();
							final LinkTagRenderer link = viewItemService.getViewableLink(context, viewableResource,
								viewerId);
							link.setLabel(new TextLabel(attachment.getDescription()));
							addDelimitedMetadata(LABEL_ATTACHMENT, link);
						}
					}
				}
			}

			final ConnectorTerminology terminology = repositoryService.getConnectorTerminology(connector.getLmsType());

			// Course (or linking item in EQUELLA connector)
			final String course = content.getCourse();
			if( !Check.isEmpty(course) )
			{
				final Object courseData;
				if( !Check.isEmpty(content.getCourseUrl()) )
				{
					final HtmlLinkState courseLink = new HtmlLinkState(new TextLabel(course));
					courseLink.setBookmark(new SimpleBookmark(content.getCourseUrl()));
					courseLink.setTarget("_blank");
					courseData = courseLink;
				}
				else
				{
					courseData = course;
				}
				addDelimitedMetadata(new KeyLabel(terminology.getCourseHeading()), courseData);
			}

			// Folder (or linking attachment for EQUELLA connector)
			final String folder = content.getFolder();
			if( !Check.isEmpty(folder) )
			{
				final Object folderData;
				if( !Check.isEmpty(content.getFolderUrl()) )
				{
					final HtmlLinkState folderLink = new HtmlLinkState(new TextLabel(content.getFolder()));
					folderLink.setBookmark(new SimpleBookmark(content.getFolderUrl()));
					folderLink.setTarget("_blank");
					folderData = folderLink;
				}
				else
				{
					folderData = folder;
				}
				addDelimitedMetadata(new KeyLabel(terminology.getLocationHeading()), folderData);
			}

			final Date dateAdded = content.getDateAdded();
			if( dateAdded != null )
			{
				addDelimitedMetadata(LABEL_DATE_ADDED, dateRendererFactory.createDateRenderer(dateAdded));
			}
			final Date dateModified = content.getDateModified();
			if( dateModified != null )
			{
				addDelimitedMetadata(LABEL_DATE_MODIFIED, dateRendererFactory.createDateRenderer(dateModified));
			}

			final List<ConnectorContentAttribute> attributes = content.getAttributeList();
			for( final ConnectorContentAttribute attribute : attributes )
			{
				if( attribute.isHide() )
				{
					break;
				}

				// TODO: SectionUtils.convertToRenderer should be able to do
				// this

				Object value = attribute.getValue();
				if( value != null )
				{
					if( value instanceof Date )
					{
						addDelimitedMetadata(new KeyLabel(attribute.getLabelKey()),
							dateRendererFactory.createDateRenderer((Date) value));
					}
					else
					{
						if( !(value instanceof String && Check.isEmpty((String) value)) )
						{
							addDelimitedMetadata(new KeyLabel(attribute.getLabelKey()), value);
						}
					}
				}
			}

			if( connectorService.canExport(connector) )
			{
				if( content.getUuid() != null )
				{
					if( repositoryService.supportsEdit(connector.getLmsType()) )
					{
						HtmlLinkState state = new HtmlLinkState(LABEL_BUTTON_EDIT,
							new OverrideHandler(editFunction, new ConnectorItemKey(content, connector.getId()),
								content.getExternalTitle(), getDescriptionText()));
						addRatingAction(new ButtonRenderer(state).showAs(ButtonType.EDIT));
					}
					if( repositoryService.supportsDelete(connector.getLmsType()) )
					{
						HtmlLinkState state = new HtmlLinkState(LABEL_BUTTON_DELETE,
							new OverrideHandler(deleteFunction, new ConnectorItemKey(content, connector.getId()))
								.addValidator(new Confirm(LABEL_DELETE_CONFIRM)));
						addRatingAction(new ButtonRenderer(state).showAs(ButtonType.DELETE));
					}
				}
			}

			super.setupMetadata(context);
		}

		@Override
		public Label getTitleLabel()
		{
			return new HighlightableBundleLabel(null, content.getExternalTitle(), bundleCache,
				listSettings.getHilightedWords(), false);
		}

		private String getDescriptionText()
		{
			final String description = content.getExternalDescription();
			if( description == null )
			{
				return "";
			}
			return description;
		}

		@Override
		public Label getDescription()
		{
			return new HighlightableBundleLabel(null, getDescriptionText(), bundleCache,
				listSettings.getHilightedWords(), true);
		}

		public ConnectorContent getContent()
		{
			return content;
		}

		public void setContent(ConnectorContent content)
		{
			this.content = content;
		}

		public Connector getConnector()
		{
			return connector;
		}

		public void setConnector(Connector connector)
		{
			this.connector = connector;
		}

		public void setDeleteFunction(JSCallable deleteFunction)
		{
			this.deleteFunction = deleteFunction;
		}

		public JSCallable getEditFunction()
		{
			return editFunction;
		}

		public void setEditFunction(JSCallable editFunction)
		{
			this.editFunction = editFunction;
		}
	}
}
