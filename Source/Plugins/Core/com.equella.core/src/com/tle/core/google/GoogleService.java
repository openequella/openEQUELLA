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
