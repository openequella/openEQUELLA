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

package com.tle.core.item.convert.migration;

import java.io.IOException;
import java.io.InputStream;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;
import com.tle.core.institution.convert.PostReadMigrator;
import com.tle.core.item.convert.ItemConverter.ItemConverterInfo;
import com.tle.core.item.service.impl.ItemUrlGatherer;
import com.tle.core.services.FileSystemService;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;

@Bind
@Singleton
@SuppressWarnings("nls")
public class UpdateReferencedUrlsOnImport implements PostReadMigrator<ItemConverterInfo>
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ItemUrlGatherer urlGatherer;
	@Inject
	private URLCheckerService urlChecker;

	@Override
	public void migrate(ItemConverterInfo info) throws IOException
	{
		// Warning - this migrator is configured to always run on import,
		// regardless of whether it has been run on the data previously.

		final Item item = info.getItem();
		item.getReferencedUrls().clear();

		try( InputStream in = fileSystemService.read(info.getFileHandle(), "_ITEM/item.xml") )
		{
			for( String url : urlGatherer.gatherURLs(item, new PropBagEx(in)) )
			{
				item.getReferencedUrls().add(urlChecker.getUrlStatus(url, URLCheckMode.IMPORT));
			}
		}
	}
}
