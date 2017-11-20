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

package com.tle.mypages.parse.conversion;

import java.util.ArrayList;
import java.util.List;

import com.dytech.edge.common.Constants;
import com.tle.beans.item.ItemKey;

@SuppressWarnings("nls")
public class StagingConversion
{
	private static final String ITEMS = "items/";
	private static final String PREVIEW = "preview/";
	private static final String FILE = "file/";

	private final List<HrefConversion> conversions = new ArrayList<HrefConversion>();

	public StagingConversion(boolean toStaging, ItemKey itemId, String previewId, String stagingId)
	{
		this(toStaging, itemId, previewId, stagingId, Constants.BLANK, Constants.BLANK);
	}

	public StagingConversion(boolean toStaging, ItemKey itemId, String previewId, String stagingId,
		String sourceFolder, String destFolder)
	{
		String source1;
		String source2;
		String dest1;
		String dest2;
		String previewPrefix = getPreviewPrefix(previewId);
		String stagingPrefix = getFileStagingPrefix(stagingId);
		if( itemId == null )
		{
			source1 = previewPrefix;
			source2 = stagingPrefix;
			dest1 = previewPrefix;
			dest2 = stagingPrefix;
		}
		else
		{
			String itemPrefix = getItemPrefix(itemId);
			String itemFilePrefix = getItemFilePrefix(itemId);

			if( toStaging )
			{
				source1 = itemPrefix;
				source2 = itemFilePrefix;
				dest1 = previewPrefix;
				dest2 = stagingPrefix;
			}
			else
			{
				dest1 = itemPrefix;
				dest2 = itemFilePrefix;
				source1 = previewPrefix;
				source2 = stagingPrefix;
				conversions.add(new RawAnchorReplacementConversion());
			}
		}

		conversions.add(new PrefixConversion(source1 + sourceFolder, dest1 + destFolder));
		conversions.add(new PrefixConversion(source2 + sourceFolder, dest2 + destFolder));
	}

	private String getItemPrefix(ItemKey itemId)
	{
		return ITEMS + itemId.toString() + '/';
	}

	private String getPreviewPrefix(String previewId)
	{
		return PREVIEW + previewId + "/1/";
	}

	private String getFileStagingPrefix(String stagingId)
	{
		return FILE + stagingId + "/$/";
	}

	private String getItemFilePrefix(ItemKey itemId)
	{
		return FILE + itemId + '/';
	}

	public List<HrefConversion> getConversions()
	{
		return conversions;
	}
}
