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

package com.tle.core.institution.convert;

import com.dytech.edge.common.Constants;
import com.tle.common.filesystem.handle.SubTemporaryFile;
import com.tle.common.filesystem.handle.TemporaryFileHandle;

public abstract class AbstractItemXmlMigrator implements ItemXmlMigrator
{
	@Override
	public void beforeMigrate(ConverterParams params, TemporaryFileHandle staging, SubTemporaryFile file)
		throws Exception
	{
		// nothing
	}

	@Override
	public void afterMigrate(ConverterParams params, SubTemporaryFile file) throws Exception
	{
		// nothing;
	}

	@SuppressWarnings("nls")
	public SubTemporaryFile getDataFolder(SubTemporaryFile itemsHandle, String entry)
	{
		return new SubTemporaryFile(itemsHandle, entry.replace(".xml", Constants.BLANK));
	}

	@SuppressWarnings("nls")
	public SubTemporaryFile getMetadataXml(SubTemporaryFile itemsHandle, String entry)
	{
		return new SubTemporaryFile(getDataFolder(itemsHandle, entry), "_ITEM/item.xml");
	}
}
