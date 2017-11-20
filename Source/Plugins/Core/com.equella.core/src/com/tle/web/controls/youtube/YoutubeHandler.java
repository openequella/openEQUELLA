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

package com.tle.web.controls.youtube;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.google.api.client.googleapis.json.GoogleJsonError.ErrorInfo;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;
import com.google.api.services.youtube.model.VideoStatistics;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.Pair;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.wizard.controls.youtube.YoutubeSettings;
import com.tle.core.google.GoogleService;
import com.tle.core.guice.Bind;
import com.tle.web.controls.universal.AbstractDetailsAttachmentHandler;
import com.tle.web.controls.universal.AttachmentHandlerLabel;
import com.tle.web.controls.universal.BasicAbstractAttachmentHandler;
import com.tle.web.controls.universal.DialogRenderOptions;
import com.tle.web.controls.universal.UniversalControlState;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.equella.utils.VoidKeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.result.util.NumberLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.HeadingRenderer;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupLinkRenderer;
import com.tle.web.viewurl.ItemSectionInfo;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.attachments.AttachmentResourceService;

@SuppressWarnings("nls")
@Bind
@NonNullByDefault
public class YoutubeHandler extends BasicAbstractAttachmentHandler<YoutubeHandler.YoutubeHandlerModel>
{
	private static final int PER_PAGE = 50;
	public static final String KEY_ALL_YOUTUBE = "AllYouTube";

	@PlugKey("youtube.name")
	private static Label NAME_LABEL;
	@PlugKey("youtube.description")
	private static Label DESCRIPTION_LABEL;
	@PlugKey("youtube.add.title")
	private static Label ADD_TITLE_LABEL;
	@PlugKey("youtube.edit.title")
	private static Label EDIT_TITLE_LABEL;
	@PlugKey("add.views")
	private static String ADD_VIEWS_LABEL;
	@PlugKey("add.author")
	private static String ADD_AUTHOR_LABEL;
	@PlugKey("add.noapikey")
	private static String NO_API_KEY;
	@PlugKey("add.noapikey.desc")
	private static String NO_API_KEY_DESC;
	@PlugKey("error.apikey.invalid")
	private static String API_KEY_INVALID;
	@PlugKey("error.channelid.invalid")
	private static String CHANNEL_INVALID;

	@PlugKey("youtube.details.customparams.error")
	private static Label CUSTOM_PARAMS_ERROR;
	@PlugKey("youtube.details.views")
	private static Label VIEWS_LABEL;
	@PlugKey("youtube.details.rating")
	private static Label RATING;
	@PlugKey("youtube.details.rating.info")
	private static String RATING_INFO;

	@PlugKey("youtube.details.viewlink")
	private static Label VIEW_LINK_LABEL;

	@Inject
	private GoogleService google;
	@Inject
	private DateRendererFactory dateRendererFactory;
	@Inject
	private AttachmentResourceService attachmentResourceService;

	@Component
	private TextField query;
	@Component
	@PlugKey("add.search")
	private Button search;
	@Component
	private MultiSelectionList<Void> results;
	@Component(name = "ytcp")
	private TextField customParamsArea;

	@Component
	private SingleSelectionList<Pair<String, String>> channelList;

	private YoutubeSettings youtubeSettings;

	@Override
	public String getHandlerId()
	{
		return "youTubeHandler";
	}

	@Override
	public void onRegister(SectionTree tree, String parentId, UniversalControlState state)
	{
		youtubeSettings = new YoutubeSettings(state.getControlConfiguration());
		super.onRegister(tree, parentId, state);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		if( youtubeSettings.isAllowChannelSelection() )// YouTube Channel is
														// enabled
		{
			List<Pair<String, String>> channels = new ArrayList<Pair<String, String>>();

			if( youtubeSettings.isOptionAllowChannelSelection() )
			{
				channels.add(0, new Pair<String, String>(KEY_ALL_YOUTUBE, "All Youtube"));
			}

			channels.addAll(youtubeSettings.getChannels());
			channelList.setListModel(new SimpleHtmlListModel<Pair<String, String>>(channels)
			{
				@Override
				protected Option<Pair<String, String>> convertToOption(Pair<String, String> obj)
				{
					return new NameValueOption<Pair<String, String>>(new NameValue(obj.getSecond(), obj.getSecond()),
						obj);
				}
			});
		}
		else
		{
			channelList.setListModel(null);
		}

		channelList.addChangeEventHandler(new ReloadHandler());

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

				List<SearchResult> searchVideos = null;

				try
				{
					if( youtubeSettings.isAllowChannelSelection() )
					{
						String channel = channelList.getSelectedValue(info).getFirst();
						// restrict to defined channels
						if( youtubeSettings.isOptionRestrictChannelSelection() )
						{
							// search within a defined channel
							searchVideos = google.searchVideoIdsWithinChannel(q, "relevance", channel, PER_PAGE);
						}
						else
						{
							if( channel.equals(KEY_ALL_YOUTUBE) )
							{
								// search within all youtube
								searchVideos = google.searchVideos(q, "relevance", PER_PAGE);
							}
							else
							{
								// search within a defined channel
								searchVideos = google.searchVideoIdsWithinChannel(q, "relevance", channel, PER_PAGE);
							}
						}
					}
					else
					{
						// search within all youtube
						searchVideos = google.searchVideos(q, "relevance", PER_PAGE);
					}

				}
				catch( GoogleJsonResponseException gjre )
				{
					List<ErrorInfo> errors = gjre.getDetails().getErrors();
					if( !Check.isEmpty(errors) )
					{
						for( ErrorInfo ei : errors )
						{
							switch( ei.getReason() )
							{
								case "keyInvalid":
									getModel(info).setError(CurrentLocale.get(API_KEY_INVALID));
									break;
								case "invalidChannelId":
									getModel(info).setError(CurrentLocale.get(CHANNEL_INVALID));
									break;
							}
						}
					}
				}

				// provide a no result message if the return is empty
				if( searchVideos == null || searchVideos.size() == 0 )
				{
					getModel(info).setNoResult(true);
					return null;
				}

				final List<Option<Void>> rv = new ArrayList<Option<Void>>();

				// Get detailed info
				List<Video> videosWithDetail = google.getVideos(searchVideos.stream().map(m -> m.getId().getVideoId())
					.collect(Collectors.toList()));

				for( Video video : videosWithDetail )
				{
					String videoId = video.getId();
					VideoSnippet vidInfo = video.getSnippet();

					final String title = vidInfo.getTitle();
					String href = "//www.youtube.com/v/" + videoId;
					final LinkRenderer titleLink = new PopupLinkRenderer(new HtmlLinkState(new SimpleBookmark(href)));
					titleLink.setLabel(new TextLabel(title));

					ImageRenderer thumbnail = null;
					thumbnail = new ImageRenderer(vidInfo.getThumbnails().getDefault().getUrl(), new TextLabel(title));

					YoutubeResultOption result = new YoutubeResultOption(videoId);
					result.setAuthor(new KeyLabel(ADD_AUTHOR_LABEL, vidInfo.getChannelTitle()));
					result.setDate(dateRendererFactory
						.createDateRenderer(new Date(vidInfo.getPublishedAt().getValue())));

					String description = vidInfo.getDescription();
					result.setDescription(Check.isEmpty(description) ? null : new WrappedLabel(new TextLabel(
						description), 250));

					result.setViews(new KeyLabel(ADD_VIEWS_LABEL, video.getStatistics() != null ? video.getStatistics()
						.getViewCount() : 0));
					result.setLink(titleLink);
					result.setThumbnail(thumbnail);

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
			return YoutubeUtils.ATTACHMENT_TYPE.equals(ca.getType());
		}
		return false;
	}

	@Override
	public Label getTitleLabel(RenderContext context, boolean editing)
	{
		return editing ? EDIT_TITLE_LABEL : ADD_TITLE_LABEL;
	}

	@Override
	protected void saveDetailsToAttachment(SectionInfo info, Attachment attachment)
	{
		super.saveDetailsToAttachment(info, attachment);
		if( customParamsArea.getValue(info) != null )
		{
			CustomAttachment youtubeAttachment = (CustomAttachment) attachment;
			youtubeAttachment.setData(YoutubeUtils.PROPERTY_PARAMETERS, customParamsArea.getValue(info));
		}
	}

	@Override
	protected SectionRenderable renderAdd(RenderContext context, DialogRenderOptions renderOptions)
	{
		if( google.isEnabled() )
		{
			results.getState(context).setDisallowMultiple(!isMultipleAllowed(context));
			results.getListModel().getOptions(context);
			renderOptions.setShowSave(!Check.isEmpty(results.getSelectedValuesAsStrings(context)));

			return viewFactory.createResult("add-youtube.ftl", this);
		}

		HeadingRenderer heading = new HeadingRenderer(3, new KeyLabel(NO_API_KEY));
		LabelRenderer error = new LabelRenderer(new KeyLabel(NO_API_KEY_DESC));
		renderOptions.setShowAddReplace(false);
		renderOptions.setShowSave(false);

		return new CombinedRenderer(heading, error);
	}

	@Override
	protected List<Attachment> createAttachments(SectionInfo info)
	{
		List<Attachment> attachments = Lists.newArrayList();
		List<String> videoIds = Lists.newArrayList(results.getSelectedValuesAsStrings(info));
		List<Video> videos = google.getVideos(videoIds);

		for( Video v : videos )
		{
			CustomAttachment a = new CustomAttachment();

			Channel channel = google.getChannel(v.getSnippet().getChannelId());
			a.setType(YoutubeUtils.ATTACHMENT_TYPE);

			Thumbnail defaultThumb = v.getSnippet().getThumbnails().getDefault();
			a.setData(YoutubeUtils.PROPERTY_THUMB_URL, defaultThumb.getUrl());
			a.setThumbnail(defaultThumb.getUrl());

			a.setData(YoutubeUtils.PROPERTY_PLAY_URL, "//www.youtube.com/v/" + v.getId());

			a.setData(YoutubeUtils.PROPERTY_ID, v.getId());
			a.setData(YoutubeUtils.PROPERTY_DURATION, v.getContentDetails().getDuration());
			a.setData(YoutubeUtils.PROPERTY_AUTHOR, channel.getSnippet().getTitle());
			DateTime uploaded = v.getSnippet().getPublishedAt();
			if( uploaded != null )
			{
				a.setData(YoutubeUtils.PROPERTY_DATE, uploaded.getValue());
			}
			String title = v.getSnippet().getTitle();
			a.setData(YoutubeUtils.PROPERTY_TITLE, title);
			a.setDescription(title);

			attachments.add(a);
		}
		return attachments;
	}

	@Override
	protected SectionRenderable renderDetails(RenderContext context, DialogRenderOptions renderOptions)
	{
		YoutubeHandlerModel model = getModel(context);
		// Get common details from viewable resource
		final Attachment a = getDetailsAttachment(context);
		ItemSectionInfo itemInfo = context.getAttributeForClass(ItemSectionInfo.class);
		ViewableResource resource = attachmentResourceService.getViewableResource(context, itemInfo.getViewableItem(),
			a);

		addAttachmentDetails(context, resource.getCommonAttachmentDetails());

		String videoId = (String) a.getData(YoutubeUtils.PROPERTY_ID);
		if( !Check.isEmpty(videoId) )
		{

			// Embedded video (could substitute description?)
			String ua = context.getRequest().getHeader("User-Agent");
			// http://dev.equella.com/issues/6144
			if( ua != null && (ua.contains("MSIE 7.0") || ua.contains("MSIE 8.0") || ua.contains("MSIE 9.0")) )
			{
				String embed = "<embed id=\"ytpreview\" class=\"preview\" src=\"//www.youtube.com/v/" + videoId
					+ "?version=3\" type=\"application/x-shockwave-flash\" allowscriptaccess=\"always\" "
					+ "allowfullscreen=\"true\"></embed>";
				model.addSpecificDetail("embed", new Pair<Label, Object>(null, embed));
			}
			else
			{
				String embed = "<iframe id=\"ytpreview\" class=\"preview\" src=\"//www.youtube.com/embed/" + videoId
					+ "\" frameborder=\"0\" allowfullscreen></iframe>";
				model.addSpecificDetail("embed", new Pair<Label, Object>(null, embed));
			}

			if( google.isEnabled() )
			{
				Video video = google.getVideo(videoId);
				model.addSpecificDetail("description", new Pair<Label, Object>(new TextLabel(""), video.getSnippet()
					.getDescription()));

				VideoStatistics stats = video.getStatistics();
				addAttachmentDetail(context, VIEWS_LABEL, new NumberLabel(stats != null ? stats.getViewCount() : 0));

				// Rating - Likes/Dislikes
				if( stats != null )
				{
					BigInteger numLikes = stats.getLikeCount();
					BigInteger numDislikes = stats.getDislikeCount();
					SpanRenderer spinfo = new SpanRenderer("rating-info", new KeyLabel(RATING_INFO, numLikes,
						numDislikes));
					addAttachmentDetail(context, RATING, new CombinedRenderer(new YouTubeRating(numLikes.intValue(),
						numDislikes.intValue()), spinfo));
				}
			}
		}

		String customParams = (String) a.getData(YoutubeUtils.PROPERTY_PARAMETERS);
		if( !Check.isEmpty(customParams) )
		{
			customParamsArea.setValue(context, customParams);
		}
		// Add a view link
		HtmlLinkState linkState = new HtmlLinkState(VIEW_LINK_LABEL, new SimpleBookmark("http://youtu.be/" + videoId));
		linkState.setTarget(HtmlLinkState.TARGET_BLANK);
		model.setViewlink(new LinkRenderer(linkState));
		return viewFactory.createResult("edit-youtube.ftl", this);
	}

	@Override
	protected boolean validateDetailsPage(SectionInfo info)
	{
		boolean valid = super.validateDetailsPage(info);
		if( !Check.isEmpty(customParamsArea.getValue(info)) )
		{
			valid &= validateCustomParams(info, customParamsArea.getValue(info));
		}
		return valid;
	}

	private boolean validateCustomParams(SectionInfo info, String customParamString)
	{
		// FIXME could be done with a regex that matches
		// [string + whitespace][=][string + whitespace][nothing||,]
		for( String param : customParamString.split(",") )
		{
			if( !param.contains("=") )
			{
				getModel(info).addError("customParams", CUSTOM_PARAMS_ERROR);
				return false;
			}
		}
		return true;
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

	@Override
	public Class<YoutubeHandlerModel> getModelClass()
	{
		return YoutubeHandlerModel.class;
	}

	public static class YoutubeHandlerModel extends AbstractDetailsAttachmentHandler.AbstractAttachmentHandlerModel
	{
		private boolean noResult;
		private String error;

		public boolean isNoResult()
		{
			return noResult;
		}

		public void setNoResult(boolean noResult)
		{
			this.noResult = noResult;
		}

		public String getError()
		{
			return error;
		}

		public void setError(String error)
		{
			this.error = error;
		}
	}

	public static class YoutubeResultOption extends VoidKeyOption
	{
		private SectionRenderable thumbnail;
		private SectionRenderable link;
		private SectionRenderable date;
		private Label description;
		private Label author;
		private Label views;

		public YoutubeResultOption(String videoId)
		{
			super(null, videoId);
		}

		public void setThumbnail(SectionRenderable thumbnail)
		{
			this.thumbnail = thumbnail;
		}

		public void setAuthor(Label author)
		{
			this.author = author;
		}

		public void setViews(Label views)
		{
			this.views = views;
		}

		public void setDescription(Label description)
		{
			this.description = description;
		}

		public void setLink(SectionRenderable link)
		{
			this.link = link;
		}

		public void setDate(SectionRenderable date)
		{
			this.date = date;
		}

		public SectionRenderable getThumbnail()
		{
			return thumbnail;
		}

		public Label getAuthor()
		{
			return author;
		}

		public Label getViews()
		{
			return views;
		}

		public Label getDescription()
		{
			return description;
		}

		public SectionRenderable getLink()
		{
			return link;
		}

		public SectionRenderable getDate()
		{
			return date;
		}
	}

	public static class YouTubeRating extends DivRenderer
	{
		private final DivRenderer likes;
		private final DivRenderer dislikes;

		public YouTubeRating(int numLikes, int numDislikes)
		{
			super(new TagState());
			tagState.addClass("rating-bar");
			likes = new DivRenderer("rating-bar-likes", "");
			dislikes = new DivRenderer("rating-bar-dislikes", "");

			int[] percentages = getPercentages(numLikes, numDislikes);
			likes.setStyles(MessageFormat.format("width: {0}%;", percentages[0]), null, null);
			dislikes.setStyles(MessageFormat.format("width: {0}%;", percentages[1]), null, null);

			setNestedRenderable(new CombinedRenderer(likes, dislikes));
		}

		private int[] getPercentages(int numLikes, int numDislikes)
		{
			double total = (double) numLikes + (double) numDislikes;
			double div = numLikes / total;
			int like = (int) Math.round(div * 100);
			return new int[]{like, (100 - like)};
		}
	}

	public SingleSelectionList<Pair<String, String>> getChannelList()
	{
		return channelList;
	}

	@Override
	protected boolean validateAddPage(SectionInfo info)
	{
		return true;
	}

	@Override
	public String getMimeType(SectionInfo info)
	{
		return YoutubeUtils.MIME_TYPE;
	}

	public TextField getCustomParamsArea()
	{
		return customParamsArea;
	}
}