package com.tle.web.galleryviewer;

import java.io.IOException;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.jquery.JQuerySelector.Type;
import com.tle.web.sections.jquery.JQueryStatement;
import com.tle.web.sections.jquery.libraries.JQueryFancyBox;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.ObjectExpression;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

public class FancyBoxLinkRenderer extends LinkRenderer
{
	private static final String CLASS_FANCYLINK = "fancyLink"; //$NON-NLS-1$
	private static final FancyBoxLinkReady INST = new FancyBoxLinkReady();

	public FancyBoxLinkRenderer(HtmlLinkState state)
	{
		super(state);
		addClass(CLASS_FANCYLINK);
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		writer.preRender(INST);
		super.realRender(writer);
	}

	@Override
	public void ensureClickable()
	{
		// It is clickable
	}

	public static class FancyBoxLinkReady implements PreRenderable
	{

		@Override
		public void preRender(PreRenderContext info)
		{
			info.addReadyStatements(new JQueryStatement(Type.RAW, ".attachments-browse ." + CLASS_FANCYLINK,
				new FunctionCallExpression(JQueryFancyBox.FANCYBOX, new ObjectExpression("type", "image"))));
		}

	}
}