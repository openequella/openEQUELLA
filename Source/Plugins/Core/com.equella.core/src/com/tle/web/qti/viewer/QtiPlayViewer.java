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

package com.tle.web.qti.viewer;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.core.qti.QtiConstants;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.viewers.AbstractResourceViewer;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
public class QtiPlayViewer extends AbstractResourceViewer
{
	static
	{
		PluginResourceHandler.init(QtiPlayViewer.class);
	}

	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		return resource.getMimeType().equals(QtiConstants.TEST_MIME_TYPE);
	}

	@Override
	public Class<? extends SectionId> getViewerSectionClass()
	{
		return QtiPlayViewerSection.class;
	}

	@SuppressWarnings("nls")
	@Override
	public String getViewerId()
	{
		return "qtiTestViewer";
	}

}
