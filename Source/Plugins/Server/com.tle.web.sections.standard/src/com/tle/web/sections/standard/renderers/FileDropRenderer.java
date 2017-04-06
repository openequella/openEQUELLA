package com.tle.web.sections.standard.renderers;

import com.google.common.base.Strings;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.CssInclude;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagRenderer;
import com.tle.web.sections.standard.model.HtmlFileDropState;

@SuppressWarnings("nls")
public class FileDropRenderer extends TagRenderer
{
	private static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(FileDropRenderer.class);

	private static final CssInclude CSS = CssInclude.include(urlHelper.url("css/filedrop.css")).make();
	private static final IncludeFile JS = new IncludeFile(urlHelper.url("js/dnd.js"));
	private static final IncludeFile DND_JS = new IncludeFile(urlHelper.url("js/jquery.filedrop.js"));

	private static final JSCallable SETUP = new ExternallyDefinedFunction("register_drag_and_drop", JS, DND_JS,
		JQueryProgression.PRERENDER);

	protected final HtmlFileDropState dropState;

	public FileDropRenderer(HtmlFileDropState state)
	{
		super("div", state);
		this.dropState = state;
	}

	@Override
	public SectionRenderable getNestedRenderable()
	{
		if( nestedRenderable != null )
		{
			return nestedRenderable;
		}

		if( dropState.getLabel() != null )
		{
			nestedRenderable = dropState.createLabelRenderer();
		}

		return nestedRenderable;
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.getForm().setEncoding("multipart/form-data");

		info.preRender(CSS);

		final ObjectExpression oe = new ObjectExpression();
		oe.put("maxFiles", dropState.getMaxFiles());
		oe.put("ajaxUploadForm", Jq.$(info.getHelper().getFormExpression()));
		oe.put("ajaxBeforeUpload", dropState.getAjaxBeforeUpload());
		oe.put("ajaxAfterUpload", dropState.getAjaxAfterUpload());
		oe.put("uploadFinished", dropState.getUploadFinishedCallback());
		oe.put("ajaxMethod", dropState.getAjaxMethod());
		oe.put("fallbackFormId", dropState.getFallbackFormId());
		oe.put("fallbackHiddenIds", dropState.getFallbackHiddenIds());
		oe.put("uploadId", dropState.getUploadId());
		oe.put("banned", dropState.getBanned());
		oe.put("allowedMimes", dropState.getAllowedMimetypes());
		oe.put("mimeErrorMessage", dropState.getMimetypeErrorMessage());
		oe.put("maxFilesizeErrorMessage", dropState.getMaxFilesizeErrorMessage());
		oe.put("maxFilesize", dropState.getMaxFilesize());

		if( !Strings.isNullOrEmpty(dropState.getProgressAreaId()) )
		{
			oe.put("progressAreaId", dropState.getProgressAreaId());
		}

		final JSStatements initDnd = Js.call_s(SETUP, oe);
		info.addReadyStatements(initDnd);
	}
}
