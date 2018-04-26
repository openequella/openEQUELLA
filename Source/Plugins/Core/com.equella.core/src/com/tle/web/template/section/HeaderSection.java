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

package com.tle.web.template.section;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.StandardRenderContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.js.generic.statement.StatementBlock;
import com.tle.web.sections.render.AppendedLabel;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TemplateRenderable;
import com.tle.web.sections.render.TemplateResult;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.template.Decorations;
import com.tle.web.template.RenderNewTemplate;

@SuppressWarnings("nls")
@Bind
public class HeaderSection extends AbstractPrototypeSection<HeaderSection.HeaderModel> implements HtmlRenderer
{
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@PlugKey("windowtitlepostfix")
	public static Label LABEL_WINDOWTITLE;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return new RenderHeader();
	}

	public class RenderHeader implements TemplateResult
	{
		private TemplateResult headerTemplate;

		private TemplateResult getTemplateResult(RenderContext context)
		{
			if( headerTemplate == null )
			{
				HeaderModel model = getModel(context);
				StandardRenderContext standard = (StandardRenderContext) context.getPreRenderContext();
				setupHeaderScripts(context, model, standard);
				setupPostMarkupScripts(context, model, standard);

				List<CssInclude> cssFiles = new ArrayList<CssInclude>();
				cssFiles.addAll(standard.getCssFiles());
				Collections.sort(cssFiles);
				model.setStylesheets(cssFiles);
				model.setIncludeRtlStyles(CurrentLocale.isRightToLeft());
				model.setNewLayout(RenderNewTemplate.isNewLayout(context));

				setupTitle(context, model);
				model.setHead(standard.getHeaderMarkup());
				headerTemplate = viewFactory.createTemplateResultWithModel("header.ftl", model);
			}
			return headerTemplate;
		}

		@Override
		public TemplateRenderable getNamedResult(RenderContext info, String name)
		{
			if( name.equals("header") || name.equals("postmarkup") )
			{
				return getTemplateResult(info).getNamedResult(info, name);
			}
			return null;
		}
	}

	private void setupTitle(RenderContext context, HeaderModel model)
	{
		Decorations decorations = Decorations.getDecorations(context);
		model
			.setTitle(new LabelRenderer(AppendedLabel.get(decorations.getTitle(), LABEL_WINDOWTITLE, TextLabel.SPACE)));
	}

	private void setupPostMarkupScripts(RenderContext context, HeaderModel model, StandardRenderContext helper)
	{
		helper.addStatements(StatementBlock.get(helper.dequeueFooterStatements()));
		List<JSStatements> ready = helper.dequeueReadyStatements();
		if( !ready.isEmpty() )
		{
			helper.addStatements(new FunctionCallStatement(JQueryCore.JQUERY, new AnonymousFunction(new StatementBlock(
				ready).setSeperate(true))));
		}
		assert helper.dequeueReadyStatements().isEmpty();
		for( JSStatements postMarkupStatment : preRenderPageScripts(context, helper) )
		{
			model.addPostMarkupScript(postMarkupStatment.getStatements(context));
		}
		List<String> jsFileList = helper.getJsFiles();
		model.setExternalPostScripts(jsFileList.subList(model.getExternalScripts().size(), jsFileList.size()));
	}

	private void setupHeaderScripts(RenderContext context, HeaderModel model, StandardRenderContext helper)
	{
		for( JSStatements headerStatement : preRenderPageScripts(context, helper) )
		{
			model.addHeaderScript(headerStatement.getStatements(context));
		}
		model.setExternalScripts(helper.getJsFiles());
	}

	private List<JSStatements> preRenderPageScripts(RenderContext context, StandardRenderContext helper)
	{
		LinkedList<JSStatements> renderedStatements = new LinkedList<JSStatements>();
		int iterations = 0;
		List<JSStatements> origStatements = helper.dequeueStatements();
		while( !origStatements.isEmpty() )
		{
			List<JSStatements> statements = new ArrayList<JSStatements>(origStatements);
			renderedStatements.addAll(0, statements);
			context.preRender(statements);
			origStatements = helper.dequeueStatements();
			if( ++iterations > 10 )
			{
				throw new SectionsRuntimeException("10 looks like infinity");
			}
		}
		return renderedStatements;
	}

	public static class HeaderModel
	{
		private List<CssInclude> stylesheets;
		private boolean includeRtlStyles;
		private boolean newLayout;
		private final StringBuilder headerScript = new StringBuilder();
		private final StringBuilder postMarkupScript = new StringBuilder();
		private Collection<String> externalScripts;
		private Collection<String> externalPostScripts;
		private String head;
		private SectionRenderable title;

		public String getHead()
		{
			return head;
		}

		public void setHead(String head)
		{
			this.head = head;
		}

		public SectionRenderable getTitle()
		{
			return title;
		}

		public void setTitle(SectionRenderable title)
		{
			this.title = title;
		}

		public void addHeaderScript(String script)
		{
			headerScript.append(script);
		}

		public void addPostMarkupScript(String script)
		{
			postMarkupScript.append(script);
		}

		public void setStylesheets(List<CssInclude> stylesheets)
		{
			this.stylesheets = stylesheets;
		}

		public List<CssInclude> getStylesheets()
		{
			return stylesheets;
		}

		public Collection<String> getExternalScripts()
		{
			return externalScripts;
		}

		public String getHeaderScript()
		{
			return headerScript.toString();
		}

		public StringBuilder getPostMarkupScript()
		{
			return postMarkupScript;
		}

		public Collection<String> getExternalPostScripts()
		{
			return externalPostScripts;
		}

		public void setExternalPostScripts(Collection<String> externalPostScripts)
		{
			this.externalPostScripts = externalPostScripts;
		}

		public void setExternalScripts(Collection<String> externalScripts)
		{
			this.externalScripts = externalScripts;
		}

		public boolean isIncludeRtlStyles()
		{
			return includeRtlStyles;
		}

		public void setIncludeRtlStyles(boolean includeRtlStyles)
		{
			this.includeRtlStyles = includeRtlStyles;
		}

		public boolean isNewLayout()
		{
			return newLayout;
		}

		public void setNewLayout(boolean newLayout)
		{
			this.newLayout = newLayout;
		}
	}

	@Override
	public Class<HeaderModel> getModelClass()
	{
		return HeaderModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "head";
	}
}
