package com.tle.mycontent;

import java.util.List;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * @author Aaron
 */
public interface ContentHandlerSection extends SectionId
{
	List<HtmlComponentState> getMajorActions(RenderContext context);

	List<HtmlComponentState> getMinorActions(RenderContext context);
}
