package com.tle.web.login;

import com.tle.web.login.LogonSection.LogonModel;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.HtmlRenderer;

/**
 * @author Aaron
 */
public interface LoginLink extends SectionId, HtmlRenderer
{
	void setup(RenderEventContext context, LogonModel model);
}
