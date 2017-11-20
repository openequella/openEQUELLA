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

package com.tle.web.qti.viewer.questions.renderer.base;

import java.util.Map;

import javax.inject.Inject;

import uk.ac.ed.ph.jqtiplus.node.content.variable.FeedbackElement;
import uk.ac.ed.ph.jqtiplus.node.shared.VariableType;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.value.Value;

import com.tle.core.qti.service.QtiService;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.sections.render.SectionRenderable;

/**
 * @author Aaron
 */
public abstract class FeedbackElementRenderer extends QtiNodeRenderer
{
	@Inject
	private QtiService qtiService;

	private final FeedbackElement model;

	protected FeedbackElementRenderer(FeedbackElement model, QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final QtiViewerContext context = getContext();
		final ItemSessionState itemSessionState = context.getItemSessionController().getItemSessionState();

		// TODO: does it need to be closed??? What about multiple attempts?
		if( itemSessionState.isEnded() )
		{
			final ItemSessionController itemSessionController = context.getItemSessionController();
			final ResolvedAssessmentItem resolvedAssessmentItem = context.getItemSessionController()
				.getResolvedAssessmentItem();
			if( qtiService.isResponded(resolvedAssessmentItem.getItemLookup().extractIfSuccessful(), itemSessionState) )
			{

				// TODO: probably a cleaner way to see if there was a response
				final Value outcomeValue = itemSessionController.evaluateVariableValue(model.getOutcomeIdentifier(),
					VariableType.OUTCOME);
				if( !outcomeValue.isNull() )
				{
					final boolean vis = model.isVisible(itemSessionController);
					if( vis )
					{
						return super.createTopRenderable();
					}
				}
			}
		}
		return null;
	}

	@Override
	protected void addAttributes(Map<String, String> attrs)
	{
		super.addAttributes(attrs);
		attrs.put("class", "feedback");
	}
}
