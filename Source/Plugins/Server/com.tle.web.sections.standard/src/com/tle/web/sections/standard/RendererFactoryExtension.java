package com.tle.web.sections.standard;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;

public interface RendererFactoryExtension
{
	SectionRenderable getRenderer(RendererFactory factory, SectionInfo info, String renderer, HtmlComponentState state);
}
