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

package com.tle.qti.render;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.tle.common.Check;
import com.tle.common.i18n.CurrentLocale;
import com.tle.qti.data.QTIAnswer;
import com.tle.qti.data.QTIItem;
import com.tle.qti.data.QTIMaterial;
import com.tle.qti.data.QTIResponse;
import com.tle.qti.data.QTIResponseElement;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.sections.standard.renderers.SpanRenderer;

public class QTIItemRenderer
{
	private static final String DESC = "description"; //$NON-NLS-1$
	private QTIItem item;

	protected PluginResourceHelper resources = ResourcesService.getResourceHelper(QTIItemRenderer.class);

	public QTIItemRenderer(QTIItem item)
	{
		this.item = item;
	}

	public SectionRenderable getTitle()
	{
		return new SimpleSectionResult(item.getTitle());
	}

	public SectionRenderable getQuestion()
	{
		QTIMaterial question = item.getQuestion();
		return new SimpleSectionResult(question.getHtml());

	}

	public SectionRenderable getResponses()
	{
		CombinedRenderer renderer = new CombinedRenderer();
		Map<String, QTIResponse> responses = item.getResponses();
		Collection<QTIResponse> responseValues = responses.values();
		boolean matching = item.getType().equalsIgnoreCase("Matching"); //$NON-NLS-1$

		if( matching && !responseValues.isEmpty() )
		{
			CombinedRenderer respRenderer = new CombinedRenderer();
			CombinedRenderer ansRenderer = null;
			for( QTIResponse response : responseValues )
			{
				respRenderer.addRenderer(new DivRenderer("li", "", new SimpleSectionResult(response //$NON-NLS-1$//$NON-NLS-2$
					.getDisplay().getHtml())));

				if( ansRenderer == null )
				{
					ansRenderer = new CombinedRenderer();
					Collection<QTIResponseElement> entries = response.getElements().values();
					for( QTIResponseElement value : entries )
					{
						ansRenderer.addRenderer(new DivRenderer("li", response.getType(), //$NON-NLS-1$
							new SimpleSectionResult(value.getDisplay().getHtml())));
					}
				}
			}

			renderer.addRenderer(new DivRenderer(DESC, new SimpleSectionResult(CurrentLocale.get(resources
				.key("matching.text"))))); //$NON-NLS-1$
			renderer.addRenderer(new DivRenderer("ol", "matching-left", respRenderer)); //$NON-NLS-1$ //$NON-NLS-2$ 
			renderer.addRenderer(new DivRenderer("ul", "matching-right standard", ansRenderer)); //$NON-NLS-1$ //$NON-NLS-2$ 
		}
		else
		{
			for( QTIResponse response : responseValues )
			{
				CombinedRenderer respRenderer = new CombinedRenderer();

				respRenderer.addRenderer(new DivRenderer("span", "response-text", //$NON-NLS-1$//$NON-NLS-2$
					new SimpleSectionResult(response.getDisplay().getHtml())));

				String key = "unknown.text"; //$NON-NLS-1$
				if( response.getType().equalsIgnoreCase("fib") ) //$NON-NLS-1$
				{
					key = "fib.text"; //$NON-NLS-1$
				}
				else if( response.getType().equalsIgnoreCase("choice") ) //$NON-NLS-1$
				{
					String cardinality = response.getCardinality().toLowerCase();
					if( cardinality.equals("multiple") ) //$NON-NLS-1$
					{
						key = "multiple.text"; //$NON-NLS-1$
					}
					else if( cardinality.equals("single") ) //$NON-NLS-1$
					{
						key = "single.text"; //$NON-NLS-1$
					}
				}

				renderer.addRenderer(new DivRenderer(DESC, new SimpleSectionResult(CurrentLocale.get(
					resources.key(key), response.getType()))));

				CombinedRenderer otherRender = new CombinedRenderer();
				Collection<QTIResponseElement> entries = response.getElements().values();
				for( QTIResponseElement value : entries )
				{
					String html = value.getDisplay().getHtml().trim();
					if( !Check.isEmpty(html) )
					{
						otherRender.addRenderer(new DivRenderer("li", response.getType(), //$NON-NLS-1$
							new SimpleSectionResult(html)));
					}
				}

				renderer.addRenderer(respRenderer);
				renderer.addRenderer(new DivRenderer("ol", response.getType(), otherRender)); //$NON-NLS-1$ 

			}
		}

		return renderer;

	}

	public SectionRenderable getGeneralFeedback()
	{
		CombinedRenderer renderer = new CombinedRenderer();

		List<QTIMaterial> generalFeedback = item.getGeneralFeedback();
		for( QTIMaterial qtiMaterial : generalFeedback )
		{
			renderer.addRenderer(new SimpleSectionResult(qtiMaterial.getHtml()));
		}

		return renderer;
	}

	@SuppressWarnings("nls")
	public SectionRenderable getAnswers()
	{
		CombinedRenderer list = new CombinedRenderer();
		List<QTIAnswer> answers = item.getAnswers();
		for( QTIAnswer qtiAnswer : answers )
		{
			CombinedRenderer itemRenderer = new CombinedRenderer();
			double score = qtiAnswer.getScore();
			if( qtiAnswer.getAction().equalsIgnoreCase("subtract") )
			{
				score *= -1;
			}
			String answer = qtiAnswer.getDisplay().getHtml();

			SpanRenderer span = new SpanRenderer(score > 0 ? "correct" : "incorrect", new SimpleSectionResult(answer
				+ " : " + score));
			itemRenderer.addRenderer(span);

			StringBuilder html = new StringBuilder();
			List<QTIMaterial> feedbackDisplay = qtiAnswer.getFeedback();
			if( feedbackDisplay.size() > 0 )
			{
				html.append(" - ");
				for( QTIMaterial qtiMaterial : feedbackDisplay )
				{
					html.append(qtiMaterial.getHtml());
				}
			}
			itemRenderer.addRenderer(new SpanRenderer("feedback", new SimpleSectionResult(html.toString())));
			list.addRenderer(new DivRenderer("li", "", itemRenderer));
		}

		return new DivRenderer("ol", "standard", list);
	}

	public static class SimpleHtmlOption<T> extends SimpleOption<T>
	{
		public SimpleHtmlOption(String name, String value)
		{
			super(name, value);
		}

		@Override
		public boolean isNameHtml()
		{
			return true;
		}
	}
}
