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

package com.tle.core.remoterepo.merlot.syndication;

import org.jdom2.Namespace;

import com.rometools.rome.feed.module.Module;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public interface MerlotModule extends Module
{
	String URI = "http://www.merlot.org/merlot/materials-rest";
	Namespace NAMESPACE = Namespace.getNamespace(URI);

	String getTitle();

	void setTitle(String title);

	String getUrl();

	void setUrl(String url);
}
