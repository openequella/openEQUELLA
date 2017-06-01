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

import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;


/**
 * @author larry
 */
public interface FlickrService
{
	/**
	 * @param params prepopulated query values, to be supplemented by
	 *            boilerplate defaults to guide the detail of returned content
	 * @param queryLimit the maximum number of values to return for a query,
	 *            effectively the number of result items the callers wants to
	 *            display at once. Flickr limit is 500.
	 * @param pageToGet the ordinal number of the page to retrieve, starting
	 *            from page 1
	 * @throws FlickrException if we expect the caller to handle the exception
	 *             gently, otherwise rethrow FlickrExceptions (and any others)
	 *             as runtime exceptions
	 */
	PhotoList<Photo> searchOnParams(FlickrSearchParameters params, int queryLimit, int pageToGet, String apiKey,
		String sharedSecret) throws FlickrException;

	String getServiceName();
}
