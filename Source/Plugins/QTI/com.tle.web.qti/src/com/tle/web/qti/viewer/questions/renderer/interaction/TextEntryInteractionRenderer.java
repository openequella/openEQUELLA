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

import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.NonNullByDefault;
import com.tle.web.qti.viewer.QtiViewerConstants;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.QtiNodeRenderer;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlTextFieldState;

@SuppressWarnings("nls")
@NonNullByDefault
public class TextEntryInteractionRenderer extends QtiNodeRenderer
{
	private static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(TextEntryInteractionRenderer.class);

	/**
	 * Standard sections components factory
	 */
	// @Inject
	// private RendererFactory renderFactory;

	private final HtmlTextFieldState state = new HtmlTextFieldState();
	private TextEntryInteraction model;

	@AssistedInject
	protected TextEntryInteractionRenderer(@Assisted TextEntryInteraction model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preProcess()
	{
		super.preProcess();
		final QtiViewerContext context = getContext();
		final ItemSessionController itemSessionController = context.getItemSessionController();
		final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
		final Identifier responseId = model.getResponseIdentifier();

		final Set<Identifier> unboundResponseIdentifiers = itemSessionState.getUnboundResponseIdentifiers();
		if( !unboundResponseIdentifiers.isEmpty() )
		{
			for( Identifier unboundResponseId : unboundResponseIdentifiers )
			{
				if( unboundResponseId.equals(responseId) )
				{
					context.addError(resources.getString("viewer.error.unboundresponse"), unboundResponseId);
					state.addClass("unboundresponse");
				}
			}
		}

		final Set<Identifier> invalidResponseIdentifiers = itemSessionState.getInvalidResponseIdentifiers();
		if( !invalidResponseIdentifiers.isEmpty() )
		{
			for( Identifier invalidResponseId : unboundResponseIdentifiers )
			{
				if( invalidResponseId.equals(responseId) )
				{
					context.addError(resources.getString("viewer.error.invalidresponse"), invalidResponseId);
					state.addClass("invalidresponse");
				}
			}
		}
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final QtiViewerContext context = getContext();

		final Identifier responseId = model.getResponseIdentifier();
		state.setName(QtiViewerConstants.CONTROL_PREFIX + id(responseId));

		final List<String> values = context.getValues(responseId);
		if( values != null && values.size() > 0 )
		{
			state.setValue(values.get(0));
		}
		state.setSize(model.getExpectedLength());

		final ItemSessionController itemSessionController = context.getItemSessionController();
		final ItemSessionState itemSessionState = itemSessionController.getItemSessionState();
		if( itemSessionState.isEnded() )
		{
			state.setDisabled(true);
		}

		final DisplayModel dm = new DisplayModel();
		dm.setTextEntry(state);
		return view.createResultWithModel("textinteraction.ftl", dm);
	}

	@NonNullByDefault(false)
	public static class DisplayModel
	{
		private HtmlTextFieldState textEntry;

		public HtmlTextFieldState getTextEntry()
		{
			return textEntry;
		}

		public void setTextEntry(HtmlTextFieldState textEntry)
		{
			this.textEntry = textEntry;
		}
	}
}
