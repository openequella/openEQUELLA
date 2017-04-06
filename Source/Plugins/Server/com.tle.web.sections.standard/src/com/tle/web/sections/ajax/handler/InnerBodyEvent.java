package com.tle.web.sections.ajax.handler;

import com.google.common.collect.ImmutableSet;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGeneratorListener;

@SuppressWarnings("nls")
public final class InnerBodyEvent extends UpdateDomEvent
{
	private static final String BODY_EVENT = "$UP$<BODY>";
	private static InnerBodyEvent INSTANCE = new InnerBodyEvent();

	private InnerBodyEvent()
	{
		super(BODY_EVENT, new UpdateDomKey(null, ImmutableSet.of(AjaxGenerator.AJAXID_BODY), null));
	}

	@Override
	public SectionEvent<?> createEvent(SectionInfo info, String[] params)
	{
		super.createEvent(info, params);
		ParametersEvent event = null;
		if( params.length > 0 )
		{
			MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
			event = new ParametersEvent(EventGeneratorListener.convertToParamMap(params), false);
			minfo.addParametersEvent(event);
		}
		return event;
	}

	@Override
	public int getParameterCount()
	{
		return -1;
	}

	public static void ensureRegistered(SectionInfo info)
	{
		MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
		SectionTree tree = minfo.getRootTree();
		EventGeneratorListener listener = EventGeneratorListener.getForTree(tree);
		synchronized( listener )
		{
			if( listener.getRegisteredHandler(BODY_EVENT) == null )
			{
				listener.registerHandler(INSTANCE);
			}
		}
	}
}
