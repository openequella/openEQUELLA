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

package com.tle.core.google;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.gdata.client.books.BooksService;
import com.google.gdata.client.books.VolumeQuery;
import com.google.gdata.data.books.VolumeEntry;
import com.google.gdata.data.books.VolumeFeed;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.web.google.api.GoogleApiUtils;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind(GoogleService.class)
@Singleton
public class GoogleServiceImpl implements GoogleService
{
	private static final String EQUELLA = "EQUELLA";

	@Inject
	private ConfigurationService configService;

	private YouTube tubeService;
	private BooksService booksService;

	@Override
	public List<SearchResult> searchVideos(String query, String orderBy, long limit) throws GoogleJsonResponseException
	{
		return search(query, orderBy, null, limit, "id,snippet").getItems();
	}

	@Override
	public List<SearchResult> searchVideoIds(String query, String orderBy, long limit)
		throws GoogleJsonResponseException
	{
		return search(query, orderBy, null, limit, "id").getItems();
	}

	@Override
	public List<SearchResult> searchVideoIdsWithinChannel(String query, String orderBy, String channelId, long limit)
		throws GoogleJsonResponseException
	{
		return search(query, orderBy, channelId, limit, "id").getItems();
	}

	private SearchListResponse search(String query, String orderBy, String channelId, long limit, String data)
		throws GoogleJsonResponseException
	{
		try
		{
			YouTube.Search.List search = getTubeService().search().list(data);
			search.setKey(getApiKey());
			search.setQ(query);
			search.setType("video");
			search.setMaxResults(limit);
			search.setVideoEmbeddable("true");
			search.setOrder(orderBy);

			if( !Check.isEmpty(channelId) )
			{
				search.setChannelId(channelId);
			}

			SearchListResponse searchListResponse = search.execute();

			return searchListResponse;
		}
		catch( GoogleJsonResponseException gjre )
		{
			/*
			 * This is some ghetto code to handle the switch from V2 to V3. All
			 * V3 calls work with channelIds and there is no longer the option
			 * to use userIds.
			 */
			String message = gjre.getDetails().getMessage();
			if( "Invalid channel.".equals(message) )
			{
				Channel channel = getChannelForUser(channelId);
				if( channel != null )
				{
					SearchListResponse searchListResponse = search(query, orderBy, channel.getId(), limit, data);
					return searchListResponse;
				}
			}

			// Throw the exception to be handled by the Youtube Handler
			throw gjre;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	@Override
	public Channel getChannel(String channelId)
	{
		List<Channel> channels = getChannels(Lists.newArrayList(channelId));
		return Check.isEmpty(channels) ? null : channels.get(0);
	}

	@Override
	public Channel getChannelForUser(String userId)
	{
		try
		{
			YouTube.Channels.List channels = getTubeService().channels().list("snippet");
			channels.setKey(getApiKey());
			channels.setForUsername(userId);
			ChannelListResponse channelListResponse = channels.execute();

			List<Channel> items = channelListResponse.getItems();

			return Check.isEmpty(items) ? null : items.get(0);
		}
		catch( Exception ex )
		{
			throw Throwables.propagate(ex);
		}
	}

	@Override
	public List<Channel> getChannels(List<String> channelIds)
	{
		try
		{
			YouTube.Channels.List channels = getTubeService().channels().list("snippet");
			channels.setKey(getApiKey());
			channels.setId(Joiner.on(",").join(channelIds));
			ChannelListResponse channelListResponse = channels.execute();

			return channelListResponse.getItems();
		}
		catch( Exception ex )
		{
			throw Throwables.propagate(ex);
		}
	}

	@Override
	public Video getVideo(String videoId)
	{
		List<Video> videos = getVideos(Lists.newArrayList(videoId));
		return Check.isEmpty(videos) ? null : videos.get(0);
	}

	@Override
	public List<Video> getVideos(List<String> videoIds)
	{
		try
		{
			YouTube.Videos.List videos = getTubeService().videos().list("id,snippet,player,contentDetails,statistics");
			videos.setKey(getApiKey());
			videos.setId(Joiner.on(",").join(videoIds));
			VideoListResponse vlr = videos.execute();

			return vlr.getItems();
		}
		catch( Exception ex )
		{
			throw Throwables.propagate(ex);
		}
	}

	private synchronized YouTube getTubeService()
	{
		if( tubeService == null )
		{
			tubeService = new YouTube.Builder(new NetHttpTransport(), new JacksonFactory(),
				new HttpRequestInitializer()
				{
					@Override
					public void initialize(HttpRequest request) throws IOException
					{
						// Nothing?
					}
				}).setApplicationName(EQUELLA).build();
		}
		return tubeService;
	}

	@Override
	public VolumeEntry getBook(String bookId)
	{
		try
		{
			return getBooksService().getEntry(new URL(bookId), VolumeEntry.class);
		}
		catch( Exception ex )
		{
			throw Throwables.propagate(ex);
		}
	}

	@Override
	public VolumeFeed searchBooks(String query, int offset, int limit)
	{
		try
		{
			VolumeQuery vquery = new VolumeQuery(new URL("http://www.google.com/books/feeds/volumes"));
			vquery.setMinViewability(VolumeQuery.MinViewability.PARTIAL);
			vquery.setFullTextQuery(query.trim());
			vquery.setStartIndex(offset + 1);
			vquery.setMaxResults(limit);
			return getBooksService().query(vquery, VolumeFeed.class);
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	private synchronized BooksService getBooksService()
	{
		if( booksService == null )
		{
			booksService = new BooksService("EQUELLA");
		}
		return booksService;
	}

	private String getApiKey()
	{
		return configService.getProperty(GoogleApiUtils.GOOGLE_API_KEY);
	}

	@Override
	public boolean isEnabled()
	{
		return !Check.isEmpty(getApiKey());
	}
}
