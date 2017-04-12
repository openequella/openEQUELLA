package com.tle.core.freetext.extracter.standard;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.TextExtracterExtension;

/**
 * @author aholland
 */
public abstract class AbstractTextExtracterExtension implements TextExtracterExtension
{
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
		return "com.tle.core.freetext.extracter.standard." + getKey().toLowerCase() + ".title"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
