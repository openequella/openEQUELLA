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

package com.tle.web.qti.viewer.questions.renderer;

import java.io.IOException;
import java.net.URI;

import uk.ac.ed.ph.jqtiplus.node.item.Stylesheet;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.Check;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;

/**
 * @author Aaron
 */
public class StylesheetRenderer extends QtiNodeRenderer
{
	private final Stylesheet model;

	@AssistedInject
	public StylesheetRenderer(@Assisted Stylesheet model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preProcess()
	{
		// Nah
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		final URI href = model.getHref();
		if( href != null )
		{
			final String url = (isRelativeUrl(href) ? getContext().getViewResourceUrl(href.toString()).getHref() : href
				.toString());
			info.addCss(url);
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		// Nothing
	}

	private boolean isRelativeUrl(URI url)
	{
		return Check.isEmpty(url.getHost());
	}
}
