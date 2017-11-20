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

package com.tle.web.controls.googlebook;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.gdata.data.books.VolumeEntry;
import com.google.gdata.data.books.VolumeFeed;
import com.google.gdata.data.dublincore.Creator;
import com.google.gdata.data.dublincore.Date;
import com.google.gdata.data.dublincore.Description;
import com.google.gdata.data.dublincore.Format;
import com.google.gdata.data.dublincore.Publisher;
import com.google.gdata.data.extensions.Rating;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Utils;
import com.tle.core.google.GoogleService;
import com.tle.core.guice.Bind;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.AttachmentHandlerUtils;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.ListLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.Pager;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupLinkRenderer;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class GoogleBookHandler extends BasicAbstractAttachmentHandler<AbstractAttachmentHandlerModel>
{
	private static final int PER_PAGE = 10;

	@PlugKey("gbook.name")
	private static Label NAME_LABEL;
	@PlugKey("gbook.description")
	private static Label DESCRIPTION_LABEL;
	@PlugKey("gbook.add.title")
	private static Label ADD_TITLE_LABEL;
	@PlugKey("gbook.edit.title")
	private static Label EDIT_TITLE_LABEL;

	@PlugKey("gbook.details.description")
	private static Label DESCRIPTION;
	@PlugKey("gbook.details.author")
	private static Label AUTHOR;
	@PlugKey("gbook.details.authors")
	private static Label AUTHORS;
	@PlugKey("gbook.details.publisher")
	private static Label PUBLISHER;
	@PlugKey("gbook.details.publishers")
	private static Label PUBLISHERS;
	@PlugKey("gbook.details.rating")
	private static Label RATING;

	@PlugKey("gbook.details.viewlink")
	private static Label VIEW_LINK_LABEL;

	@Inject
	private GoogleService google;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@Component
	private TextField query;
	@Component
	@PlugKey("gbook.add.search")
	private Button search;
	@Component
	private MultiSelectionList<Void> results;
	@Component
	private Pager pager;

	@Override
	public String getHandlerId()
	{
		return "googleBookHandler";
	}

	@Override
	public void createNew(SectionInfo info)
	{
		super.createNew(info);
		query.setValue(info, null);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		search.setClickHandler(new ReloadHandler());
		results.setListModel(new DynamicHtmlListModel<Void>()
		{
			@Override
			protected Iterable<Option<Void>> populateOptions(SectionInfo info)
			{

				String q = query.getValue(info);
				if( Check.isEmpty(q) || getModel(info).isButtonUpdate() )
				{
					return Collections.emptyList();
				}

				VolumeFeed searchBooks = google.searchBooks(q, (pager.getCurrentPage(info) - 1) * PER_PAGE, PER_PAGE);
				pager.setup(info, (searchBooks.getTotalResults() - 1) / PER_PAGE + 1, 8);

				final List<Option<Void>> rv = new ArrayList<Option<Void>>();
				for( VolumeEntry entry : searchBooks.getEntries() )
				{
					StringBuilder description = new StringBuilder();
					for( Description desc : entry.getDescriptions() )
					{
						description.append(desc.getValue());
					}

					List<String> authorNames = Lists.transform(entry.getCreators(), new Function<Creator, String>()
					{
						@NonNullByDefault(false)
						@Override
						public String apply(Creator creator)
						{
							return creator.getValue();
						}
					});
					String authors = Utils.join(authorNames.toArray(), ", ");

					String title = entry.getTitle().getPlainText();
					String href = entry.getInfoLink().getHref();

					LinkRenderer titleLink = new PopupLinkRenderer(new HtmlLinkState(new SimpleBookmark(href)));
					titleLink.setLabel(new TextLabel(title));

					LinkRenderer thumbnail = null;
					if( entry.getThumbnailLink() != null )
					{
						thumbnail = new PopupLinkRenderer(new HtmlLinkState(new SimpleBookmark(href)));
						thumbnail.setNestedRenderable(new ImageRenderer(entry.getThumbnailLink().getHref(),
							new TextLabel(title)));
					}

					GoogleBookResult result = new GoogleBookResult(entry.getId());
					result.setAuthor(authors);
					result.setThumbnail(thumbnail);
					result.setDescription(description.toString());
					result.setLink(titleLink);

					rv.add(result);
				}
				return rv;
			}

			@Override
			protected Iterable<Void> populateModel(SectionInfo info)
			{
				return null;
			}
		});

		pager.setEventHandler(JSHandler.EVENT_CHANGE, new ReloadHandler());
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		StatementHandler updateHandler = new StatementHandler(dialogState.getDialog().getFooterUpdate(tree,
			events.getEventHandler("updateButtons")));

		results.setEventHandler(JSHandler.EVENT_CHANGE, updateHandler);
	}

	@Override
	public AttachmentHandlerLabel getLabel()
	{
		return new AttachmentHandlerLabel(NAME_LABEL, DESCRIPTION_LABEL);
	}

	@Override
	public boolean supports(IAttachment attachment)
	{
		if( attachment instanceof CustomAttachment )
		{
			CustomAttachment ca = (CustomAttachment) attachment;
			return GoogleBookConstants.ATTACHMENT_TYPE.equals(ca.getType());
		}
		return false;
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? EDIT_TITLE_LABEL : ADD_TITLE_LABEL;
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		renderOptions.setShowSave(!Check.isEmpty(results.getSelectedValuesAsStrings(context)));
		return viewFactory.createResult("add-googlebook.ftl", this);
	}

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		List<Attachment> attachments = Lists.newArrayList();
		Set<String> bookIds = results.getSelectedValuesAsStrings(info);
		for( String bookId : bookIds )
		{
			VolumeEntry bookEntry = google.getBook(bookId);

			CustomAttachment a = new CustomAttachment();
			a.setType(GoogleBookConstants.ATTACHMENT_TYPE);
			a.setData(GoogleBookConstants.PROPERTY_ID, bookId);
			a.setData(GoogleBookConstants.PROPERTY_THUMB_URL, bookEntry.getThumbnailLink().getHref());
			List<Date> bookEntryDates = bookEntry.getDates();
			if( !Check.isEmpty(bookEntryDates) )
			{
				a.setData(GoogleBookConstants.PROPERTY_PUBLISHED, bookEntryDates.get(0).getValue());
			}

			// Hax
			List<Format> formats = bookEntry.getFormats();
			if( !Check.isEmpty(formats) )
			{
				for( Format format : formats )
				{
					String infoStr = format.getValue();
					if( infoStr.contains("pages") )
					{
						a.setData(GoogleBookConstants.PROPERTY_FORMATS, infoStr.split(" ")[0]);
						break;
					}
				}
			}
			a.setData(GoogleBookConstants.PROPERTY_URL, bookEntry.getInfoLink().getHref());
			a.setDescription(bookEntry.getTitle().getPlainText());
			attachments.add(a);
		}
		return attachments;
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		// Common details
		AbstractAttachmentHandlerModel model = getModel(context);
		final Attachment a = getDetailsAttachment(context);
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			a);
		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		// Dynamic details
		String bookId = (String) a.getData(GoogleBookConstants.PROPERTY_ID);
		if( !Check.isEmpty(bookId) )
		{
			VolumeEntry bookEntry = google.getBook(bookId);

			// Description TODO - Perhaps a more link?
			List<Description> descriptions = bookEntry.getDescriptions();
			if( !Check.isEmpty(descriptions) )
			{
				model.addSpecificDetail(GoogleBookConstants.PROPERTY_DESCRIPTION, new Pair<Label, Object>(DESCRIPTION,
					new WrappedLabel(new TextLabel(descriptions.get(0).getValue()), 1250, true, false)));
			}

			// Authors
			List<String> authors = Lists.transform(bookEntry.getCreators(), new Function<Creator, String>()
			{
				@Override
				public String apply(Creator input)
				{
					return input.getValue();
				}

			});
			if( !Check.isEmpty(authors) )
			{
				addAttachmentDetail(context, authors.size() > 1 ? AUTHORS : AUTHOR, new ListLabel(authors, "<br>"));
			}

			// Publisher
			List<String> pubs = Lists.transform(bookEntry.getPublishers(), new Function<Publisher, String>()
			{
				@Override
				public String apply(Publisher input)
				{
					return input.getValue();
				}

			});
			if( !Check.isEmpty(authors) )
			{
				addAttachmentDetail(context, pubs.size() > 1 ? PUBLISHERS : PUBLISHER, new ListLabel(pubs, "<br>"));
			}

			// Rating
			Rating bookEntryRating = bookEntry.getRating();

			// Depending on a 0 int value mapping to "zero" rating IN
			// RATING_CLASSES
			int rating = bookEntryRating != null ? AttachmentHandlerUtils.getRating(bookEntryRating.getAverage()) : 0;
			addAttachmentDetail(context, RATING, new DivRenderer("rating-stars "
				+ AttachmentHandlerUtils.RATING_CLASSES.get(rating), ""));

			// Add a view link
			String href = bookEntry.getInfoLink().getHref();
			HtmlLinkState linkState = new HtmlLinkState(VIEW_LINK_LABEL, new SimpleBookmark(href));
			linkState.setTarget(HtmlLinkState.TARGET_BLANK);
			model.setViewlink(new LinkRenderer(linkState));
		}
		return viewFactory.createResult("edit-googlebook.ftl", this);
	}

	public TextField getQuery()
	{
		return query;
	}

	public Button getSearchButton()
	{
		return search;
	}

	public MultiSelectionList<Void> getResults()
	{
		return results;
	}

	public Pager getPager()
	{
		return pager;
	}

	@Override
	public Class<AbstractAttachmentHandlerModel> getModelClass()
	{
		return AbstractAttachmentHandlerModel.class;
	}

	@NonNullByDefault(false)
	public static class GoogleBookResult extends VoidKeyOption
	{
		private SectionRenderable thumbnail;
		private SectionRenderable link;
		private String description;
		private String author;

		public GoogleBookResult(String bookId)
		{
			super(null, bookId);
		}

		public void setThumbnail(SectionRenderable thumbnail)
		{
			this.thumbnail = thumbnail;
		}

		public void setAuthor(String author)
		{
			this.author = author;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public void setLink(SectionRenderable link)
		{
			this.link = link;
		}

		public SectionRenderable getThumbnail()
		{
			return thumbnail;
		}

		public String getAuthor()
		{
			return author;
		}

		public String getDescription()
		{
			return description;
		}

		public SectionRenderable getLink()
		{
			return link;
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
		return GoogleBookConstants.MIME_TYPE;
	}
}