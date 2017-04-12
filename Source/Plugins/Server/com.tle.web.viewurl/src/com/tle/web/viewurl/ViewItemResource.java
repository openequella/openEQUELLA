package com.tle.web.viewurl;

import java.util.Set;

import com.tle.annotation.Nullable;
import com.tle.web.sections.Bookmark;
import com.tle.web.stream.ContentStream;
import com.tle.web.viewable.ViewableItem;

public interface ViewItemResource
{
	String KEY_MUST_SHOW = "$MUST$SHOW"; //$NON-NLS-1$
	String KEY_PREFER_STREAM = "$PREFER$STREAM$"; //$NON-NLS-1$

	ViewableItem getViewableItem();

	boolean getBooleanAttribute(Object key);

	<T> T getAttribute(Object key);

	void setAttribute(Object key, Object value);

	Set<String> getPrivileges();

	String getFilepath();

	String getFileDirectoryPath();

	String getFilenameWithoutPath();

	Bookmark createCanonicalURL();

	int getForwardCode();

	ContentStream getContentStream();

	@Nullable
	ViewItemViewer getViewer();

	@Nullable
	String getDefaultViewerId();

	String getMimeType();

	@Nullable
	ViewAuditEntry getViewAuditEntry();

	void wrappedBy(ViewItemResource resource);

	boolean isPathMapped();

	boolean isRestrictedResource();

}
