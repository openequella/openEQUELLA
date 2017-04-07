package com.tle.web.sections.render;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderResultListener;

public class OutputResultListener implements RenderResultListener
{
	private RenderContext info;

	public OutputResultListener(RenderContext info)
	{
		this.info = info;
	}

	@Override
	@SuppressWarnings("nls")
	public void returnResult(SectionResult result, String fromId)
	{
		if( result != null )
		{
			HttpServletResponse response = info.getResponse();
			info.setRendered();
			if( result instanceof SectionRenderable )
			{
				response.setContentType("text/html");
				response.setHeader("Cache-Control", "no-cache, no-store");
				response.setHeader("Pragma", "no-cache");
				response.setDateHeader("Expires", 0);
				response.setCharacterEncoding("UTF-8");

				SectionRenderable renderable = (SectionRenderable) result;
				try( SectionWriter writer = new SectionWriter(response.getWriter(), info) )
				{
					writer.render(renderable);
				}
				catch( IOException e )
				{
					SectionUtils.throwRuntime(e);
				}
			}
		}
	}
}
