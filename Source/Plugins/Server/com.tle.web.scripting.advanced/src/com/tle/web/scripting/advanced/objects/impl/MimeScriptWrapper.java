package com.tle.web.scripting.advanced.objects.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.tle.beans.mime.MimeEntry;
import com.tle.core.mimetypes.MimeTypeService;
import com.tle.web.scripting.advanced.objects.MimeScriptObject;
import com.tle.web.scripting.advanced.types.MimeTypeScriptType;
import com.tle.web.scripting.impl.AbstractScriptWrapper;

/**
 * @author aholland
 */
public class MimeScriptWrapper extends AbstractScriptWrapper implements MimeScriptObject
{
	private static final long serialVersionUID = 1L;

	private final MimeTypeService mimeService;

	public MimeScriptWrapper(MimeTypeService mimeService)
	{
		this.mimeService = mimeService;
	}

	@Override
	public MimeTypeScriptType getMimeTypeForFilename(String filename)
	{
		MimeEntry entry = mimeService.getEntryForFilename(filename);
		if( entry != null )
		{
			return new MimeTypeScriptTypeImpl(entry);
		}
		return null;
	}

	public static class MimeTypeScriptTypeImpl implements MimeTypeScriptType
	{
		private static final long serialVersionUID = 1L;

		private final MimeEntry wrapped;
		private List<String> otherExtensions;

		public MimeTypeScriptTypeImpl(MimeEntry wrapped)
		{
			this.wrapped = wrapped;
		}

		@Override
		public String getDescription()
		{
			// null safe it
			String description = wrapped.getDescription();
			return (description == null ? "" : description); //$NON-NLS-1$
		}

		@Override
		public List<String> getFileExtensions()
		{
			if( otherExtensions == null )
			{
				otherExtensions = new ArrayList<String>(wrapped.getExtensions());
				Collections.sort(otherExtensions);
			}
			return otherExtensions;
		}

		@Override
		public String getType()
		{
			return wrapped.getType();
		}
	}
}
