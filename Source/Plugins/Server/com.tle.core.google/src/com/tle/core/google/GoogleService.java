package com.tle.core.google;

import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.gdata.data.books.VolumeEntry;
import com.google.gdata.data.books.VolumeFeed;

/**
 * @author Aaron
 */
public interface GoogleService
{
	// YouTube
	Video getVideo(String videoId);

	List<Video> getVideos(List<String> videoIds);

	List<SearchResult> searchVideos(String query, String orderBy, long limit) throws GoogleJsonResponseException;

	List<SearchResult> searchVideoIds(String query, String orderBy, long limit) throws GoogleJsonResponseException;

	List<SearchResult> searchVideoIdsWithinChannel(String query, String orderBy, String channel, long limit)
		throws GoogleJsonResponseException;

	Channel getChannel(String channelId);

	Channel getChannelForUser(String userId);

	List<Channel> getChannels(List<String> channelIds);

	// Google books
	// Volume getBook(String bookId);

	// List<SearchResult> searchBooks(String query, int offset, int limit);

	VolumeEntry getBook(String bookId);

	VolumeFeed searchBooks(String query, int offset, int limit);

	boolean isEnabled();

}
