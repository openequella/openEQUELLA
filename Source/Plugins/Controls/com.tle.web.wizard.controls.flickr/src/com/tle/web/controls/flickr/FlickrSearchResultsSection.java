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

package com.tle.web.controls.flickr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.tle.common.Check;
import com.tle.common.NameValueExtra;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.searching.SearchResults;
import com.tle.common.searching.SimpleSearchResults;
import com.tle.common.util.UtcDate;
import com.tle.common.wizard.controls.universal.handlers.FlickrSettings;
import com.tle.core.flickr.FlickrSearchParameters;
import com.tle.core.flickr.FlickrService;
import com.tle.web.controls.flickr.filter.FilterByCreativeCommonsLicencesSection;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.search.base.AbstractSearchResultsSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.ResultListCollector;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.TextUtils;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.MultiSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.ImageRenderer;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.sections.standard.renderers.popup.PopupLinkRenderer;

/**
 * @author larry
 */
@SuppressWarnings("nls")
public class FlickrSearchResultsSection
	extends
		AbstractSearchResultsSection<FlickrListEntry, FlickrSearchEvent, FlickrSearchResultsEvent, AbstractSearchResultsSection.SearchResultsModel>
{
	private static final Logger LOGGER = Logger.getLogger(FlickrSearchResultsSection.class);

	private static final String FLICKR_HOME = "www.flickr.com";

	@PlugKey("search.wraphelp")
	private static String SUGGEST_WRAP;
	@PlugKey("search.starthelp")
	private static Label LABEL_SUGGESTSTART;
	@PlugKey("search.error")
	private static String SEARCH_ERROR;

	@Inject
	private FlickrService flickrService;
	@Inject
	private FlickrListSection itemList;
	@Inject
	private DateRendererFactory dateRendererFactory;

	@ViewFactory
	private FreemarkerFactory searchViewFactory;

	@Component
	private MultiSelectionList<Photo> results;

	@TreeLookup
	private FlickrLayoutSection flickrLayoutSection;
	@TreeLookup
	private FlickrQuerySection flickrQuerySection;
	@TreeLookup
	private FilterByCreativeCommonsLicencesSection filterByCreativeCommonsLicencesSection;

	// We can get away with this. The tree is fully dynamic
	private FlickrHandler flickrHandler;

	private JSHandler updateHandler;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(itemList, id);

		results.setListModel(new DynamicHtmlListModel<Photo>()
		{
			private List<FlickrListEntry> flist;

			@Override
			@SuppressWarnings("deprecation")
			protected Iterable<Option<Photo>> populateOptions(SectionInfo info)
			{

				if( flickrHandler.getModel(info).isButtonUpdate() )
				{
					return Collections.emptyList();
				}

				// Our only opportunity to pass a value held by the Handler.
				// This will determine if the results are rendered as a multiple
				// checklist or as single-select radio buttons.
				results.getState(info).setDisallowMultiple(!flickrHandler.isMultiple());

				flist = itemList.getModel(info).getItems();
				final List<Option<Photo>> rv = new ArrayList<Option<Photo>>();
				for( Iterator<FlickrListEntry> iter = flist.iterator(); iter.hasNext(); )
				{
					FlickrListEntry flea = iter.next();
					Photo photo = flea.getPhoto();
					String photoTitle = photo.getTitle();
					if( photoTitle != null && photoTitle.length() > TextUtils.TITLE_LENGTH )
					{
						photoTitle = photoTitle.substring(0, TextUtils.TITLE_LENGTH);
					}
					String href = photo.getUrl();

					final LinkRenderer titleLink = new PopupLinkRenderer(new HtmlLinkState(new SimpleBookmark(href)));
					titleLink.setLabel(new TextLabel(photoTitle));
					ImageRenderer thumbnail = new ImageRenderer(photo.getThumbnailUrl(), new TextLabel(photoTitle));

					FlickrResultOption result = new FlickrResultOption(photo);
					result.setAuthor(photo.getOwner().getUsername());
					if( photo.getDatePosted() != null )
					{
						result.setDatePosted(dateRendererFactory.createDateRenderer(photo.getDatePosted()));
					}
					if( photo.getDateTaken() != null )
					{
						result.setDateTaken(new UtcDate(photo.getDateTaken()).toDate());
					}
					String photoDesc = photo.getDescription();
					try
					{
						photoDesc = stripHtmlFrom(photoDesc);
					}
					catch( Exception e )
					{
						throw new RuntimeException(e);
					}

					if( photoDesc != null && photoDesc.length() > TextUtils.DESCRIPTION_LENGTH )
					{
						photoDesc = photoDesc.substring(0, TextUtils.DESCRIPTION_LENGTH);
					}
					result.setDescription(photoDesc);
					result.setLink(titleLink);
					result.setThumbnail(thumbnail);

					result.setPhotoSize(FlickrUtils.describePhotoSize(photo));
					// deprecated, will probably never tell us anything
					result.setViews(photo.getViews());
					String photoLicence = photo.getLicense();
					if( Check.isEmpty(photoLicence) )
					{
						photoLicence = FilterByCreativeCommonsLicencesSection.NO_DATA;
					}

					String displayLicenceVal = filterByCreativeCommonsLicencesSection
						.getDisplayLicenceForValue(photoLicence);
					if( !Check.isEmpty(displayLicenceVal) )
					{
						result.setLicense(displayLicenceVal);
					}

					rv.add(result);
				}

				return rv;
			}

			@Override
			protected Iterable<Photo> populateModel(SectionInfo info)
			{
				return null;
			}

			// trying to prevent a reload of an empty list ...?
			@Override
			public Option<Photo> getOption(SectionInfo info, String value)
			{
				for( FlickrListEntry fle : flist )
				{
					if( fle.getPhoto().getId().equalsIgnoreCase(value) )
					{
						return new FlickrResultOption(fle.getPhoto());
					}
				}
				return null;
			}

		});
	}

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);
		paging.setDefaultPerPage(FlickrUtils.PAGER_PER_PAGE);
		// #5526 - lingering 'screen options' button being rendered on global
		// screen behind the wizard's fancy box, and didn't work as intended:
		// hence suppressed...
		paging.setRenderScreenOptions(false);
		results.setEventHandler(JSHandler.EVENT_CHANGE, updateHandler);
	}

	@Override
	public void processResults(SectionInfo info, FlickrSearchResultsEvent resultsEvent)
	{
		SearchResults<Photo> sersults = resultsEvent.getResults();
		List<Photo> lobjex = sersults.getResults();
		if( lobjex != null )
		{
			for( Photo photo : lobjex )
			{
				FlickrListEntry flePhoto = new FlickrListEntry(photo);
				itemList.addListItem(info, flePhoto);
			}
		}
	}

	@Override
	protected FlickrSearchResultsEvent createResultsEvent(SectionInfo info, FlickrSearchEvent searchEvent)
	{
		SearchResults<Photo> searchResults = doSearch(info, searchEvent);
		return new FlickrSearchResultsEvent(searchResults, searchEvent.isMoreThanKeywordFilter());
	}
	


	@Override
	public FlickrSearchEvent createSearchEvent(SectionInfo info)
	{
		String queryField = flickrQuerySection.getQueryField().getValue(info);
		FlickrSearchParameters params = new FlickrSearchParameters();
		params.setSearchRawText(queryField);
		return new FlickrSearchEvent(flickrLayoutSection, params);
	}

	@Override
	public FlickrListSection getItemList(SectionInfo info)
	{
		return itemList;
	}

	public MultiSelectionList<Photo> getResults()
	{
		return results;
	}

	protected SearchResults<Photo> doSearch(SectionInfo info, FlickrSearchEvent searchEvent)
	{
		// Remove any selections on new search
		results.setSelectedStringValues(info, null);
		SimpleSearchResults<Photo> searchResults = null;
		try
		{
			PhotoList<Photo> photoList = invokeService(info, searchEvent);

			int queryTotal = 0, photoListSize = 0;

			if( photoList != null )
			{
				photoListSize = photoList.size();
				queryTotal = photoList.getTotal();
				searchResults = new SimpleSearchResults<Photo>(photoList, photoListSize, searchEvent.getOffset(),
					queryTotal);
			}
		}
		catch( FlickrException e )
		{
			searchResults = new SimpleSearchResults<Photo>(null, 0, 0, 0);
			String errorMessage = e.getMessage();
			searchResults.setErrorMessage(CurrentLocale.get(SEARCH_ERROR));
			LOGGER.error(errorMessage);
		}

		return searchResults;
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		SearchResultsModel model = getModel(context);
		FlickrSearchResultsEvent resultsEvent = null;

		FlickrSearchEvent searchEvent = createSearchEvent(context);
		searchEvent.setLoggable(true);
		context.processEvent(searchEvent);

		// Either the query field, institution or the user filter must be
		// populated
		if( Check.isEmpty(searchEvent.getQuery()) && Check.isEmpty(searchEvent.getParams().getUserRawText())
			&& Check.isEmpty(searchEvent.getParams().getUserId()) )
		{
			searchEvent.setInvalid(true);
		}

		if( !searchEvent.isInvalid() && !flickrHandler.getModel(context).isButtonUpdate() )
		{
			resultsEvent = createResultsEvent(context, searchEvent);
		}

		if( resultsEvent != null && resultsEvent.isErrored() )
		{
			model.setErrored(true);
			model.setErrorTitle(getErrorTitle(context, searchEvent, resultsEvent));
			model.setErrorMessageLabels(getErrorMessageLabels(context, searchEvent, resultsEvent));
		}
		else
		{
			if( resultsEvent == null )
			{
				model.setSuggestions(new KeyLabel(SUGGEST_WRAP, LABEL_SUGGESTSTART));
			}
			else
			{
				context.processEvent(resultsEvent);
				model.setItemList(getItemList(context));
			}
			boolean resultsAvailable = paging.isResultsAvailable(context);
			model.setResultsAvailable(resultsAvailable);
			model.setResultsText(paging.getResultsText(context));
			if( !resultsAvailable && searchEvent.isFiltered() )
			{
				model.setNoResultsTitle(getNoResultsTitle(context, searchEvent, resultsEvent));
				model.setSuggestions(getSuggestions(context, searchEvent, resultsEvent));
			}
		}

		model.setActions(SectionUtils.renderChildren(context, this, new ResultListCollector(true)).getFirstResult());
		model.setResultsTitle(getDefaultResultsTitle(context, searchEvent, resultsEvent));
		return searchViewFactory.createResult("flickrresults.ftl", this);
	}

	protected Label getHeaderTitle(SectionInfo info)
	{
		return new TextLabel(FLICKR_HOME);
	}

	List<NameValueExtra> getAllLicenceValues()
	{
		return filterByCreativeCommonsLicencesSection != null ? filterByCreativeCommonsLicencesSection
			.getAllLicenceValues() : null;
	}

	private PhotoList<Photo> invokeService(SectionInfo info, FlickrSearchEvent searchEvent) throws FlickrException
	{
		FlickrSearchParameters params = searchEvent.getParams();
		PhotoList<Photo> photoList = null;
		int srchOffset = searchEvent.getOffset();
		// Offset is a zero-based logic but flickr uses ordinal page numbers
		// (ie, lowest is "1")
		int pagerCurrentPage = (srchOffset / FlickrUtils.PAGER_PER_PAGE) + 1;

		FlickrSettings flickrSettings = flickrHandler.getFlickrSettings();

		photoList = flickrService.searchOnParams(params, pagerCurrentPage, FlickrUtils.PAGER_PER_PAGE,
			flickrSettings.getApiKey(), flickrSettings.getApiSharedSecret());

		return photoList;
	}

	public void setFlickrHandler(FlickrHandler flickrHandler)
	{
		this.flickrHandler = flickrHandler;
	}

	public String stripHtmlFrom(String original)

	{
		// If there's no content, return to sender unopened
		if( Check.isEmpty(original) )
		{
			return original;
		}

		String cleaned = Jsoup.clean(original, Whitelist.simpleText());

		return cleaned;
	}

	public static class FlickrResultOption implements Option<Photo>
	{
		private final String value;
		private boolean disabled;
		private SectionRenderable thumbnail;
		private SectionRenderable link;
		private SectionRenderable datePosted;
		private Date dateTaken;
		private String description;
		private String photoSize;
		private String license;
		private String author;
		private long views;
		private final Photo photo;

		public FlickrResultOption(Photo photo)
		{
			this.photo = photo;
			this.value = photo.getId();
			this.description = photo.getDescription();
			this.disabled = false;
		}

		public void setThumbnail(SectionRenderable thumbnail)
		{
			this.thumbnail = thumbnail;
		}

		public void setAuthor(String author)
		{
			this.author = author;
		}

		public void setViews(long views)
		{
			this.views = views;
		}

		public void setDescription(String description)
		{
			this.description = description;
		}

		public void setLink(SectionRenderable link)
		{
			this.link = link;
		}

		public void setDatePosted(SectionRenderable datePosted)
		{
			this.datePosted = datePosted;
		}

		public void setDateTaken(Date dateTaken)
		{
			this.dateTaken = dateTaken;
		}

		public void setPhotoSize(String photoSize)
		{
			this.photoSize = photoSize;
		}

		public void setLicense(String license)
		{
			this.license = license;
		}

		/**
		 * Photo title appears as the link value: there's nothing we want from
		 * calling getName per se.
		 */
		@Override
		public String getName()
		{
			return null;
		}

		@Override
		public String getValue()
		{
			return value;
		}

		@Override
		public boolean isDisabled()
		{
			return disabled;
		}

		@Override
		public boolean isNameHtml()
		{
			return false;
		}

		@Override
		public Photo getObject()
		{
			return photo;
		}

		@Override
		public boolean hasAltTitleAttr()
		{
			return false;
		}

		@Override
		public String getAltTitleAttr()
		{
			return null;
		}

		public SectionRenderable getThumbnail()
		{
			return thumbnail;
		}

		public String getAuthor()
		{
			return author;
		}

		public long getViews()
		{
			return views;
		}

		public String getDescription()
		{
			return description;
		}

		public SectionRenderable getLink()
		{
			return link;
		}

		public SectionRenderable getDatePosted()
		{
			return datePosted;
		}

		public Date getDateTaken()
		{
			return dateTaken;
		}

		public String getPhotoSize()
		{
			return photoSize;
		}

		public String getLicense()
		{
			return license;
		}

		@Override
		public String getGroupName()
		{
			return null;
		}

		@Override
		public void setDisabled(boolean disabled)
		{
			this.disabled = disabled;
		}

	}

	public void setUpdateHandler(JSHandler updateHandler)
	{
		this.updateHandler = updateHandler;
	}
}
