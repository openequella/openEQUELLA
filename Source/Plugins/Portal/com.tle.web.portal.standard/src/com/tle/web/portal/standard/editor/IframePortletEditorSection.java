package com.tle.web.portal.standard.editor;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import com.dytech.edge.common.Constants;
import com.tle.common.Check;
import com.tle.common.portal.entity.Portlet;
import com.tle.core.guice.Bind;
import com.tle.core.portal.service.PortletEditingBean;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.portal.editor.AbstractPortletEditorSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.TextField;
import com.tle.web.sections.standard.annotations.Component;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
@Bind
public class IframePortletEditorSection
	extends
		AbstractPortletEditorSection<IframePortletEditorSection.IframePortletEditorModel>
{
	private static final String TYPE = "iframe";

	protected static final PluginResourceHelper resources = ResourcesService
		.getResourceHelper(IframePortletEditorSection.class);

	@ViewFactory
	private FreemarkerFactory thisView;

	@Component(name = "u", stateful = false)
	private TextField url;

	@Override
	protected SectionRenderable customRender(RenderEventContext context, IframePortletEditorModel model,
		PortletEditingBean portlet) throws Exception
	{
		return thisView.createResult("edit/editiframeportlet.ftl", context);
	}

	@Override
	protected Portlet createNewPortlet()
	{
		return new Portlet(TYPE);
	}

	@Override
	protected void customLoad(SectionInfo info, PortletEditingBean portlet)
	{
		final String u = portlet.getConfig();
		if( !Check.isEmpty(u) )
		{
			url.setValue(info, u);
		}
		else
		{
			url.setValue(info, "http://");
		}
	}

	@Override
	protected void customSave(SectionInfo info, PortletEditingBean portlet)
	{
		portlet.setConfig(url.getValue(info));
	}

	@Override
	protected void customClear(SectionInfo info)
	{
		url.setValue(info, Constants.BLANK);
	}

	@Override
	protected void customValidate(SectionInfo info, PortletEditingBean portlet, Map<String, Object> errors)
	{
		try
		{
			final URL u = new URL(url.getValue(info));
			try
			{
				u.openConnection().connect();
			}
			catch( Exception e )
			{
				errors.put(
					"url",
					resources.getString("editor.rss.error.url.notreachable",
						e.getClass().getName() + ' ' + e.getMessage()));
			}
		}
		catch( MalformedURLException mal )
		{
			errors.put("url", resources.getString("editor.rss.error.url.notvalid"));
		}
	}

	@Override
	public Class<IframePortletEditorModel> getModelClass()
	{
		return IframePortletEditorModel.class;
	}

	public TextField getUrl()
	{
		return url;
	}

	public static class IframePortletEditorModel extends AbstractPortletEditorSection.AbstractPortletEditorModel
	{
		// Nothing by default
	}

	@Override
	public SectionRenderable renderHelp(RenderContext context)
	{
		return thisView.createResult("help/iframeportleteditorhelp.ftl", this);
	}
}
