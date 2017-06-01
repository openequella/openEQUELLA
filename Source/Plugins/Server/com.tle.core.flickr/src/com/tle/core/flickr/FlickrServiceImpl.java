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

package com.tle.core.flickr;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.Transport;
import com.flickr4java.flickr.people.PeopleInterface;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.PhotosInterface;
import com.google.common.base.Throwables;
import com.tle.common.Check;
import com.tle.core.guice.Bind;

/**
 * @author larry
 */
@SuppressWarnings("nls")
@Bind(FlickrService.class)
@Singleton
public class FlickrServiceImpl implements FlickrService
{
	private static final String FLICKR_API_KEY = "22e4edfe7564577fc7dff66f1e550f2e";
	private static final String FLICKR_SHARED_SECRET = "7f3e1515e85a94e1";

	/**
	 * A string to place in the search params in order to flag that we want this
	 * icon_server, original_format, last_update, geo, media, path_alias,
	 */
	private static final Set<String> EXTRAS_SET = new HashSet<String>(Arrays.asList("description", "license",
		"date_upload", "date_taken", "owner_name", "tags", "machine_tags", "o_dims", "views", "url_sq", "url_t",
		"url_s", "url_m", "url_z", "url_l", "url_o"));

	/**
	 * We allow that where tags are sent by caller, that either the totality of
	 * tags is the search criterion (hence "all"), or just "any".
	 */
	private static final String ANY = "any";
	private static final String ALL = "all";

	private static final String SERVICE_NAME = "flickr.photos.search";

	/**
	 * the expanded class FlickrSearchParameters is queried to determine what
	 * fine-tuning of the search parameters is required before submitting to the
	 * flickr service API. If a tag search has been requested, the tags will be
	 * tokenised on commas if any are present, otherwise on spaces. If a value
	 * has been set for a flickr user string, that string is examined to see if
	 * it is an email, a flick user-id or (failing either) a flickr user-name.
	 */
	@Override
	public PhotoList<Photo> searchOnParams(FlickrSearchParameters params, int pageToGet, int queryLimit,
		String apiKey,
		String apiSharedSecret) throws FlickrException
	{
		Flickr flickr = getFlickr(apiKey, apiSharedSecret);

		// From the unparsed user input, see if there's either a flick userid
		// already provided; otherwise get the flickr id by specialised flickr
		// query
		/* boolean sendingFlickrUserId = */
		extractUserSearchString(flickr, params);

		if( params.getSearchRawText() != null )
		{
			String rawText = params.getSearchRawText().trim();
			if( rawText.length() > 0 )
			{
				if( params.isTagsNotText() )
				{
					if( rawText.contains(",") )
					{
						params.setTags(rawText.split(","));
					}
					else
					{
						params.setTags(rawText.split(" "));
					}
					if( params.isTagsAll() )
					{
						params.setTagMode(ALL);
					}
					else
					{
						params.setTagMode(ANY);
					}
				}
				else
				{
					params.setText(rawText);
				}
			}
		}

		// The various extras flags (to specify level of detail retrieved)
		params.setExtras(EXTRAS_SET);

		PhotosInterface photosInterface = flickr.getPhotosInterface();

		try
		{
			PhotoList<Photo> photoList = photosInterface.search(params, queryLimit, pageToGet);
			return photoList;
		}
		catch( FlickrException fe ) // NOSONAR
		{
			throw fe;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}
	}

	/**
	 * Determine if there is non-whitespace content in the "user" field, and if
	 * to to decide if it's a flickr userid (not so likely, but more efficient)
	 * or a flickr user's email address. If neither, we assume it's a flickr
	 * username. Assign the resultant value in the searchParams.
	 * 
	 * @param info SectionInfo containing the raw text field
	 * @param params an in/out parameter, into which the user info is inserted
	 * @return true if we eventually set a value as flickr id, false otherwise
	 */
	private boolean extractUserSearchString(Flickr flickr, FlickrSearchParameters params) throws FlickrException
	{
		boolean isEmail = false, isFlickrId = false, isFlickrUsername = false, haveFlickrUserId = false;

		String strInput = params.getUserRawText();
		if( strInput != null )
		{
			strInput = strInput.trim();
			if( strInput.length() > 0 )
			{
				int indexOfAt = strInput.indexOf('@');
				// Input contains a solitary occurrence of @ within, but not
				// at either end? So it's either an email or a FlickrId
				if( indexOfAt >= 1 && strInput.length() > indexOfAt + 1
					&& strInput.substring(indexOfAt + 1).indexOf('@') < 0 )
				{
					// at the very least, an email config is "a@b.c"
					int relativeIndexOfDot = strInput.substring(indexOfAt).indexOf('.');
					if( relativeIndexOfDot >= 2 )
					{
						if( strInput.length() >= relativeIndexOfDot + indexOfAt + 2 )
						{
							isEmail = true;
						}
					}
					else
					{
						boolean couldBeFlickrId = true;
						for( int i = 0; i < indexOfAt; ++i )
						{
							if( !Character.isDigit(strInput.substring(0, indexOfAt).charAt(i)) )
							{
								couldBeFlickrId = false;
								break;
							}
						}
						// we assume flickr username contains digits + '@' + a
						// bit more alphanumeric
						isFlickrId = couldBeFlickrId;
					}

				}
				// If it's neither an email nor flickrId, the default assumption
				// is that it's a flickr username
				if( !(isEmail || isFlickrId) )
				{
					isFlickrUsername = true;
				}
			}
		}
		String flickrUserId = null;
		if( isEmail )
		{
			// do a search based on email address
			flickrUserId = queryFlickrOnUserEmail(flickr, strInput);
		}
		else if( isFlickrId )
		{
			// String contains solitary '@'
			flickrUserId = strInput;
		}
		else if( isFlickrUsername )
		{
			// we assume its an flick user's username of arbitrary format
			flickrUserId = queryFlickrOnUsername(flickr, strInput);
		}

		if( flickrUserId != null && flickrUserId.length() > 0 )
		{
			params.setUserId(flickrUserId);
			haveFlickrUserId = true;
		}
		return haveFlickrUserId;
	}

	/**
	 * Having what we presume is an email, query Flickr for the UserId string
	 * 
	 * @param presumedEmail
	 * @return Flickr user's Id string if found, otherwise null
	 * @throws FlickrException Flickr throws with a user not found message if
	 *             such applies
	 */
	private String queryFlickrOnUserEmail(Flickr flickr, String presumedEmail) throws FlickrException
	{
		String flickrUserId = null;
		PeopleInterface pi = flickr.getPeopleInterface();
		User flickrUser = null;
		try
		{
			flickrUser = pi.findByEmail(presumedEmail);
		}
		catch( FlickrException fe ) // NOSONAR - throw these in the raw, wrap
									// any others
		{
			throw fe;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}

		if( flickrUser != null )
		{
			flickrUserId = flickrUser.getId();
		}

		return flickrUserId;
	}

	/**
	 * Having what we presume is an email, query Flickr for the UserId string
	 * 
	 * @param presumedEmail
	 * @return Flickr user's Id string if found, otherwise null
	 * @throws FlickrException Flickr throws with a user not found message if
	 *             such applies
	 */
	private String queryFlickrOnUsername(Flickr flickr, String presumedUsername) throws FlickrException
	{
		String flickrUserId = null;
		PeopleInterface pi = flickr.getPeopleInterface();
		User flickrUser = null;
		try
		{
			flickrUser = pi.findByUsername(presumedUsername);
		}
		catch( FlickrException fe ) // NOSONAR - throw these in the raw, wrap
									// any others
		{
			throw fe;
		}
		catch( Exception e )
		{
			throw Throwables.propagate(e);
		}

		if( flickrUser != null )
		{
			flickrUserId = flickrUser.getId();
		}

		return flickrUserId;
	}

	@Override
	public String getServiceName()
	{
		return SERVICE_NAME;
	}

	private Flickr getFlickr(String apiKey, String apiSharedSecret)
	{
		Transport rest = new REST();
		if( Check.isEmpty(apiKey) )
		{
			return new Flickr(FLICKR_API_KEY, FLICKR_SHARED_SECRET, rest);
		}
		return new Flickr(apiKey, apiSharedSecret, rest);
	}
}
