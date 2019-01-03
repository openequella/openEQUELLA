/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.viewurl;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.standard.renderers.LinkTagRenderer;

@NonNullByDefault
public interface ResourceViewer
{
	boolean supports(SectionInfo info, ViewableResource resource);

	@Nullable
	ResourceViewerConfigDialog createConfigDialog(String parentId, SectionTree tree,
		ResourceViewerConfigDialog defaultDialog);

	LinkTagRenderer createLinkRenderer(SectionInfo info, ViewableResource resource, Bookmark viewUrl);

	LinkTagRenderer createLinkRenderer(SectionInfo info, ViewableResource resource);

	ViewItemUrl createViewItemUrl(SectionInfo info, ViewableResource resource);

	Bookmark createStreamUrl(SectionInfo info, ViewableResource resource);

	@Nullable
	ViewItemViewer getViewer(SectionInfo info, ViewItemResource resource);
}
