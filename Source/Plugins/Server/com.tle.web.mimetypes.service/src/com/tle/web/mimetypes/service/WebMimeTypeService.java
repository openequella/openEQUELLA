package com.tle.web.mimetypes.service;

import java.net.URL;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.mime.MimeEntry;

@NonNullByDefault
public interface WebMimeTypeService
{
	@Nullable
	MimeEntry getEntryForFilename(String filename);

	String getMimeTypeForFilename(String filename);

	@Nullable
	MimeEntry getEntryForMimeType(String mimeType);

	URL getIconForEntry(@Nullable MimeEntry entry);

	URL getIconForEntry(@Nullable MimeEntry entry, boolean allowCache);

	URL getDefaultIconForEntry(@Nullable MimeEntry entry);

	boolean hasCustomIcon(MimeEntry entry);

	/**
	 * @param entry
	 * @param base64icon May be null, in which case the default icon will be
	 *            restored
	 */
	void setIconBase64(MimeEntry entry, @Nullable String base64Icon);
}
