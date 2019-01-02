/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.equella.layout;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.equella.receipt.ReceiptService;
import com.tle.web.sections.equella.render.Bootstrap;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CombinedTemplateResult;
import com.tle.web.sections.render.FallbackTemplateResult;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TemplateResultCollector;
import com.tle.web.template.Breadcrumbs;
import com.tle.web.template.Decorations;
import com.tle.web.template.ReactPageModel;
import com.tle.web.template.RenderNewTemplate;
import com.tle.web.template.section.event.BlueBarEvent;
import sun.font.Decoration;

@TreeIndexed
@NonNullByDefault
@SuppressWarnings("nls")
public abstract class OneColumnLayout<M extends OneColumnLayout.OneColumnLayoutModel>
	extends
		AbstractPrototypeSection<M> implements HtmlRenderer
{
	public static final String BODY = "body";
	public static final String UPPERBODY = "upperbody";

	@ViewFactory
	private FreemarkerFactory layoutFactory;
	@ViewFactory(fixed = false, optional = true)
	protected FreemarkerFactory viewFactory;

	@Inject
	private ReceiptService receiptService;

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final M model = getModel(context);

		TemplateResult template = setupTemplate(context);
		if( template == null )
		{
			return null;
		}
		model.setTemplate(template);

		Label receipt = receiptService.getReceipt();
		if( receipt != null )
		{
			model.setReceipt(receipt.getText());
		}

		context.preRender(Bootstrap.PRERENDER);

		Decorations decs = Decorations.getDecorations(context);
		addBreadcrumbsAndTitle(context, decs, Breadcrumbs.get(context));

		GenericTemplateResult temp = new GenericTemplateResult();
		temp.addNamedResult(BODY, layoutFactory.createResultWithModel(getLayout(context).getFtl(), model));
		return new FallbackTemplateResult(temp, model.getTemplate());
	}

	@Override
	public Class<M> getModelClass()
	{
		return (Class<M>) OneColumnLayoutModel.class;
	}

	private ContentLayout getLayout(SectionInfo info)
	{
		ContentLayout layout = ContentLayout.getLayout(info);
		if( layout == null )
		{
			return getDefaultLayout(info);
		}
		return layout;
	}

	protected ContentLayout getDefaultLayout(SectionInfo info)
	{
		return ContentLayout.ONE_COLUMN;
	}

	protected TemplateResult setupTemplate(RenderEventContext context)
	{
		return getTemplateResult(context);
	}

	@Nullable
	protected TemplateResult getTemplateResult(RenderEventContext context)
	{
		final TemplateResultCollector collector = new TemplateResultCollector();
		final M model = getModel(context);
		final SectionId modalSection = model.getModalSection();
		if( modalSection != null )
		{
			SectionUtils.renderSection(context, modalSection, collector);
		}
		else
		{
			renderChildren(context, this, collector);
		}

		final CombinedTemplateResult templateResult = collector.getTemplateResult();
		final BlueBarEvent blueBarEvent = new BlueBarEvent(context);
		context.processEvent(blueBarEvent);
		return templateResult;
	}

	protected abstract void addBreadcrumbsAndTitle(SectionInfo info, Decorations decorations, Breadcrumbs crumbs);

	public void setModalSection(SectionInfo info, SectionId sectionId)
	{
		getModel(info).setModalSection(sectionId);
	}

	@NonNullByDefault(false)
	public static class OneColumnLayoutModel
	{
		private TemplateResult template;
		private SectionId modalSection;
		private String receipt;

		public SectionId getModalSection()
		{
			return modalSection;
		}

		public void setModalSection(SectionId modalSection)
		{
			this.modalSection = modalSection;
		}

		public TemplateResult getTemplate()
		{
			return template;
		}

		public void setTemplate(TemplateResult template)
		{
			this.template = template;
		}

		public String getReceipt()
		{
			return receipt;
		}

		public void setReceipt(String receipt)
		{
			this.receipt = receipt;
		}
	}
}
