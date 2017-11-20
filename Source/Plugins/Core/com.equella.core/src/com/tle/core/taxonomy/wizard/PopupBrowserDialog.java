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

package com.tle.core.taxonomy.wizard;

import java.util.regex.Pattern;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermResult;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.ButtonRenderer;
import com.tle.web.sections.equella.render.ButtonRenderer.ButtonType;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.expression.ScriptVariable;
import com.tle.web.sections.js.generic.function.SimpleFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@NonNullByDefault
@Bind
@SuppressWarnings("nls")
public class PopupBrowserDialog extends AbstractPopupBrowserDialog<PopupBrowserDialog.PopupBrowserModel>
{
	private static final LabelRenderer SPACER = new LabelRenderer(new TextLabel("&nbsp;&nbsp;&nbsp;", true));

	@PlugKey("wizard.popupbrowser.selectthisterm")
	private static Label SELECT_VIEWED_TERM_LABEL;
	@PlugKey("wizard.popupbrowser.selectterm")
	private static Label SELECT_TERM_LABEL;
	@PlugKey("wizard.popupbrowser.viewterm")
	private static Label VIEW_TERM_LABEL;

	@Inject
	private TaxonomyService taxonomyService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	private SimpleFunction showTermFunc;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		// The following function is used internally by the tree and search
		// results, but is also a public API for people to invoke:
		// showTerm('some\full\path')
		ScriptVariable termVar = new ScriptVariable("term");
		showTermFunc = new SimpleFunction("showTerm", new FunctionCallStatement(ajaxEvents.getAjaxUpdateDomFunction(
			tree, this, events.getEventHandler("showTerm"), ajaxEvents.getEffectFunction(EffectType.FADEOUTIN),
			"termViewer"), termVar), termVar);
	}

	@Override
	protected String getContentBodyClass(RenderContext context)
	{
		return "popupbrowserdialog";
	}

	@Override
	protected SectionRenderable getRenderableContents(RenderContext context)
	{
		return viewFactory.createResult("popupbrowser/popup.ftl", this);
	}

	@EventHandlerMethod
	public void showTerm(SectionInfo info, String termFullPath)
	{
		final PopupBrowserModel model = getModel(info);
		final TermResult term = taxonomyService.getTerm(taxonomyUuid, termFullPath);

		model.setShownTerm(term.getTerm());
		model.setShownFullTerm(term.getFullTerm().replaceFirst("\\\\?" + Pattern.quote(term.getTerm()), ""));
		model.setShownHtmlData(taxonomyService.getDataForTerm(taxonomyUuid, termFullPath, "LONG_DATA"));

		if( isSelectable(term) )
		{
			HtmlComponentState select = new HtmlComponentState(new OverrideHandler(selectTermFunc, termFullPath));
			select.setLabel(SELECT_VIEWED_TERM_LABEL);
			model.setSelectShownTerm(new ButtonRenderer(select).showAs(ButtonType.SELECT));
		}
	}

	@Override
	protected SectionRenderable getTermClickTarget(TermResult tr)
	{
		SectionRenderable termLabel = SectionUtils.convertToRenderer(tr.getTerm());

		HtmlLinkState view = new HtmlLinkState(new OverrideHandler(showTermFunc, tr.getFullTerm()));
		view.setLabel(VIEW_TERM_LABEL);
		view.addClass("viewterm");

		if( !isSelectable(tr) )
		{
			return new CombinedRenderer(termLabel, SPACER, new LinkRenderer(view));
		}
		else
		{
			HtmlComponentState select = new HtmlComponentState(new OverrideHandler(selectTermFunc, tr.getFullTerm()));
			select.setLabel(SELECT_TERM_LABEL);
			select.addClass("add");

			return new CombinedRenderer(termLabel, SPACER, new LinkRenderer(select), SPACER, new LinkRenderer(view));
		}
	}

	public SimpleFunction getShowTermFunc()
	{
		return showTermFunc;
	}

	@Override
	public PopupBrowserModel instantiateDialogModel(SectionInfo info)
	{
		return new PopupBrowserModel();
	}

	public static class PopupBrowserModel extends AbstractPopupBrowserDialog.AbstractPopupBrowserModel
	{
		private String shownTerm;
		private String shownFullTerm;
		private String shownHtmlData;
		private SectionRenderable selectShownTerm;

		public String getShownTerm()
		{
			return shownTerm;
		}

		public void setShownTerm(String shownTerm)
		{
			this.shownTerm = shownTerm;
		}

		public String getShownFullTerm()
		{
			return shownFullTerm;
		}

		public void setShownFullTerm(String shownFullTerm)
		{
			this.shownFullTerm = shownFullTerm;
		}

		public String getShownHtmlData()
		{
			return shownHtmlData;
		}

		public void setShownHtmlData(String shownHtmlData)
		{
			this.shownHtmlData = shownHtmlData;
		}

		public SectionRenderable getSelectShownTerm()
		{
			return selectShownTerm;
		}

		public void setSelectShownTerm(SectionRenderable selectShownTerm)
		{
			this.selectShownTerm = selectShownTerm;
		}
	}
}
