package com.tle.web.sections.standard;

import java.util.Collection;
import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * This class creates renderers based on a renderer name string, and a
 * {@link HtmlComponentState}.
 * 
 * @author jmaginnis
 */
public interface RendererFactory
{
	SectionRenderable getRenderer(SectionInfo info, HtmlComponentState state);

	SectionRenderable convertToRenderer(Object object);

	SectionRenderable convertToRenderer(Object... objects);

	SectionRenderable[] convertToRenderers(Object... objects);

	List<SectionRenderable> convertToRenderers(Collection<?> objects);
}
