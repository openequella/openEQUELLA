/*
 * Created on Jun 21, 2004 For "The Learning Edge"
 */
package com.tle.web.wizard.standard.controls;

import com.tle.core.guice.Bind;
import com.tle.core.wizard.controls.HTMLControl;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.wizard.controls.AbstractSimpleWebControl;
import com.tle.web.wizard.controls.CStaticHTML;

/**
 * @author jmaginnis
 */
@Bind
public class StaticHTML extends AbstractSimpleWebControl
{
	private CStaticHTML staticHtml;

	@Override
	public void setWrappedControl(HTMLControl control)
	{
		super.setWrappedControl(control);
		staticHtml = (CStaticHTML) control;
	}

	@Override
	public void doEdits(SectionInfo info)
	{
		// nothing
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return new SimpleSectionResult(staticHtml.getResolvedHtml());
	}

	@Override
	protected ElementId getIdForLabel()
	{
		return null;
	}
}
