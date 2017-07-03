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

package com.tle.core.services;

import java.net.URI;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import hurl.build.QueryBuilder;

/**
 * @author Nicholas Read
 */
public interface UrlService
{
	URL getAdminUrl();

	URI getUriForRequest(HttpServletRequest request, String parameters);

	QueryBuilder getQueryBuilderForRequest(HttpServletRequest request);

	URI getBaseUriFromRequest(HttpServletRequest request);

	boolean isRelativeUrl(String url);
}
