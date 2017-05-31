package com.tle.web.sections.standard.renderers;

import com.google.common.base.Strings;
import com.tle.common.i18n.CurrentLocale;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.Jq;
import com.tle.web.sections.jquery.libraries.JQueryProgression;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.expression.ElementByIdExpression;
import com.tle.web.sections.js.generic.expression.ElementIdExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.*;
import com.tle.web.sections.standard.model.HtmlFileDropState;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings("nls")
public class FileDropRenderer extends TagRenderer
{
	private static final PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(FileDropRenderer.class);

	private static final CssInclude CSS = CssInclude.include(urlHelper.url("css/filedrop.css")).make();
	private static final IncludeFile JS = new IncludeFile(urlHelper.url("js/filedrop.js"));

	private static final String KEY_DND = urlHelper.key("dnd.dropfiles");

	private static final JSCallable SETUP = new ExternallyDefinedFunction("setupFileDrop", JS,
		JQueryProgression.PRERENDER);

	protected final HtmlFileDropState dropState;

	public FileDropRenderer(HtmlFileDropState state)
	{
		super("div", state);
		this.dropState = state;
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException {
		super.prepareFirstAttributes(writer, attrs);
		addClass(attrs, "filedrop");
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException {
		writer.writeTag("div", "class", "dndicon");
		writer.endTag("div");
		writer.writeTag("p");
		writer.writeText(CurrentLocale.get(KEY_DND));
		writer.endTag("p");
		super.writeMiddle(writer);
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException {
		super.writeEnd(writer);

		writer.writeTag("input", "class", "filedrop-file", "type", "file", "id",
				getElementId(writer)+"_file", "multiple", "true", "tabIndex", "-1");
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.preRender(CSS);

		ObjectExpression oe = new ObjectExpression();
		Bookmark ajaxUploadUrl = dropState.getAjaxUploadUrl();
		if( ajaxUploadUrl != null )
		{
			oe.put("ajaxUploadUrl", ajaxUploadUrl.getHref());
			oe.put("validateFile", dropState.getValidateFile());
		}
		else
		{
			throw new SectionsRuntimeException("Must set an ajax upload url for filedrop");
		}
		oe.put("maxFiles", dropState.getMaxFiles());
		final JSStatements initDnd = Js.call_s(SETUP, oe, this);
		info.addReadyStatements(initDnd);
	}
}
