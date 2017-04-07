package com.tle.web.sections.events;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SimpleSectionId;

public class RenderEvent extends AbstractSectionEvent<RenderEventListener>
{
	private final String listenerId;
	private final SectionId sectionId;
	private final RenderContext renderContext;
	private RenderResultListener listener;

	public RenderEvent(RenderContext renderContext, String id, RenderResultListener listener)
	{
		this(renderContext, new SimpleSectionId(id), listener);
	}

	public RenderEvent(RenderContext renderContext, SectionId id, RenderResultListener listener)
	{
		this.renderContext = renderContext;
		this.sectionId = id;
		this.listenerId = id != null ? id.getSectionId() : null;
		this.listener = listener;
	}

	public RenderResultListener getListener()
	{
		return listener;
	}

	public void setListener(RenderResultListener listener)
	{
		this.listener = listener;
	}

	public RenderContext getRenderContext()
	{
		return renderContext;
	}

	public void returnResult(SectionResult result)
	{
		if( listener != null )
		{
			listener.returnResult(result, sectionId.getSectionId());
		}
	}

	@Override
	public SectionId getForSectionId()
	{
		return sectionId;
	}

	@Override
	public String getListenerId()
	{
		return listenerId;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, RenderEventListener eventListener) throws Exception
	{
		eventListener.render(new StandardRenderEventContext(sectionId, renderContext, this));
	}

	@Override
	public Class<RenderEventListener> getListenerClass()
	{
		return RenderEventListener.class;
	}

}
