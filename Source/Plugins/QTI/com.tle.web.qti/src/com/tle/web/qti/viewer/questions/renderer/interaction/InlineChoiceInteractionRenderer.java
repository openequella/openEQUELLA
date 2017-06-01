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

import javax.inject.Inject;

import uk.ac.ed.ph.jqtiplus.node.content.variable.TextOrVariable;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.common.NameValue;
import com.tle.web.qti.viewer.QtiViewerConstants;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.NameValueOption;
import com.tle.web.sections.standard.model.Option;

@SuppressWarnings("nls")
@NonNullByDefault
public class InlineChoiceInteractionRenderer extends QtiNodeRenderer
{
	@PlugKey("viewer.question.inlinechoice.novalue")
	private static Label LABEL_NO_RESPONSE;
	static
	{
		PluginResourceHandler.init(InlineChoiceInteractionRenderer.class);
	}

	private final InlineChoiceInteraction model;

	/**
	 * Standard sections components factory
	 */
	@Inject
	private RendererFactory renderFactory;

	@AssistedInject
	protected InlineChoiceInteractionRenderer(@Assisted InlineChoiceInteraction model,
		@Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final QtiViewerContext context = getContext();

		final List<Option<?>> options = Lists.newArrayList();
		// blank top option
		final String noSelectionText = LABEL_NO_RESPONSE.getText();
		options.add(new NameValueOption<InlineChoice>(new NameValue(noSelectionText, ""), null));
		int maxTextLength = noSelectionText.length();
		for( InlineChoice choice : model.getInlineChoices() )
		{
			final List<TextOrVariable> children = choice.getChildren();
			SectionRenderable childRenderable = null;
			for( TextOrVariable textNode : children )
			{
				childRenderable = CombinedRenderer.combineMultipleResults(childRenderable,
					qfac.chooseRenderer(textNode, context));
			}

			final String label = renderToText(childRenderable);
			if( label.length() > maxTextLength )
			{
				maxTextLength = label.length();
			}
			options.add(new NameValueOption<InlineChoice>(new NameValue(label, id(choice.getIdentifier())), choice));
		}

		final Identifier responseId = model.getResponseIdentifier();
		final HtmlListState list = new HtmlListState();
		list.addClass("inlineChoice");
		list.setName(QtiViewerConstants.CONTROL_PREFIX + id(responseId));
		list.setOptions(options);
		list.setStyle("width:" + (maxTextLength * 0.7) + "em");
		final List<String> responseValues = context.getValues(responseId);
		if( responseValues != null )
		{
			list.setSelectedValues(Sets.newHashSet(responseValues));
		}
		else
		{
			list.setSelectedValues(Sets.<String> newHashSet());
		}

		final ItemSessionController itemSessionController = context.getItemSessionController();
		final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
		if( itemSessionState.isEnded() )
		{
			list.setDisabled(true);
		}
		else
		{
			list.setEventHandler(JSHandler.EVENT_CHANGE, new StatementHandler(context.getValueChangedFunction()));
		}

		return renderFactory.getRenderer(context.getRenderContext(), list);
	}

	@Override
	protected boolean isNestedTop()
	{
		return false;
	}

	@Nullable
	@Override
	protected SectionRenderable createNestedRenderable()
	{
		return null;
	}

	@Override
	protected String getTagName()
	{
		return "select";
	}
}
