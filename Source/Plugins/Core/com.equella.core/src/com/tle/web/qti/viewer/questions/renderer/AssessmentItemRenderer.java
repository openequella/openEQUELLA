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
import java.util.List;

import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.ModalFeedback;
import uk.ac.ed.ph.jqtiplus.node.item.Stylesheet;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

import com.google.common.collect.Lists;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.common.Pair;
import com.tle.web.qti.viewer.QtiViewerContext;
import com.tle.web.qti.viewer.questions.renderer.unsupported.UnsupportedQuestionException;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class AssessmentItemRenderer extends QtiNodeRenderer
{
	private final DisplayModel dm = new DisplayModel();
	private final AssessmentItem model;

	private QtiNodeRenderer itemBodyRenderer;
	private List<QtiNodeRenderer> modalFeedbacks = Lists.newArrayList();

	@AssistedInject
	public AssessmentItemRenderer(@Assisted AssessmentItem model, @Assisted QtiViewerContext context)
	{
		super(model, context);
		this.model = model;
	}

	@Override
	public void preProcess()
	{
		final ItemBody itemBody = model.getItemBody();
		itemBodyRenderer = qfac.chooseRenderer(itemBody, getContext());

		// Modal feedback sits outside of itembody
		for( ModalFeedback fb : model.getModalFeedbacks() )
		{
			final QtiNodeRenderer modalFeedbackRenderer = qfac.chooseRenderer(fb, getContext());
			modalFeedbacks.add(modalFeedbackRenderer);
		}

		// See if any errors have trickled up
		final List<Pair<String, Identifier>> errors = getContext().getErrors();
		if( errors.size() > 0 )
		{
			final List<Pair<String, String>> displayErrors = Lists.newArrayList();
			for( Pair<String, Identifier> error : errors )
			{
				displayErrors.add(new Pair<String, String>(error.getFirst(), id(error.getSecond())));
			}
			dm.setErrors(displayErrors);
		}
	}

	@Override
	protected SectionRenderable createTopRenderable()
	{
		dm.setTitle(new LabelRenderer(new TextLabel(model.getTitle())));
		dm.setItemBody(itemBodyRenderer);
		dm.setModalFeedbacks(modalFeedbacks);
		return view.createResultWithModel("assessmentitem.ftl", dm);
	}

	@Override
	protected boolean isNestedTop()
	{
		return false;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		final List<Stylesheet> stylesheets = model.getStylesheets();
		if( stylesheets != null )
		{
			for( Stylesheet ss : stylesheets )
			{
				qfac.chooseRenderer(ss, getContext()).preRender(info);
			}
		}
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		try
		{
			super.realRender(writer);
		}
		catch( UnsupportedQuestionException unsupported )
		{
			final SectionRenderable renderable = view.createResultWithModel("unsupported.ftl", unsupported);
			renderable.realRender(writer);
		}
	}

	@Override
	protected SectionRenderable createNestedRenderable()
	{
		return null;
	}

	public static class DisplayModel
	{
		private SectionRenderable title;
		private SectionRenderable itemBody;
		private List<QtiNodeRenderer> modalFeedbacks;
		private List<Pair<String, String>> errors;

		public SectionRenderable getTitle()
		{
			return title;
		}

		public void setTitle(SectionRenderable title)
		{
			this.title = title;
		}

		public SectionRenderable getItemBody()
		{
			return itemBody;
		}

		public void setItemBody(SectionRenderable itemBody)
		{
			this.itemBody = itemBody;
		}

		public List<QtiNodeRenderer> getModalFeedbacks()
		{
			return modalFeedbacks;
		}

		public void setModalFeedbacks(List<QtiNodeRenderer> modalFeedbacks)
		{
			this.modalFeedbacks = modalFeedbacks;
		}

		public List<Pair<String, String>> getErrors()
		{
			return errors;
		}

		public void setErrors(List<Pair<String, String>> errors)
		{
			this.errors = errors;
		}
	}
}
