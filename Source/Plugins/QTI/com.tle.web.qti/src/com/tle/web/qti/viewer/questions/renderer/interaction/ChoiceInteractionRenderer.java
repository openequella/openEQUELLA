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

package com.tle.web.qti.viewer.questions.renderer.interaction;

import java.util.List;
import java.util.Set;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Prompt;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerConstants;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.qti.viewer.questions.renderer.SimpleChoiceRenderer;
import com.tle.web.qti.viewer.questions.renderer.base.BlockInteractionRenderer;
import com.tle.web.sections.render.SectionRenderable;

@NonNullByDefault
public class ChoiceInteractionRenderer extends BlockInteractionRenderer
{
	private final DisplayModel dm = new DisplayModel();
	private final ChoiceInteraction model;

	@AssistedInject
	public ChoiceInteractionRenderer(@Assisted ChoiceInteraction model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preProcess()
	{
		// super.preProcess();

		final List<SimpleChoiceRenderer> choiceRenderables = Lists.newArrayList();
		final int maxChoices = model.getMaxChoices();
		final Identifier responseId = model.getResponseIdentifier();
		final List<String> selectedValues = getContext().getValues(responseId);
		final Set<String> selectedValuesSet;
		if( selectedValues != null )
		{
			selectedValuesSet = Sets.newHashSet(selectedValues);
		}
		else
		{
			selectedValuesSet = Sets.newHashSet();
		}
		for( SimpleChoice choice : model.getSimpleChoices() )
		{
			final SimpleChoiceRenderer simpleChoiceRenderer = qfac.getRendererFactory().createSimpleChoiceRenderer(
				choice, getContext(), QtiViewerConstants.CONTROL_PREFIX + id(responseId),
				maxChoices > 1 || maxChoices == 0);
			final String value = id(choice.getIdentifier());
			if( value != null && selectedValuesSet.contains(value) )
			{
				simpleChoiceRenderer.setChecked(true);
			}
			simpleChoiceRenderer.preProcess();
			choiceRenderables.add(simpleChoiceRenderer);
		}
		dm.setChoices(choiceRenderables);

		final Prompt prompt = model.getPrompt();
		if( prompt != null )
		{
			final QtiNodeRenderer promptRenderer = qfac.chooseRenderer(prompt, getContext());
			dm.setPrompt(promptRenderer);
		}
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		return view.createResultWithModel("choiceinteraction.ftl", dm);
	}

	@Override
	protected boolean isNestedTop()
	{
		return false;
	}

	@Override
	protected SectionRenderable createNestedRenderable()
	{
		return null;
	}

	@NonNullByDefault(false)
	public static class DisplayModel
	{
		private SectionRenderable prompt;
		private List<SimpleChoiceRenderer> choices;

		public SectionRenderable getPrompt()
		{
			return prompt;
		}

		public void setPrompt(SectionRenderable prompt)
		{
			this.prompt = prompt;
		}

		public List<SimpleChoiceRenderer> getChoices()
		{
			return choices;
		}

		public void setChoices(List<SimpleChoiceRenderer> choices)
		{
			this.choices = choices;
		}
	}
}
