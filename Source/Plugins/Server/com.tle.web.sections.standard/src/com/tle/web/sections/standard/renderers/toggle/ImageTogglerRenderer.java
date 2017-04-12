package com.tle.web.sections.standard.renderers.toggle;

import java.io.IOException;
import java.util.Map;

import com.tle.common.Check;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlBooleanState;

@SuppressWarnings("nls")
public class ImageTogglerRenderer extends AbstractHiddenToggler
{
	private static final String CSS_URL = ResourcesService.getResourceHelper(ImageTogglerRenderer.class).url(
		"css/toggler.css");

	public ImageTogglerRenderer(HtmlBooleanState bstate)
	{
		super(bstate);
		addClass("imageToggler");
	}

	@Override
	protected void prepareFirstAttributes(SectionWriter writer, Map<String, String> attrs) throws IOException
	{
		super.prepareFirstAttributes(writer, attrs);
		if( bstate.isChecked() )
		{
			addClass(attrs, "imageTogglerChecked");
		}
		else
		{
			addClass(attrs, "imageTogglerUnchecked");
		}
		SectionRenderable renderable = getNestedRenderable();
		if( renderable != null )
		{
			String altText = SectionUtils.renderToString(writer.getInfo(), renderable);
			if( !Check.isEmpty(altText) )
			{
				attrs.put("alt", altText);
			}
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		super.preRender(info);
		info.addCss(CSS_URL);
	}

	@Override
	protected void writeMiddle(SectionWriter writer) throws IOException
	{
		// nothing
	}

	@Override
	protected void writeEnd(SectionWriter writer) throws IOException
	{
		super.writeEnd(writer);

		// This is to work around a Chrome/Safari rendering issue - see #3058
		writer.write("&nbsp;");
	}
}
