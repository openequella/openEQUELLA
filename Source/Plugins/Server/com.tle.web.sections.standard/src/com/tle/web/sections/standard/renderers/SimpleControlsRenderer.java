package com.tle.web.sections.standard.renderers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.ControlsState;
import com.tle.web.sections.standard.dialog.model.DialogControl;

public class SimpleControlsRenderer implements SectionRenderable
{
	private ControlsState state;

	public SimpleControlsRenderer(ControlsState state)
	{
		this.state = state;
	}

	@Override
	public void realRender(SectionWriter writer) throws IOException
	{
		List<SectionRenderable> rendered = new ArrayList<SectionRenderable>();
		List<DialogControl> controls = state.getControls();
		for( DialogControl control : controls )
		{
			rendered.add(SectionUtils.renderSection(writer, control.getControl()));
		}
		for( SectionRenderable renderable : rendered )
		{
			writer.render(renderable);
		}
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		// Nothing to do here
	}
}
