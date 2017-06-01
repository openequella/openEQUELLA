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

package com.tle.web.echo.viewer;

import javax.inject.Singleton;

import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.web.echo.data.EchoAttachmentData;
import com.tle.web.sections.SectionInfo;
import com.tle.web.viewurl.ViewableResource;

@Bind
@Singleton
@SuppressWarnings("nls")
public class EchoPlayerViewer extends AbstractEchoViewer
{
	@Override
	public boolean supports(SectionInfo info, ViewableResource resource)
	{
		IAttachment a = resource.getAttachment();
		if( a != null )
		{
			EchoAttachmentData ed = getEchoAttachmentData(a);
			boolean supportsMime = super.supports(info, resource);
			return ed == null ? supportsMime : supportsMime && !Check.isEmpty(ed.getEchoData().getEchoLinkUrl());
		}
		return super.supports(info, resource);
	}

	@Override
	public String getViewerId()
	{
		return "echoPlayerViewer";
	}
}