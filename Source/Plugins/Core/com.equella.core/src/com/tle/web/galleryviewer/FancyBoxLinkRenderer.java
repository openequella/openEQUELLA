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

package com.tle.web.galleryviewer;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQueryFancyBox;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

public class FancyBoxLinkRenderer extends LinkRenderer
{
	private static final String CLASS_FANCYLINK = "fancyLink"; //$NON-NLS-1$
	private static final FancyBoxLinkReady INST = new FancyBoxLinkReady();

	public FancyBoxLinkRenderer(HtmlLinkState state)
	{
		super(state);
		addClass(CLASS_FANCYLINK);
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		writer.preRender(INST);
		super.realRender(writer);
	}

	@Override
	public void ensureClickable()
	{
		// It is clickable
	}

	public static class FancyBoxLinkReady implements PreRenderable
	{

		@Override
		public void preRender(PreRenderContext info)
		{
			info.addReadyStatements(new JQueryStatement(Type.RAW, ".attachments-browse ." + CLASS_FANCYLINK,
				new FunctionCallExpression(JQueryFancyBox.FANCYBOX, new ObjectExpression("type", "image"))));
		}

	}
}