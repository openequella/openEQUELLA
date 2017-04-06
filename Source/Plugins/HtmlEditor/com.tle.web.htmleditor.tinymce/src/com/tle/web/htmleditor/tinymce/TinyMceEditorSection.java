package com.tle.web.htmleditor.tinymce;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.tle.common.scripting.ScriptContextFactory;
import com.tle.core.guice.Bind;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.HtmlEditorButtonDefinition;
import com.tle.web.htmleditor.HtmlEditorInterface;
import com.tle.web.htmleditor.tinymce.service.TinyMceService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@Bind
public class TinyMceEditorSection extends AbstractPrototypeSection<TinyMceModel>
	implements
		HtmlEditorInterface,
		PreRenderable
{
	protected static final int DEFAULT_ROWS = 10;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private TinyMceService tinyMceService;

	@PlugKey("editor.link.fullscreen")
	@Component
	protected Link fullscreen;
	@Component(stateful = false)
	protected TextField html;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		html.setEventHandler(JSHandler.EVENT_PRESUBMIT, tinyMceService.getPreSubmitHandler(html));
		fullscreen.setClickHandler(tinyMceService.getToggleFullscreeenHandler(html, fullscreen));
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "tinyedit"; //$NON-NLS-1$
	}

	@Override
	public Class<TinyMceModel> getModelClass()
	{
		return TinyMceModel.class;
	}

	/**
	 * Properties: width height plugins - A comma delimited list of tiny MCE
	 * plugin ids sessionId - A wizard session ID pageId - A HtmlAttachment UUID
	 * rows
	 */
	@Override
	public void setData(SectionInfo info, Map<String, String> properties, ScriptContextFactory scriptContextFactory)
		throws Exception
	{
		TinyMceModel model = getModel(info);
		model.setScriptContextFactory(scriptContextFactory);
		tinyMceService.populateModel(info, model, properties, false, false, false, false, null, null);
		html.setValue(info, properties.get("html")); //$NON-NLS-1$
	}

	@Override
	public String getHtml(SectionInfo info)
	{
		return html.getValue(info);
	}

	@Override
	public void preRender(PreRenderContext context)
	{
		final TinyMceModel model = getModel(context);
		tinyMceService.preRender(context, html, model);
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return viewFactory.createResult("editor/htmleditor.ftl", context, this);
	}

	@Override
	public LinkedHashMap<String, HtmlEditorButtonDefinition> getAllButtons(SectionInfo info)
	{
		return tinyMceService.getButtons(info);
	}

	@Override
	public List<List<String>> getDefaultButtonConfiguration()
	{
		return tinyMceService.getDefaultButtonConfiguration();
	}

	public TextField getHtml()
	{
		return html;
	}

	public Link getFullscreenLink()
	{
		return fullscreen;
	}
}
