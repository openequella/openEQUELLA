package com.tle.web.template.section.event;

import java.util.EventListener;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.template.section.HelpAndScreenOptionsSection;

/**
 * @author Aaron
 */
public class BlueBarEvent extends AbstractSectionEvent<BlueBarEventListener>
{
	private final RenderContext context;
	private final HelpAndScreenOptionsSection screenOptions;

	public BlueBarEvent(RenderContext context)
	{
		this.context = context;
		screenOptions = context.lookupSection(HelpAndScreenOptionsSection.class);
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, BlueBarEventListener listener) throws Exception
	{
		listener.addBlueBarResults(context, this);
	}

	public void addHelp(SectionRenderable renderable)
	{
		if( renderable != null )
		{
			screenOptions.addTab(context, BlueBarConstants.Type.HELP.content(renderable));
		}
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return BlueBarEventListener.class;
	}

	public void addTab(BlueBarRenderable blueBarRenderable)
	{
		if( blueBarRenderable != null )
		{
			screenOptions.addTab(context, blueBarRenderable);
		}
	}

	public void addScreenOptions(SectionRenderable renderable)
	{
		if( renderable != null )
		{
			screenOptions.addTab(context, BlueBarConstants.Type.SCREENOPTIONS.content(renderable));
		}
	}

}
