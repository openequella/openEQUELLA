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

import java.util.Iterator;

import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.mathml.Math;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.base.FlowStaticRenderer;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.js.generic.statement.ScriptStatement;

/**
 * @author Aaron
 */
@NonNullByDefault
@SuppressWarnings("nls")
public class MathRenderer extends FlowStaticRenderer
{
	private final Math model;

	@AssistedInject
	public MathRenderer(@Assisted Math model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		if( !info.getBooleanAttribute(MathRenderer.class) )
		{
			info.addFooterStatements(new ScriptStatement("MathJax.Hub.Queue(['Typeset', MathJax.Hub]);"));
			info.setAttribute(MathRenderer.class, true);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends QtiNode> Iterator<T> getChildIterator()
	{
		return (Iterator<T>) model.getContent().iterator();
	}
}
