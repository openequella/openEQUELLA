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
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import uk.ac.ed.ph.jqtiplus.attribute.Attribute;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.shared.VariableType;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.value.Value;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.core.guice.Bind;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.dialog.EquellaDialog;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.renderers.DivRenderer;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class ModalFeedbackRenderer extends QtiNodeRenderer
{
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

	@PlugKey("viewer.modalfeedback.defaulttitle")
	private static Label LABEL_MODAL_FEEDBACK;

	static
	{
		PluginResourceHandler.init(ModalFeedbackRenderer.class);
	}

	private final ModalFeedback model;

	// @Inject
	// private FeedbackDialog dialog;
	// @Inject
	// private RendererFactory factory;
	// @Inject
	// private RegistrationController registrationController;

	@AssistedInject
	protected ModalFeedbackRenderer(@Assisted ModalFeedback model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@PostConstruct
	public void init()
	{
		// TODO: use proper dialog, but requires rebuilding the
		// whole tree on
		// the POST request
		// dialog.setModalFeedbackRenderer(this);
		//
		// final DefaultSectionTree sectionTree = new
		// DefaultSectionTree(registrationController, new SectionNode("mfr"
		// + id(model.getIdentifier()) + "_"));
		// final MutableSectionInfo msi =
		// getContext().getRenderContext().getAttributeForClass(MutableSectionInfo.class);
		// sectionTree.registerSections(dialog, null);
		// sectionTree.treeFinished();
		// msi.addTreeToBottom(sectionTree, true);
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		final QtiViewerContext context = getContext();
		final ItemSessionState itemSessionState = context.getItemSessionController().getItemSessionState();
		// TODO: does it need to be closed???
		if( itemSessionState.isEnded() )
		{
			final ItemSessionController itemSessionController = context.getItemSessionController();

			// TODO: probably a cleaner way to see if there was a response
			final Value outcomeValue = itemSessionController.evaluateVariableValue(model.getOutcomeIdentifier(),
				VariableType.OUTCOME);
			if( !outcomeValue.isNull() )
			{
				final boolean vis = model.isVisible(itemSessionController);
				if( vis )
				{
					// TODO: use proper dialog, but requires rebuilding the
					// whole tree on
					// the POST request
					// final Link button = dialog.getOpener();
					// return
					// ChooseRenderer.getSectionRenderable(getContext().getRenderContext(),
					// button,
					// button.getDefaultRenderer(), factory);

					HtmlComponentState state = new HtmlComponentState();
					state.setLabel(getTitle());
					SectionRenderable nested = super.createNestedRenderable();

					String caption = renderToText(nested);
					// Normalise the space for the alert
					caption = WHITESPACE_PATTERN.matcher(caption).replaceAll(" ").trim();

					state.setClickHandler(Js.handler(Js.alert(caption)));
					return new ButtonRenderer(state);
				}
			}
		}
		return null;
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

	protected Label getTitle()
	{
		final Attribute<?> title = model.getAttributes().get(ModalFeedback.ATTR_TITLE_NAME);
		if( title.isSet() )
		{
			return new TextLabel(title.valueToQtiString());
		}
		return LABEL_MODAL_FEEDBACK;
	}

	@Bind
	public static class FeedbackDialog extends EquellaDialog<FeedbackDialog.FeedbackDialogModel>
	{
		private ModalFeedbackRenderer modalFeedbackRenderer;

		public FeedbackDialog()
		{
			setAjax(false);
		}

		@Override
		protected SectionRenderable getRenderableContents(RenderContext context)
		{
			SectionRenderable children = null;
			final Iterator<QtiNode> childIterator = modalFeedbackRenderer.getChildIterator();
			while( childIterator.hasNext() )
			{
				final QtiNode node = childIterator.next();
				children = CombinedRenderer.combineResults(children,
					modalFeedbackRenderer.qfac.chooseRenderer(node, modalFeedbackRenderer.getContext()));
			}

			return new DivRenderer("modalFeedback", children);
		}

		@Override
		public void registered(String id, SectionTree tree)
		{
			super.registered(id, tree);
			final Link button = getOpener();
			button.setLabel(modalFeedbackRenderer.getTitle());
			button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		}

		@Override
		public String getWidth()
		{
			return "640px";
		}

		@Override
		public String getHeight()
		{
			return "480px";
		}

		@Override
		protected Label getTitleLabel(RenderContext context)
		{
			return modalFeedbackRenderer.getTitle();
		}

		@Override
		public FeedbackDialogModel instantiateDialogModel(SectionInfo info)
		{
			return new FeedbackDialogModel();
		}

		public void setModalFeedbackRenderer(ModalFeedbackRenderer modalFeedbackRenderer)
		{
			this.modalFeedbackRenderer = modalFeedbackRenderer;
		}

		public static class FeedbackDialogModel extends DialogModel
		{
			// Nada
		}
	}
}
