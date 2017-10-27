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

package com.tle.web.controls.youtube.migration;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.log4j.Logger;
import org.hibernate.ScrollableResults;
import org.hibernate.annotations.AccessType;
import org.hibernate.classic.Session;

import com.tle.core.guice.Bind;
import com.tle.core.hibernate.impl.HibernateMigrationHelper;
import com.tle.core.migration.AbstractHibernateDataMigration;
import com.tle.core.migration.MigrationInfo;
import com.tle.core.migration.MigrationResult;
import com.tle.core.plugins.impl.PluginServiceImpl;
import com.tle.core.xml.service.XmlService;
import com.tle.web.sections.SectionUtils;

/**
 * Solution for Redmine #7946. Older youtube attachments record only the
 * playUrl, and not the videoId as a discrete value. The youtube player now
 * looks for the videoId saved in the Attachments metadata, and composes a new
 * URL on the fly, no longer restricted to the 'old-style' playUrl value.
 * Accordingly, for older youtube attachments which lack the videoId, it is
 * necessary to parse the playUrl value, extract and preserve the video Id
 * substring as the videoId value.<br>
 * Looking for URL values of the form
 * http://www.youtube.com/x/y/z/AA00BB11CC3?foo=whatever&bar=whateverelse, where
 * we're indifferent as to the path segments (x/y/z) if any between the server
 * and the 11-character identifier, and equally indifferent as to the param
 * key-values pairs if any. What we're looking for is the vidoeId string which
 * is the last path segment before the key-value parameters. (In practice this
 * videoId appears to be always 11 characters long). We look for an explicit
 * videoId key-value in the attachments attribute Map, and having established
 * the absence thereof, update the attachments data map with an explicit vidoeId
 * extracted from the full URL.<br>
 * NB - some youtube URLs in the system lack a '?' dividing the URL path from
 * the query key-value parameters eg
 * http://www.youtube.com/x/y/z/AA00BB11CC3&foo=woteva&bar=wotevaelse, hence we
 * allow our parse of the URL to accept this oddity.
 * 
 * @author larry
 */
@SuppressWarnings("nls")
@Bind
@Singleton
public class UpdateOlderYoutubeAttachmentMigration extends AbstractHibernateDataMigration
{
	private static final Logger LOGGER = Logger.getLogger(UpdateOlderYoutubeAttachmentMigration.class);
	private static final String keyPrefix = PluginServiceImpl.getMyPluginId(UpdateOlderYoutubeAttachmentMigration.class)
		+ ".migration.";

	private static final String YOUTUBE_URL = "http://www.youtube.com";
	private static final String PLAY_URL_KEY = "playUrl";
	private static final String VIDEO_ID_KEY = "videoId";
	private static final String KEY_PATH = "$PATH$";

	@Inject
	private XmlService xmlService;

	@Override
	public MigrationInfo createMigrationInfo()
	{
		return new MigrationInfo(keyPrefix + "older.youtube.title", keyPrefix + "older.youtube.description");
	}

	@Override
	protected void executeDataMigration(HibernateMigrationHelper helper, MigrationResult result, Session session)
		throws Exception
	{
		// Query for attachments with a node with text value "playUrl", and
		// another node with text value beginning with the youtube URL
		String query = "From Attachment a " + " where a.data like '%<string>" + PLAY_URL_KEY + "</string>%'"
			+ " and a.data like '%<string>" + YOUTUBE_URL + "%'";

		ScrollableResults scroll = session.createQuery(query).scroll();
		result.incrementStatus();
		int numProcessed = 0;
		while( scroll.next() )
		{
			FakeAttachment attachment = (FakeAttachment) scroll.get(0);

			Map<String, Object> dataMap = xmlService.deserialiseFromXml(getClass().getClassLoader(), attachment.data);
			boolean dataMapModified = checkForUpdate(dataMap);
			if( dataMapModified )
			{
				attachment.data = xmlService.serialiseToXml(dataMap);
				session.save(attachment);
				session.flush();
				session.clear();
				result.incrementStatus();
				numProcessed++;
			}
		}
		LOGGER.info(numProcessed + " youtube URLs updated with extracted videoIds");
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#countDataMigrations(com.tle.core.hibernate.impl.HibernateMigrationHelper,
	 *      org.hibernate.classic.Session)
	 */
	@Override
	protected int countDataMigrations(HibernateMigrationHelper helper, Session session)
	{
		// Burdensome to evaluate
		return 1;
	}

	public static boolean checkForUpdate(Map<String, Object> dataMap)
	{
		boolean wasModified = false;
		Object playUrlObj = dataMap.get(PLAY_URL_KEY);
		if( playUrlObj != null )
		{
			String playUrl = playUrlObj.toString();
			if( playUrl.startsWith(YOUTUBE_URL) ) // we assume
			{
				int lastSlash = playUrl.lastIndexOf('/');
				// URL longer than just the server path
				if( lastSlash > 6 && lastSlash < playUrl.length() )
				{
					Object videoIdObj = dataMap.get(VIDEO_ID_KEY);
					if( videoIdObj == null )
					{
						Map<String, String[]> parsedUrlMap = SectionUtils.parseParamUrl(playUrl, true);
						String urlPath = parsedUrlMap.get(KEY_PATH)[0];
						String extractedVideoId = urlPath.substring(lastSlash + 1);
						dataMap.put(VIDEO_ID_KEY, extractedVideoId);
						wasModified = true;
					}
				}
			}
		}
		return wasModified;
	}

	/**
	 * @see com.tle.core.migration.AbstractHibernateDataMigration#getDomainClasses()
	 */
	@Override
	protected Class<?>[] getDomainClasses()
	{
		return new Class[]{FakeAttachment.class};
	}

	@Entity(name = "Attachment")
	@AccessType("field")
	public static class FakeAttachment
	{
		@Id
		@GeneratedValue(strategy = GenerationType.AUTO)
		long id;

		@Column(length = 8192)
		String data;
	}
}
