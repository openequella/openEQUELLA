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

package com.tle.web.wizard.render;

import java.io.IOException;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.AbstractBufferedRenderable;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public class DefaultWizardResult extends AbstractBufferedRenderable implements WizardSectionResult
{
	private SectionRenderable title;
	private SectionRenderable html;
	private SectionRenderable tail;

	public DefaultWizardResult(SectionRenderable title, SectionRenderable html, SectionRenderable tail)
	{
		this.title = title;
		this.html = html;
		this.tail = tail;
	}

	@Override
	public SectionRenderable getTitle()
	{
		return title;
	}

	public void setTitle(SectionRenderable title)
	{
		this.title = title;
	}

	@Override
	public SectionRenderable getHtml()
	{
		return html;
	}

	public void setHtml(SectionRenderable html)
	{
		this.html = html;
	}

	public SectionRenderable getTail()
	{
		return tail;
	}

	public void setTail(SectionRenderable tail)
	{
		this.tail = tail;
	}

	@Override
	public void render(SectionWriter writer) throws IOException
	{
		title.realRender(writer);
		html.realRender(writer);
		tail.realRender(writer);
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(title, html, tail);
		super.preRender(info);
	}
}
