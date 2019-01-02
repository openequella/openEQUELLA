/*
 * Copyright 2019 Apereo
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
