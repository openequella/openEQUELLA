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

import uk.ac.ed.ph.jqtiplus.node.ForeignElement;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public class ForeignElementRenderer extends QtiNodeRenderer
{
	private final ForeignElement model;

	@AssistedInject
	public ForeignElementRenderer(@Assisted ForeignElement model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Nullable
	@Override
	protected SectionRenderable createNestedRenderable()
	{
		SectionRenderable children = null;
		// Iterator doesn't work on ForeignElements, need to call getChildren
		for( QtiNode child : model.getChildren() )
		{
			children = CombinedRenderer.combineResults(children, qfac.chooseRenderer(child, getContext()));
		}
		return children;
	}
}
