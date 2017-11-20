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

package com.tle.core.freetext.extracter.standard;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.TextExtracterExtension;
import com.tle.core.plugins.AbstractPluginService;

/**
 * @author aholland
 */
public abstract class AbstractTextExtracterExtension implements TextExtracterExtension
{
	private static String PFX_KEY = AbstractPluginService.getMyPluginId(AbstractTextExtracterExtension.class)+".";
	protected String getKey()
	{
		return this.getClass().getSimpleName();
	}

	protected abstract boolean isSupportedByDefault(MimeEntry mimeEntry);

	@Override
	public void setEnabledForMimeEntry(MimeEntry mimeEntry, boolean enabled)
	{
		mimeEntry.getAttributes().put(getKey(), Boolean.toString(enabled));
	}

	@Override
	public boolean isEnabledForMimeEntry(MimeEntry mimeEntry)
	{
		String enabled = mimeEntry.getAttributes().get(getKey());
		if( enabled == null )
		{
			// is the mimeEntry one of the ones we will support by default?
			return isSupportedByDefault(mimeEntry);
		}
		return Boolean.valueOf(enabled);
	}

	@Override
	public String getNameKey()
	{
		return PFX_KEY + getKey().toLowerCase() + ".title"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
