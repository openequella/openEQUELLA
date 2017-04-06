package com.tle.web.sections.equella.render;

import java.io.IOException;
import java.util.Map;

import javax.inject.Singleton;

import com.tle.common.i18n.CurrentLocale;
import com.tle.core.guice.Bind;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryExtension;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlFileUploadState;
import com.tle.web.sections.standard.renderers.FileRenderer;

/**
 * plugin defines the stateClassName as HtmlFileUploadState
 */
@Bind
@Singleton
@SuppressWarnings("nls")
public class EquellaFileUploadExtension implements RendererFactoryExtension
{
	private static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(EquellaFileUploadExtension.class);

	private static final CssInclude CSS = CssInclude.include(RESOURCES.url("css/render/jquery.fileinput.css")).hasRtl()
		.make();
	private static final IncludeFile JS = new IncludeFile(RESOURCES.url("scripts/render/jquery.fileinput.js"));

	private static final ExternallyDefinedFunction INIT = new ExternallyDefinedFunction("customFileInput", JS);

	private static final String BROWSE_KEY = RESOURCES.key("equellafileupload.browse");
	private static final String CHANGE_KEY = RESOURCES.key("equellafileupload.change");
	private static final String NONE_SELECTED_KEY = RESOURCES.key("equellafileupload.noneselected");

	@Override
	public SectionRenderable getRenderer(RendererFactory rendererFactory, SectionInfo info, String renderer,
		HtmlComponentState state)
	{
		return new FancyFileRenderer((HtmlFileUploadState) state); // NOSONAR
	}

	protected static class FancyFileRenderer extends FileRenderer
	{
		protected FancyFileRenderer(HtmlFileUploadState state)
		{
			super(state);
		}

		@Override
		protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
		{
			super.prepareFirstAttributes(writer, attrs);
		}

		@Override
		public void preRender(PreRenderContext info)
		{
			super.preRender(info);

			if( renderFile )
			{
				info.preRender(CSS);
				ObjectExpression oe = new ObjectExpression();
				oe.put("browseText", CurrentLocale.get(BROWSE_KEY));
				oe.put("changeText", CurrentLocale.get(CHANGE_KEY));
				oe.put("noneSelectedText", CurrentLocale.get(NONE_SELECTED_KEY));
				oe.put("extraButtonClasses", EquellaButtonExtension.CLASS_BUTTON);

				Bookmark ajaxUploadUrl = uploadState.getAjaxUploadUrl();
				if( ajaxUploadUrl != null )
				{
					oe.put("ajaxUploadUrl", ajaxUploadUrl.getHref());
					oe.put("ajaxUploadForm", Jq.$(info.getHelper().getFormExpression()));
					oe.put("ajaxBeforeUpload", uploadState.getAjaxBeforeUpload());
					oe.put("ajaxAfterUpload", uploadState.getAjaxAfterUpload());
					oe.put("uploadId", uploadState.getUploadId());
					oe.put("maxFilesize", uploadState.getMaxFilesize());
					oe.put("errorCallback", uploadState.getErrorCallback());
				}

				info.addReadyStatements(Js.statement(Jq.methodCall(this, INIT, oe)));
			}
		}
	}
}
